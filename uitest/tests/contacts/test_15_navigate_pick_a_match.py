"""Navigate to an address with several matches (#59): Main Street Diner's "Main St" has no town,
so it's Main Street in Regina or in Ottawa ("street only"), the one nearer the car first. Picking
Ottawa's sets it as the route's start."""

import pytest

from ._navigate import navigate, near, open_contact, wait_text


@pytest.mark.fake_contacts
@pytest.mark.fake_geocode
def test_navigate_pick_a_match(hmi):
    hmi.reset_guidance()
    try:
        # The car is in Kanata: Ottawa's Main Street is nearer.
        hmi.set_position(45.3088, -75.8987)
        open_contact(hmi, r"^Contacts \(Google\)", r"Main Street Diner")
        navigate(hmi, r"^WORK ADDRESS")

        wait_text(hmi, r"^2 matches$")
        matches = [t.text for t in hmi.items()]
        # Near the car (Kanata) first.
        assert matches[1:] == ["Main Street, Ottawa (street only)", "Main Street, Regina (street only)"], matches
        hmi.shot("two-matches")

        hmi.select(r"^Main Street, Ottawa")
        wait_text(hmi, r"^Use as Start$")
        # Go Back: the list again.
        hmi.select(r"^Go Back$")
        wait_text(hmi, r"^2 matches$")
        hmi.select(r"^Main Street, Ottawa")
        wait_text(hmi, r"^Use as Start$")
        hmi.select(r"^Use as Start$")

        hmi.wait_for_screen("SetupRouteSubScreen")
        session = hmi.guidance().session
        assert near(session.start, 45.4086, -75.6795), session
        assert not session.HasField("end"), session
    finally:
        hmi.reset_guidance()
