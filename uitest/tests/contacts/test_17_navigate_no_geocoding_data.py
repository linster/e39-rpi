"""Navigate when the installed map can't look addresses up (#59: built before geocoding, the
backend answers UNAVAILABLE): a notification says so, and to update the map."""

import pytest

from ._navigate import navigate, open_contact


@pytest.mark.fake_contacts
@pytest.mark.fake_geocode
def test_navigate_no_geocoding_data(hmi):
    hmi.set_fake_geocoding(no_geocoding_data=True)
    try:
        open_contact(hmi, r"^Personal", r"Anna Kowalski")
        navigate(hmi, r"^HOME ADDRESS")
        s = hmi.wait_for_notification(r"^Can't look up addresses$", clear=False)
        assert s.notification.content_text == "This map has no address data: update it", s.notification
        hmi.wait_idle(from_now=True)
        hmi.shot("no-geocoding-data")
        hmi.clear_notification()
        assert hmi.state().screen == "ContactDetailsScreen"
    finally:
        hmi.set_fake_geocoding(no_geocoding_data=False)
