"""A waiting login: its QR code again from the accounts list, and cancelling it there."""

import pytest


@pytest.mark.fake_accounts
def test_google_login_cancel(hmi):
    hmi.go("PimAccountsScreen")
    hmi.select(r"^Add Google Account\.\.\.$")
    hmi.wait_for_screen("PhoneLoginScreen")
    hmi.wait_for_notification(r"^Google account added$")
    hmi.select(r"^Go Back$")
    hmi.wait_for_screen("PimAccountsScreen")
    hmi.expect_text(r"^WAITING, Log in to Google\.\.\.$")
    hmi.expect_text(r"^GOOGLE · LOG IN NEEDED, Google$")

    # Its menu: the QR code again.
    hmi.select(r"^WAITING, Log in to Google")
    hmi.wait_for(lambda s: s.side_pane_open, "the login's menu")
    hmi.select(r"^Log In on a Phone \(QR Code\)$")
    hmi.wait_for_screen("PhoneLoginScreen")

    hmi.select(r"^Cancel Login$")
    hmi.wait_for_screen("PimAccountsScreen")
    hmi.wait_for_notification(r"^Login not finished$")
    hmi.expect_text(r"^WAITING", present=False)
