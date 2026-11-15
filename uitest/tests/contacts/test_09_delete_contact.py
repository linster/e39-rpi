"""Deleting a contact asks first."""

import pytest


@pytest.mark.fake_contacts
def test_delete_contact(hmi):
    hmi.go("ContactsScreen")
    hmi.select(r"^☰$")
    hmi.select(r"^Personal")
    hmi.select(r"René Lévesque")
    hmi.wait_for_screen("ContactDetailsScreen")
    hmi.select(r"^🗑 Delete Contact\.\.\.$")
    hmi.wait_for(lambda s: s.side_pane_open, "the question")
    hmi.expect_text(r"^Delete René Lévesque\?$")
    hmi.shot("question")
    hmi.select(r"^Keep It$")
    hmi.wait_for(lambda s: not s.side_pane_open, "the question to close")
    hmi.expect_screen("ContactDetailsScreen")

    hmi.select(r"^🗑 Delete Contact\.\.\.$")
    hmi.select(r"^Delete$")
    hmi.wait_for_screen("ContactsScreen")
    hmi.wait_for_notification(r"^Contact deleted$")
    hmi.expect_text(r"^Contacts · Personal · 1–3 of 3$")
    hmi.expect_text(r"René Lévesque", present=False)
