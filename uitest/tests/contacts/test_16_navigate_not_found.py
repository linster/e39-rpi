"""Navigate to an address the map doesn't have (#59): Carl Carlson's Edmonton address isn't among
the sample geocoder's places. A notification says so; guidance isn't touched."""

import pytest

from ._navigate import navigate, open_contact


@pytest.mark.fake_contacts
@pytest.mark.fake_geocode
def test_navigate_not_found(hmi):
    hmi.reset_guidance()
    open_contact(hmi, r"^Contacts \(Google\)", r"Carl Carlson")
    navigate(hmi, r"^HOME ADDRESS")
    s = hmi.wait_for_notification(r"^Address not found on the map$")
    assert "9 Jasper Ave" in s.notification.content_text, s.notification
    assert hmi.state().screen == "ContactDetailsScreen"
    assert hmi.guidance().session.state == "SETTING_UP"
