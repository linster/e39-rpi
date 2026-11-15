"""An account's menu: contacts off (CalDAV), offline, remove (asks first)."""

import pytest


@pytest.mark.fake_accounts
def test_account_actions(hmi):
    hmi.go("PimAccountsScreen")
    hmi.select(r", Home server$")
    hmi.wait_for(lambda s: s.side_pane_open, "the account's menu")
    names = [t.text for t in hmi.items()]
    assert names == ["Go Back", "Sync Now", "Go Offline", "Turn Contacts Off", "Remove..."], names
    hmi.select(r"^Turn Contacts Off$")
    hmi.wait_for_notification(r"^Contacts off$")
    hmi.expect_text(r"^CALDAV · OK, Home server$")

    hmi.select(r", Home server$")
    hmi.select(r"^Go Offline$")
    hmi.expect_text(r"^CALDAV · OFFLINE, Home server$")

    hmi.select(r", Home server$")
    hmi.select(r"^Remove\.\.\.$")
    hmi.wait_for(lambda s: s.side_pane_open, "the question")
    hmi.expect_text(r"^Remove Account\?$")
    hmi.shot("remove")
    hmi.select(r"^Keep It$")
    hmi.wait_for(lambda s: not s.side_pane_open, "the question to close")
    hmi.expect_text(r", Home server$")
    hmi.select(r", Home server$")
    hmi.select(r"^Remove\.\.\.$")
    hmi.select(r"^Remove$")
    hmi.wait_for_notification(r"^Account removed$")
    hmi.expect_text(r", Home server$", present=False)

    # A mail account's menu has no contacts switch.
    hmi.select(r", Home mail · ")
    names = [t.text for t in hmi.items()]
    assert names == ["Go Back", "Sync Now", "Go Offline", "Remove..."], names
