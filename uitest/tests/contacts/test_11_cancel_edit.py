"""Cancel asks before throwing away changes; with none, it just leaves."""

import pytest


@pytest.mark.fake_contacts
def test_cancel_edit(hmi):
    hmi.go("ContactsScreen")
    hmi.select(r"^☰$")
    hmi.select(r"^New Contact$")
    hmi.wait_for_screen("ContactEditScreen")
    hmi.select(r"^Cancel$")
    hmi.wait_for_screen("ContactsScreen")

    hmi.select(r"^☰$")
    hmi.select(r"^New Contact$")
    hmi.wait_for_screen("ContactEditScreen")
    hmi.select(r"^NICKNAME")
    hmi.type_text("x")
    hmi.select(r"^Cancel$")
    hmi.wait_for(lambda s: s.side_pane_open, "the question")
    hmi.expect_text(r"^Throw Away the Changes\?$")
    hmi.select(r"^Keep Editing$")
    hmi.wait_for(lambda s: not s.side_pane_open, "the question to close")
    hmi.expect_text(r"^NICKNAME, x$")
    hmi.select(r"^Cancel$")
    hmi.select(r"^Throw Away$")
    hmi.wait_for_screen("ContactsScreen")
