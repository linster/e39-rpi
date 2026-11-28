"""The main menu's Contacts entry opens the list: by name, a letter above each letter's first contact."""

import pytest


@pytest.mark.fake_contacts
def test_contact_list(hmi):
    hmi.go("BMWMainMenu")
    hmi.select(r"^Contacts$")
    hmi.wait_for_screen("ContactsScreen")
    hmi.expect_text(r"^Contacts · All · 1–100 of 260$")
    names = [t.text for t in hmi.items()]
    assert names[:5] == ["Back", "◀", "A–Z", "▶", "☰"], names
    # The letter rides on its first contact; the others have none.
    assert names[5] == "A, Alex Abbott, +1 306 555 1000", names[5]
    assert names[6] == "Brooke Abbott, +1 306 555 1001", names[6]
    hmi.shot("list")
