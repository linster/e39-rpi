"""Starting and ending guidance keeps the calculated route: on the map, after a save and read
back, and on the "Guidance Terminated" screen.

The bug: GuidanceSession's restore set the state before the route, and the route setter only
takes a route in READY_TO_CALCULATE, so every saved-and-restored ROUTE_CALCULATED, IN_GUIDANCE or
TERMINATED session lost its route. Screens got the route or not depending on whether they
subscribed before or after the last save; with the dummy calculator, that route was Kanata_path.
"""

import pytest

from _guidance import assert_route_between, assert_stored_matches, fresh, open_guidance_menu, set_up_route


@pytest.mark.maps_backend
def test_end_guidance_keeps_route(hmi):
    fresh(hmi)
    start, end, _ = set_up_route(hmi)
    calculated = list(hmi.guidance().session.route)

    hmi.select(r"^Start Guidance$")
    hmi.wait_for_screen("InGuidanceSubScreen")
    g = hmi.guidance()
    assert g.session.state == "IN_GUIDANCE"
    assert_route_between(g.session, start, end)
    assert_stored_matches(g)
    hmi.shot("in-guidance")

    # Back to the map, and end guidance from its guidance menu.
    hmi.select(r"^View Map$")
    hmi.wait_for_screen("MapScreen")
    open_guidance_menu(hmi)
    hmi.select(r"^Terminate Guidance$")
    hmi.wait_for_notification(r"^Ended Guidance$")
    g = hmi.guidance()
    assert g.session.state == "TERMINATED"
    assert list(g.session.route) == calculated, "the route changed when guidance ended"
    assert_route_between(g.session, start, end)
    assert_stored_matches(g)
    hmi.shot("map-after-terminate")

    # The guidance setup now shows the terminated route.
    open_setup_terminated(hmi)
    hmi.expect_text(r"^Guidance Terminated$")
    hmi.shot("guidance-terminated")


def open_setup_terminated(hmi):
    """Map -> Guidance -> Setup...: with guidance ended, the "Guidance Terminated" screen."""
    open_guidance_menu(hmi)
    hmi.select(r"^Setup\.\.\.$")
    hmi.wait_for_screen("RouteTerminatedSubScreen")
