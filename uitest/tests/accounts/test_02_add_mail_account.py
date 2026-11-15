"""Adding a mail account: the address, the servers found (the provider's), name and password, added."""

import pytest


@pytest.mark.fake_accounts
def test_add_mail_account(hmi):
    hmi.go("PimAccountsScreen")
    hmi.select(r"^Add Email Account\.\.\.$")
    hmi.wait_for_screen("AddMailAccountScreen")
    names = [t.text for t in hmi.items()]
    assert names == ["Cancel", "E-MAIL ADDRESS, Enter...", "YOUR NAME, As the mail's sender...", "PASSWORD, Enter...",
                     "ACCOUNT NAME, (the address)", "Add Account"], names

    hmi.select(r"^E-MAIL ADDRESS")
    hmi.type_text("dana@fastmail.com")
    hmi.wait_for(lambda s: any(t.text.startswith("SERVERS, ") for t in hmi.texts()), "the servers")
    hmi.expect_text(r"^SERVERS, Fastmail: the provider's settings$")
    hmi.expect_text(r"^INCOMING \(IMAP\), imap\.fastmail\.com:993 · TLS$")
    hmi.expect_text(r"^OUTGOING \(SMTP\), smtp\.fastmail\.com:465 · TLS$")
    hmi.expect_text(r"^APP PASSWORD", present=False)

    hmi.select(r"^YOUR NAME")
    hmi.type_text("Dana")
    hmi.select(r"^PASSWORD")
    hmi.type_text("secret")
    hmi.expect_text(r"^PASSWORD, ••••••$")
    hmi.shot("ready")

    hmi.select(r"^Add Account$")
    hmi.wait_for_screen("PimAccountsScreen")
    hmi.wait_for_notification(r"^Mail account added$")
    hmi.expect_text(r"^MAIL · (SYNCING|OK), dana@fastmail\.com$")
    hmi.shot("added")
