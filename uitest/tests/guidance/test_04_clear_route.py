""""Clear Route" drops the route, its instructions and the route status: the next route starts
from nothing."""

import pytest

from _guidance import fresh, set_up_route


@pytest.mark.maps_backend
def test_clear_route(hmi):
    fresh(hmi)
    set_up_route(hmi)
    assert hmi.guidance().instructions > 0

    hmi.select(r"^Clear Route$")
    hmi.wait_for(lambda _: hmi.guidance().session.state == "SETTING_UP", "the session to be cleared")
    hmi.wait_for(lambda _: hmi.guidance().instructions == 0, "the instructions to be cleared")
    g = hmi.guidance()
    assert not g.session.has_route and not g.stored.has_route, g
    assert not g.session.HasField("start") and not g.session.HasField("end"), g
    assert g.route_calculation == "Idle", g.route_calculation

    # Points can be chosen again: no check marks, Calculate Route complains.
    hmi.wait_idle()
    hmi.expect_text(r"^Set Start Location$")
    hmi.expect_text(r"^Set End Location$")
    hmi.shot("cleared")
