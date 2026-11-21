"""The map following the GPS (GpsMapCenterProvider), seen as the user sees it: where the cyan GPS
dot is on the map.

Following, the map stays put while the dot moves around inside it and re-centres on the dot when it
gets within EDGE_MARGIN_DP of an edge; a zoom keeps following (and re-centres if the dot is now near
the edge), a pan stops it. Following is started by Map Settings -> Follow GPS (which also follows
from startup: that part is in the unit tests, the HMI isn't restarted here) or, in guidance, by
"Re-center map on current location".

The map's size on screen depends on the theme (a NormalSize theme's window is half a pixel-doubled
one's; the bottom bar's height) and, with --show, on the window manager's frame, so the test doesn't
assume it: where the dot is just after following starts is the map's centre, and the map is
twice that, from the content's top left. One pixel a dp on this computer; dp offsets are Web
Mercator at the scale's zoom level (256 dp tiles in every theme), as in the HMI.
"""

import io
import math
import re

import pytest
from PIL import Image

from _guidance import clear_notifications, fresh, set_up_route

EDGE_MARGIN_DP = 40.0  # GpsMapCenterProvider.EDGE_MARGIN_DP
ZOOM = {"1.6 km": 13, "800 m": 14}  # MapScale.mapZoomLevel
TOLERANCE_PX = 4

START = (45.3470, -75.9190)  # Kanata, on the default map.

DOUBLED_THEME = "BMW Blue (2X)"  # the local target's theme (BmwBlueDoubledPixels)


def at(point, dx, dy, zoom=ZOOM["1.6 km"]):
    """The point dx dp right and dy dp down of point on the map at zoom (inverse Web Mercator)."""
    world = 256.0 * 2 ** zoom
    lat, lng = point
    x = (lng + 180.0) / 360.0 * world + dx
    r = math.radians(lat)
    y = (1.0 - math.log(math.tan(r) + 1.0 / math.cos(r)) / math.pi) / 2.0 * world + dy
    return (math.degrees(math.atan(math.sinh(math.pi * (1.0 - 2.0 * y / world)))), x / world * 360.0 - 180.0)


def gps_dot(hmi):
    """Where the GPS dot is in the screenshot (x, y), or None if it isn't on screen."""
    img = Image.open(io.BytesIO(hmi.screenshot_png())).convert("RGB")
    xs, ys = [], []
    px = img.load()
    for y in range(img.height):
        for x in range(img.width):
            r, g, b = px[x, y]
            if r < 60 and g > 220 and b > 220:  # Color.Cyan, the dot's anti-aliased edge aside
                xs.append(x)
                ys.append(y)
    if len(xs) < 20:
        return None
    return (sum(xs) / len(xs), sum(ys) / len(ys))


class Map:
    """The map on screen, measured from the dot when it's centred on it."""

    def __init__(self, hmi):
        hmi.wait_idle()
        dot = gps_dot(hmi)
        if dot is None:
            hmi.shot("no-dot")
        assert dot is not None, "following: no GPS dot on the map"
        self.cx, self.cy = dot
        self.half_w, self.half_h = self.cx, self.cy

    def offset(self, dot):
        return (dot[0] - self.cx, dot[1] - self.cy)

    def expect_dot(self, hmi, dx, dy, what):
        hmi.wait_idle()
        dot = gps_dot(hmi)
        ok = dot is not None and all(abs(o - e) <= TOLERANCE_PX for o, e in zip(self.offset(dot), (dx, dy)))
        if not ok:
            hmi.shot("dot-wrong")
        assert dot is not None, f"{what}: no GPS dot on the map"
        ox, oy = self.offset(dot)
        assert ok, f"{what}: the dot is at {ox:.1f}, {oy:.1f} dp from the centre, expected {dx:.1f}, {dy:.1f}"


def drive(hmi, point):
    hmi.set_position(*point)
    hmi.wait_idle()


def set_follow_by_default(hmi, on):
    hmi.go("MapFollowGpsSettingsScreen")
    texts = [t.text for t in hmi.texts()]
    if ("✓ Follow GPS by default" in texts) != on:
        hmi.select(r"Follow GPS by default$")
        hmi.wait_idle()
    hmi.expect_text(r"^✓ Follow GPS by default$", present=on)


def set_theme(hmi, name):
    """Theme Selector -> name, then the HMI window closed and opened again: a theme sets the window's
    size (a NormalSize theme's is half a pixel-doubled one's), which only a new window takes."""
    hmi.go("ThemeSelectorScreen")
    hmi.select(rf"^{re.escape(name)}$")
    hmi.wait_idle()
    hmi.reopen_window()
    hmi.go_root()


def map_menu(hmi, item):
    hmi.press()
    hmi.wait_for(lambda s: s.modal_menu_open, "the map's menu to open")
    hmi.wait_idle()
    hmi.select(item)
    hmi.wait_for(lambda s: not s.modal_menu_open, "the map's menu to close")
    hmi.wait_idle()


@pytest.mark.parametrize("theme", [DOUBLED_THEME, "BMW Blue"])
def test_follow_gps_on_the_map(hmi, theme):
    fresh(hmi)
    set_theme(hmi, theme)
    try:
        drive(hmi, START)
        set_follow_by_default(hmi, True)
        hmi.go("MapScreen")
        m = Map(hmi)
        hmi.shot("centred")
        # Where the provider re-centres: past this from the centre.
        edge_x, edge_y = m.half_w - EDGE_MARGIN_DP, m.half_h - EDGE_MARGIN_DP

        # Inside the margin the map stays and the dot moves.
        x_in, y_in = edge_x * 0.7, edge_y * 0.7
        for dx, dy in [(x_in, 0), (-x_in, 0), (0, -y_in), (x_in, y_in)]:
            drive(hmi, at(START, dx, dy))
            m.expect_dot(hmi, dx, dy, f"inside the margin ({dx:.0f}, {dy:.0f})")
        hmi.shot("inside-the-margin")

        # Near the edge: the map re-centres on the car.
        car = at(START, edge_x + 10, 0)
        drive(hmi, car)
        m.expect_dot(hmi, 0, 0, "near the right edge: re-centred")
        hmi.shot("recentred")

        # Zoom in: inside at 1.6 km, twice as far out at 800 m, near the edge: re-centred.
        d = edge_x * 0.6
        drive(hmi, at(car, d, 0))
        m.expect_dot(hmi, d, 0, "before zooming")
        map_menu(hmi, r"^Zoom$")
        hmi.left()
        hmi.press()
        hmi.wait_for(lambda s: not s.modal_menu_open, "zooming to end")
        m.expect_dot(hmi, 0, 0, "zoomed in: re-centred")
        hmi.shot("zoomed-in-recentred")
        car = at(car, d, 0)

        # Pan: following stops; the car can drive off the map.
        map_menu(hmi, r"^Pan ⬌$")
        hmi.right()
        hmi.press()
        hmi.wait_for(lambda s: not s.modal_menu_open, "panning to end")
        hmi.wait_idle()
        panned = gps_dot(hmi)
        assert panned is None or abs(m.offset(panned)[0]) > 20, f"the pan moved the map: dot at {panned}"
        drive(hmi, at(car, 0, -(m.half_h + 100), zoom=ZOOM["800 m"]))
        assert gps_dot(hmi) is None, "panned: the map doesn't follow the car off it"
        hmi.shot("panned-not-following")
    finally:
        set_follow_by_default(hmi, False)
        set_theme(hmi, DOUBLED_THEME)


@pytest.mark.maps_backend
def test_recenter_from_guidance(hmi):
    """In guidance, "Re-center map on current location" goes back to the map, centred on the car and
    following it."""
    set_follow_by_default(hmi, False)
    fresh(hmi)
    set_up_route(hmi, pan_right=12)
    hmi.select(r"^Start Guidance$")
    hmi.wait_for_screen("InGuidanceSubScreen")
    g = hmi.guidance()
    car = (g.session.start.latitude, g.session.start.longitude)
    drive(hmi, car)

    clear_notifications(hmi)
    hmi.select(r"^Re-center map on current location$")
    hmi.wait_for_screen("MapScreen")
    m = Map(hmi)
    # Centred on the car: the dot is in the middle of the map, whatever the map's size.
    img = Image.open(io.BytesIO(hmi.screenshot_png()))
    assert abs(m.cx - img.width / 2) <= TOLERANCE_PX, f"re-centred: the dot is at x {m.cx}, not mid-width"
    hmi.shot("recentred-from-guidance")

    # And following: near the left edge, the map re-centres again.
    car = at(car, -(m.half_w - EDGE_MARGIN_DP + 10), 0)
    drive(hmi, car)
    m.expect_dot(hmi, 0, 0, "following after Re-center")
