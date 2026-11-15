"""A refused password: the HMI says so and keeps the form."""

import pytest


@pytest.mark.fake_accounts
def test_refused_password(hmi):
    hmi.go("PimAccountsScreen")
    hmi.select(r"^Add Email Account\.\.\.$")
    hmi.wait_for_screen("AddMailAccountScreen")
    hmi.select(r"^E-MAIL ADDRESS")
    hmi.type_text("me@example.com")
    hmi.wait_for(lambda s: any(t.text.startswith("SERVERS, ") for t in hmi.texts()), "the servers")
    # A guess: said so.
    hmi.expect_text(r"^SERVERS, a guess: check them$")
    hmi.select(r"^PASSWORD")
    hmi.type_text("wrong")
    hmi.select(r"^Add Account$")
    s = hmi.wait_for_notification(r"^Not added$")
    assert "refused" in s.notification.content_text, s.notification.content_text
    hmi.expect_screen("AddMailAccountScreen")
    hmi.expect_text(r"^E-MAIL ADDRESS, me@example\.com$")
