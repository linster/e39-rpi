"""The map following the GPS (GpsMapCenterProvider), seen as the user sees it: where the cyan GPS
dot is on the map.

Following, the map stays put while the dot moves around inside it and re-centres on the dot when it
gets within EDGE_MARGIN_DP of an edge; a zoom keeps following (and re-centres if the dot is now near
the edge), a pan stops it. Following is started by Map Settings -> Follow GPS (which also follows
from startup: that part is in the unit tests, the HMI isn't restarted here) or, in guidance, by
"Re-center map on current location".

The map (MapViewer) fills the window's content above the status bar (FOOTER_PX), one pixel a dp on
this computer: 800 x 427 (less with --show, where a window manager's frame and title bar take some
of the window); dp offsets are Web Mercator at the scale's zoom level, as in the HMI.
"""

import io
import math

import pytest
from PIL import Image

from _guidance import clear_notifications, fresh, set_up_route

MAP_W, MAP_H = 800.0, 427.0  # GpsMapCenterProvider's MAP_WIDTH_DP, MAP_HEIGHT_DP
FOOTER_PX = 41  # the status bar (clock, date) below the map
EDGE_MARGIN_DP = 40.0  # GpsMapCenterProvider.EDGE_MARGIN_DP
ZOOM = {"1.6 km": 13, "800 m": 14}  # MapScale.mapZoomLevel
TOLERANCE_PX = 4

START = (45.3470, -75.9190)  # Kanata, on the default map.


def at(point, dx, dy, zoom=ZOOM["1.6 km"]):
    """The point dx dp right and dy dp down of point on the map at zoom (inverse Web Mercator)."""
    world = 256.0 * 2 ** zoom
    lat, lng = point
    x = (lng + 180.0) / 360.0 * world + dx
    r = math.radians(lat)
    y = (1.0 - math.log(math.tan(r) + 1.0 / math.cos(r)) / math.pi) / 2.0 * world + dy
    return (math.degrees(math.atan(math.sinh(math.pi * (1.0 - 2.0 * y / world)))), x / world * 360.0 - 180.0)


def gps_dot(hmi):
    """The GPS dot's offset from the map's centre in dp (right, down), or None if it isn't on screen."""
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
    return (sum(xs) / len(xs) - img.width / 2, sum(ys) / len(ys) - (img.height - FOOTER_PX) / 2)


def expect_dot(hmi, dx, dy, what):
    hmi.wait_idle()
    dot = gps_dot(hmi)
    if dot is None or abs(dot[0] - dx) > TOLERANCE_PX or abs(dot[1] - dy) > TOLERANCE_PX:
        hmi.shot("dot-wrong")
    assert dot is not None, f"{what}: no GPS dot on the map"
    assert abs(dot[0] - dx) <= TOLERANCE_PX and abs(dot[1] - dy) <= TOLERANCE_PX, \
        f"{what}: the dot is at {dot[0]:.1f}, {dot[1]:.1f} dp from the centre, expected {dx}, {dy}"


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


def map_menu(hmi, item):
    hmi.press()
    hmi.wait_for(lambda s: s.modal_menu_open, "the map's menu to open")
    hmi.wait_idle()
    hmi.select(item)
    hmi.wait_for(lambda s: not s.modal_menu_open, "the map's menu to close")
    hmi.wait_idle()


def test_follow_gps_on_the_map(hmi):
    fresh(hmi)
    drive(hmi, START)
    set_follow_by_default(hmi, True)
    try:
        hmi.go("MapScreen")
        expect_dot(hmi, 0, 0, "following: the map is centred on the car")
        hmi.shot("centred")

        # Inside the margin the map stays and the dot moves.
        x_in = MAP_W / 2 - EDGE_MARGIN_DP - 60
        y_in = MAP_H / 2 - EDGE_MARGIN_DP - 50
        for dx, dy in [(x_in, 0), (-x_in, 0), (0, -y_in), (x_in, y_in)]:
            drive(hmi, at(START, dx, dy))
            expect_dot(hmi, dx, dy, f"inside the margin ({dx}, {dy})")
        hmi.shot("inside-the-margin")

        # Near the edge: the map re-centres on the car.
        car = at(START, MAP_W / 2 - EDGE_MARGIN_DP + 15, 0)
        drive(hmi, car)
        expect_dot(hmi, 0, 0, "near the right edge: re-centred")
        hmi.shot("recentred")

        # Zoom in: 200 dp out at 1.6 km is 400 dp out at 800 m, near the edge: re-centred.
        drive(hmi, at(car, 200, 0))
        expect_dot(hmi, 200, 0, "200 dp right, before zooming")
        map_menu(hmi, r"^Zoom$")
        hmi.left()
        hmi.press()
        hmi.wait_for(lambda s: not s.modal_menu_open, "zooming to end")
        expect_dot(hmi, 0, 0, "zoomed in: re-centred")
        hmi.shot("zoomed-in-recentred")
        car = at(car, 200, 0)

        # Pan: following stops; the car can drive off the map.
        map_menu(hmi, r"^Pan ⬌$")
        hmi.right()
        hmi.press()
        hmi.wait_for(lambda s: not s.modal_menu_open, "panning to end")
        hmi.wait_idle()
        panned = gps_dot(hmi)
        assert panned is not None and abs(panned[0]) > 50, f"the pan moved the map: dot at {panned}"
        drive(hmi, at(car, 0, -(MAP_H / 2 + 100), zoom=ZOOM["800 m"]))
        assert gps_dot(hmi) is None, "panned: the map doesn't follow the car off it"
        hmi.shot("panned-not-following")
    finally:
        set_follow_by_default(hmi, False)


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
    expect_dot(hmi, 0, 0, "re-centred on the car")
    hmi.shot("recentred-from-guidance")

    # And following: near the left edge, the map re-centres again.
    car = at(car, -(MAP_W / 2 - EDGE_MARGIN_DP + 15), 0)
    drive(hmi, car)
    expect_dot(hmi, 0, 0, "following after Re-center")
