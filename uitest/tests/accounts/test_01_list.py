"""The shared accounts screen: every account (mail first), reached from the calendar, mail and contacts."""

import pytest

ROWS = [
    "Go Back",
    "MAIL · OK, Home mail · stefan@example.com",
    "MAIL · OK, Work · stefan@work.example.com",
    "GOOGLE · OK, Google",
    "CALDAV + CONTACTS · OK, Home server",
    "Add Email Account...",
    "Add CalDAV Account...",
    "Add Google Account...",
    "Sync All Now",
]


@pytest.mark.fake_accounts
def test_accounts_list(hmi):
    hmi.go("PimAccountsScreen")
    assert [t.text for t in hmi.items()] == ROWS, [t.text for t in hmi.items()]
    hmi.shot("accounts")


@pytest.mark.fake_accounts
def test_reached_from_the_apps(hmi):
    # Calendar → Setup → Accounts...
    hmi.go("CalendarSetupRootScreen")
    hmi.select(r"^Accounts\.\.\.$")
    hmi.wait_for_screen("PimAccountsScreen")
    # Mail settings.
    hmi.go("MailSettingsScreen")
    hmi.select(r"^Mail Accounts\.\.\.$")
    hmi.wait_for_screen("PimAccountsScreen")
    # The contact list's menu.
    hmi.go("ContactsScreen")
    hmi.select(r"^☰$")
    hmi.select(r"^Accounts\.\.\.$")
    hmi.wait_for_screen("PimAccountsScreen")
