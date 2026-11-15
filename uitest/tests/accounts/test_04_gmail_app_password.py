"""Gmail: the form asks for an app password; an ordinary password is refused with a hint; an app password works."""

import pytest


@pytest.mark.fake_accounts
def test_gmail_app_password(hmi):
    hmi.go("PimAccountsScreen")
    hmi.select(r"^Add Email Account\.\.\.$")
    hmi.wait_for_screen("AddMailAccountScreen")
    hmi.select(r"^E-MAIL ADDRESS")
    hmi.type_text("car@gmail.com")
    hmi.wait_for(lambda s: any(t.text.startswith("APP PASSWORD, ") for t in hmi.texts()), "the app password hint")
    hmi.expect_text(r"myaccount\.google\.com/apppasswords")
    hmi.expect_text(r"^APP PASSWORD \(NOT YOUR USUAL ONE\), Enter\.\.\.$")
    hmi.shot("gmail")

    hmi.select(r"^APP PASSWORD \(NOT YOUR USUAL ONE\)")
    hmi.type_text("hunter2")
    hmi.select(r"^Add Account$")
    s = hmi.wait_for_notification(r"^Not added$")
    assert "app password" in s.notification.content_text, s.notification.content_text

    hmi.select(r"^APP PASSWORD \(NOT YOUR USUAL ONE\)")
    hmi.type_text("abcdefghijklmnop")
    hmi.select(r"^Add Account$")
    hmi.wait_for_screen("PimAccountsScreen")
    hmi.wait_for_notification(r"^Mail account added$")
    hmi.expect_text(r", car@gmail\.com$")
