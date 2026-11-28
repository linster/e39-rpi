"""Navigate to a contact's address (#59): Anna Kowalski's address is one match; it becomes the
route's end, and Guidance Setup opens with the end set."""

import pytest

from ._navigate import navigate, near, open_contact, wait_text


@pytest.mark.fake_contacts
@pytest.mark.fake_geocode
def test_navigate_to_address(hmi):
    hmi.reset_guidance()
    try:
        open_contact(hmi, r"^Personal", r"Anna Kowalski")
        navigate(hmi, r"^HOME ADDRESS")

        wait_text(hmi, r"^1 match$")
        hmi.expect_text(r"^123 Main St, Regina SK S4P 1A1, Canada$")
        hmi.shot("one-match")
        hmi.select(r"^123 Main Street, Regina \(address\)$")

        wait_text(hmi, r"^Use as End$")
        hmi.shot("the-match")
        hmi.select(r"^Use as End$")

        hmi.wait_for_screen("SetupRouteSubScreen")
        hmi.expect_text(r"^✓ Set End Location$")
        hmi.expect_text(r"^Set Start Location$")
        session = hmi.guidance().session
        assert session.state == "SETTING_UP", session
        assert near(session.end, 50.4501, -104.6178), session
        assert not session.HasField("start"), session
        hmi.shot("guidance-setup")
    finally:
        hmi.reset_guidance()
