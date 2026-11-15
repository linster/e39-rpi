"""New mail shows a notification, unless switched off or the account is muted."""

import time

import pytest


def no_notification_for(hmi, seconds=3):
    deadline = time.time() + seconds
    while time.time() < deadline:
        s = hmi.state()
        assert not (s.notification.visible and s.notification.top_text.startswith("New mail")), s.notification
        time.sleep(0.2)


@pytest.mark.fake_mail
def test_new_mail_notification(hmi):
    hmi.go("MailScreen")
    hmi.deliver_mail(subject="Are you close?", from_name="Anna Kowalski", from_email="anna@example.com")
    s = hmi.wait_for_notification(r"^New mail from Anna Kowalski$")
    assert s.notification.content_text == "Are you close?"
    hmi.shot("notification")
    # The folder list counts it.
    hmi.expect_text(r"Inbox, 3$")


@pytest.mark.fake_mail
def test_notifications_off(hmi):
    hmi.go("MailSettingsScreen")
    hmi.select(r"Notify About New Mail$")
    hmi.expect_text(r"\[ \] Notify About New Mail$")
    hmi.deliver_mail(subject="Quiet please")
    no_notification_for(hmi)


@pytest.mark.fake_mail
def test_muted_account(hmi):
    hmi.go("MailMuteScreen")
    hmi.select(r"Home$")
    hmi.expect_text(r"\[ \] Home$")
    # Sample new mail goes to Home's inbox.
    hmi.deliver_mail(subject="Muted")
    no_notification_for(hmi)
