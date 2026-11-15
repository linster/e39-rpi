"""Editing a contact: a number's type and the nickname; only those change."""

import pytest


@pytest.mark.fake_contacts
def test_edit_contact(hmi):
    hmi.go("ContactsScreen")
    hmi.select(r"^☰$")
    hmi.select(r"^Personal")
    hmi.select(r"Anna Kowalski")
    hmi.wait_for_screen("ContactDetailsScreen")
    hmi.select(r"^✎ Edit Contact$")
    hmi.wait_for_screen("ContactEditScreen")
    hmi.expect_text(r"^Edit: Anna Kowalski$")
    hmi.expect_text(r"^NICKNAME, Annie$")

    hmi.select(r"^HOME, \(306\) 555-0177$")
    hmi.wait_for(lambda s: s.side_pane_open, "the number's menu")
    names = [t.text for t in hmi.items()]
    assert names[:3] == ["Go Back", "Change Number...", "Make It Mobile"], names
    assert names[-2:] == ["Make It Preferred", "Remove"], names
    hmi.select(r"^Make It Work$")
    hmi.expect_text(r"^WORK, \(306\) 555-0177$")

    hmi.select(r"^NICKNAME")
    hmi.clear_text(len("Annie"))
    hmi.type_text("Ania")
    hmi.expect_text(r"^NICKNAME, Ania$")
    hmi.shot("edited")

    hmi.select(r"^Save$")
    hmi.wait_for_screen("ContactDetailsScreen")
    hmi.wait_for_notification(r"^Contact changed$")
    hmi.expect_text(r"^WORK, \(306\) 555-0177$")
    hmi.expect_text(r"^NICKNAME, Ania$")
    # The rest as it was (the picture too).
    hmi.expect_text(r"^E-MAIL \(HOME\) ★, anna@example\.com$")
    hmi.expect_text(r"^AK, Anna Kowalski", present=False)
