"""A read-only address book's contacts can't be edited or deleted."""

import pytest


@pytest.mark.fake_contacts
def test_read_only(hmi):
    hmi.go("ContactsScreen")
    hmi.select(r"^☰$")
    hmi.select(r"^Company")
    hmi.select(r"IT Help Desk")
    hmi.wait_for_screen("ContactDetailsScreen")
    names = [t.text for t in hmi.items()]
    assert not any("Edit Contact" in n or "Delete Contact" in n for n in names), names
    # And it isn't offered for new contacts.
    hmi.back()
    hmi.wait_for_screen("ContactsScreen")
    hmi.select(r"^☰$")
    hmi.select(r"^New Contact$")
    hmi.wait_for_screen("ContactEditScreen")
    hmi.select(r"^ADDRESS BOOK")
    hmi.wait_for(lambda s: s.side_pane_open, "the address books")
    books = [t.text for t in hmi.items()]
    assert books == ["Go Back", "Personal (Home server)", "Contacts (Google)", "Members (Car club)"], books
