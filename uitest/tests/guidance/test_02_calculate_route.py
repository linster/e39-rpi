""""Calculate Route" asks the maps backend for the route between the chosen points.

The bug: the HMI's "Calculate Route" went through DummyRouteCalculator, which ignored the start
and end and always returned Kanata_path, so the map showed a route through Kanata whatever was
chosen.
"""

import re

import pytest

from _guidance import assert_route_between, assert_stored_matches, fresh, meters, set_up_route


@pytest.mark.maps_backend
def test_calculate_route_uses_the_backend(hmi):
    fresh(hmi)
    start, end, text = set_up_route(hmi)
    assert meters(start, end) > 200, f"start and end only {meters(start, end):.0f} m apart: did the pan work?"

    # The notification has the backend's length and time.
    assert re.fullmatch(r"\d+ km, \d+ min\. Start Navigation\.", text), text

    g = hmi.guidance()
    assert g.session.state == "ROUTE_CALCULATED"
    assert g.route_calculation == "Calculated"
    assert g.route_length_meters > 0 and g.instructions > 0, g
    assert_route_between(g.session, start, end)
    assert_stored_matches(g)

    # The setup screen offers the next steps.
    hmi.expect_text(r"^Start Guidance$")
    hmi.expect_text(r"^Clear Route$")
