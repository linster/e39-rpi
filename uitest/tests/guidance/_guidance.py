"""Helpers for the guidance tests: the HMI's guidance screens, driven with the knob as a user would.

A route is set up the way the HMI offers: Map -> Guidance -> Setup..., then "Set Start Location" /
"Set End Location" -> "Select on Map" -> (pan) -> "Select Center", then "Calculate Route". The
probe's GetGuidance tells the tests what the session holds.
"""

import math

# Kanata_path (MapDebug.kt): the route the old DummyRouteCalculator returned for any start and end.
KANATA_PATH_FIRST = (45.31599199535223, -75.91989904925909)
KANATA_PATH_LAST = (45.32034835430682, -75.90555126119544)
KANATA_PATH_POINTS = 20

# A route's ends are snapped to the nearest road, so they're near the chosen points, not on them.
SNAP_METERS = 500


def meters(a, b):
    """Distance between two (lat, lng) points, haversine."""
    (lat1, lng1), (lat2, lng2) = a, b
    p1, p2 = math.radians(lat1), math.radians(lat2)
    dp, dl = p2 - p1, math.radians(lng2 - lng1)
    h = math.sin(dp / 2) ** 2 + math.cos(p1) * math.cos(p2) * math.sin(dl / 2) ** 2
    return 2 * 6371000 * math.asin(math.sqrt(h))


def ll(point):
    """A probe GeoPoint as (lat, lng)."""
    return (point.latitude, point.longitude)


def fresh(hmi):
    """No guidance, no route, on the root screen."""
    hmi.reset_guidance()
    hmi.close_overlays()
    hmi.go_root()
    g = hmi.guidance()
    assert g.session.state == "SETTING_UP" and not g.session.has_route, g


def open_map_menu(hmi):
    """The map's view menu (a press on the bare map)."""
    hmi.press()
    hmi.wait_for(lambda s: s.modal_menu_open, "the map's menu to open")
    hmi.wait_idle()


def open_guidance_menu(hmi):
    """Map -> menu -> Guidance: the map's guidance menu (Setup..., Terminate Guidance, ...)."""
    s = hmi.state()
    if s.screen != "MapScreen":
        hmi.go("MapScreen")
    open_map_menu(hmi)
    hmi.select(r"^Guidance$")
    hmi.wait_for(lambda s: s.modal_menu_open, "the guidance menu to open")
    hmi.wait_idle()
    hmi.expect_text(r"^Terminate Guidance$")


def open_setup(hmi):
    """Map -> Guidance -> Setup...: the route setup screen."""
    open_guidance_menu(hmi)
    hmi.select(r"^Setup\.\.\.$")
    return hmi.wait_for_screen("SetupRouteSubScreen")


def choose_on_map(hmi, which, pan_right=0):
    """On the setup screen: "Set <which> Location" -> "Select on Map", pan right some clicks,
    "Select Center". Returns the chosen point (lat, lng) from the session."""
    hmi.select(rf"Set {which} Location$")
    hmi.wait_for(lambda s: s.side_pane_open, "the location side pane")
    hmi.wait_idle()
    hmi.select(r"^Select on Map$")
    hmi.wait_for_screen("MapScreen")
    if pan_right:
        open_map_menu(hmi)
        hmi.select(r"^Pan ⬌$")
        hmi.wait_for(lambda s: not s.modal_menu_open, "the map's menu to close")
        hmi.wait_idle()
        hmi.right(pan_right)
        hmi.press()  # leaves Pan
    open_map_menu(hmi)
    hmi.select(r"^Select Center$")
    hmi.wait_for_screen("SetupRouteSubScreen")
    session = hmi.guidance().session
    point = session.start if which == "Start" else session.end
    assert session.HasField("start" if which == "Start" else "end"), f"no {which} point after Select Center: {session}"
    return ll(point)


def calculate(hmi):
    """"Calculate Route" on the setup screen; waits for the backend's answer. Returns the
    "Calculated Route" notification's content text."""
    hmi.select(r"^Calculate Route$")
    s = hmi.wait_for_notification(r"^(Calculated Route|Route Failed)$", timeout_s=60)
    assert s.notification.top_text == "Calculated Route", \
        f"{s.notification.top_text}: {s.notification.content_text} (is the maps backend running?)"
    hmi.wait_for(lambda _: hmi.guidance().session.state == "ROUTE_CALCULATED", "the route in the session")
    return s.notification.content_text


def set_up_route(hmi, pan_right=8):
    """Start and end chosen on the map (the end panned east), route calculated. Returns
    (start, end, notification text)."""
    open_setup(hmi)
    start = choose_on_map(hmi, "Start")
    end = choose_on_map(hmi, "End", pan_right=pan_right)
    assert hmi.guidance().session.state == "READY_TO_CALCULATE"
    hmi.shot("ready-to-calculate")
    text = calculate(hmi)
    hmi.shot("route-calculated")
    return start, end, text


def assert_route_between(session, start, end):
    """The session's route is the backend's route from start to end, not Kanata_path."""
    route = [ll(p) for p in session.route]
    assert session.has_route and len(route) >= 2, f"no route in the session ({session.state})"
    assert not (len(route) == KANATA_PATH_POINTS and meters(route[0], KANATA_PATH_FIRST) < 1
                and meters(route[-1], KANATA_PATH_LAST) < 1), "the route is Kanata_path (the dummy route)"
    assert meters(route[0], start) < SNAP_METERS, f"route starts {meters(route[0], start):.0f} m from the start"
    assert meters(route[-1], end) < SNAP_METERS, f"route ends {meters(route[-1], end):.0f} m from the end"


def assert_stored_matches(g):
    """The saved-and-restored session (what the map draws after a save, and what the HMI loads at
    start) is the session in memory."""
    assert g.stored.state == g.session.state, (g.stored.state, g.session.state)
    assert g.stored.has_route == g.session.has_route, \
        f"route in memory: {g.session.has_route}, after a save and read back: {g.stored.has_route}"
    assert len(g.stored.route) == len(g.session.route)
    if g.session.route:
        assert meters(ll(g.stored.route[0]), ll(g.session.route[0])) < 1
        assert meters(ll(g.stored.route[-1]), ll(g.session.route[-1])) < 1


# --- driving along the route (the probe's SetPosition, as GPS would) ----------------------

class Route:
    """The session's route with distances along it, for feeding positions."""

    def __init__(self, guidance_state):
        self.points = [ll(p) for p in guidance_state.session.route]
        self.cum = [0.0]
        for a, b in zip(self.points, self.points[1:]):
            self.cum.append(self.cum[-1] + meters(a, b))
        self.length = self.cum[-1]

    def at(self, d):
        """The point d meters along the route."""
        d = max(0.0, min(d, self.length))
        for i in range(1, len(self.points)):
            if self.cum[i] >= d:
                seg = self.cum[i] - self.cum[i - 1]
                f = (d - self.cum[i - 1]) / seg if seg > 0 else 0.0
                (lat1, lng1), (lat2, lng2) = self.points[i - 1], self.points[i]
                return (lat1 + (lat2 - lat1) * f, lng1 + (lng2 - lng1) * f)
        return self.points[-1]

    def meters_at(self, instruction):
        """How far along the route a maneuver is (its path index)."""
        return self.cum[instruction.path_index]

    def last_outside(self, instruction, margin=40.0):
        """The furthest distance along the route before the maneuver that's still outside its
        trigger radius (straight line), by margin."""
        d = self.meters_at(instruction)
        while d > 0 and meters(self.at(d), ll(instruction.location)) <= instruction.trigger_radius_meters + margin:
            d -= 10.0
        return d


def drive(hmi, route, from_m, to_m, step_m=20.0):
    """Feeds positions from from_m to to_m along the route; returns the last guidance state."""
    g = None
    d = from_m
    while True:
        g = hmi.set_position(*route.at(d))
        if d >= to_m:
            return g
        d = min(d + step_m, to_m)


def clear_notifications(hmi):
    """Guidance's instruction notifications cover the screen's top: hide whatever shows."""
    if hmi.state().notification.visible:
        hmi.clear_notification()


def panel_titles(hmi):
    """The side panel's header texts: "Next  n/N" (a preview), "Guidance  n/N" or "Destination"."""
    import re
    # Drawn in capitals (the header style); matched ignoring case.
    return [t.text for t in hmi.texts()
            if re.fullmatch(r"(Next|Guidance)\s+\d+/\d+|Next: Destination|Destination", t.text, re.I)]
