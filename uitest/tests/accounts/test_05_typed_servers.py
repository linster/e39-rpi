"""Nothing found for the address: the servers are typed in (server, security and its usual port)."""

import pytest


@pytest.mark.fake_accounts
def test_typed_servers(hmi):
    hmi.go("PimAccountsScreen")
    hmi.select(r"^Add Email Account\.\.\.$")
    hmi.wait_for_screen("AddMailAccountScreen")
    hmi.select(r"^E-MAIL ADDRESS")
    hmi.type_text("me@home.invalid")
    hmi.wait_for_notification(r"^No servers found$")
    hmi.expect_text(r"^SERVERS, Typed in$")
    hmi.expect_text(r"^INCOMING \(IMAP\), Enter\.\.\.$")

    for role in ("INCOMING", "OUTGOING"):
        hmi.select(rf"^{role}")
        hmi.wait_for(lambda s: s.side_pane_open, "the server's menu")
        hmi.select(r"^Server: ")
        hmi.type_text("mail.home.lan")
    hmi.expect_text(r"^INCOMING \(IMAP\), mail\.home\.lan:993 · TLS$")
    hmi.select(r"^OUTGOING")
    hmi.select(r"^Use STARTTLS$")
    hmi.expect_text(r"^OUTGOING \(SMTP\), mail\.home\.lan:587 · STARTTLS$")
    hmi.shot("typed")

    hmi.select(r"^PASSWORD")
    hmi.type_text("secret")
    hmi.select(r"^Add Account$")
    hmi.wait_for_screen("PimAccountsScreen")
    hmi.wait_for_notification(r"^Mail account added$")
    hmi.expect_text(r", me@home\.invalid$")
