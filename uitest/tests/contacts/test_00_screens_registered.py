"""The contacts app's screens are registered with the navigator."""

CONTACTS_SCREENS = [
    "ca.stefanm.ibus.gui.pim.contacts.views.ContactsScreen",
    "ca.stefanm.ibus.gui.pim.contacts.views.ContactDetailsScreen",
]


def test_contacts_screens_registered(hmi):
    registered, unregistered = hmi.list_screens()
    assert unregistered == [], unregistered
    missing = [s for s in CONTACTS_SCREENS if s not in registered]
    assert missing == [], missing
