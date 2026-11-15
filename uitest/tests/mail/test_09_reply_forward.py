"""Reply and Forward fill in the draft from the message."""

import pytest


@pytest.mark.fake_mail
def test_reply_and_forward(hmi):
    hmi.go("MailScreen")
    hmi.select(r"Inbox, 2$")
    hmi.wait_for_screen("MessageListScreen")
    hmi.select(r"Road trip plans")
    hmi.wait_for_screen("MessageReaderScreen")
    hmi.press()

    hmi.select(r"^↩ Reply$")
    hmi.wait_for_screen("MailComposeScreen")
    hmi.expect_text(r"^Reply$")
    hmi.expect_text(r"^TO, Anna Kowalski$")
    hmi.expect_text(r"^SUBJECT, Re: Road trip plans$")
    hmi.expect_text(r"^ORIGINAL MESSAGE, Quoted below the text$")
    hmi.shot("reply")
    # Leaving out the quote, then back in.
    hmi.select(r"^ORIGINAL MESSAGE")
    hmi.expect_text(r"^ORIGINAL MESSAGE, Left out$")
    hmi.select(r"^ORIGINAL MESSAGE")
    # Nothing changed: Cancel doesn't ask.
    hmi.select(r"^Cancel$")
    hmi.wait_for_screen("MessageReaderScreen")

    hmi.press()
    hmi.select(r"^☰$")
    hmi.select(r"^Forward$")
    hmi.wait_for_screen("MailComposeScreen")
    hmi.expect_text(r"^Forward$")
    hmi.expect_text(r"^SUBJECT, Fwd: Road trip plans$")
    hmi.expect_text(r"^ATTACHED, Road trip plans$")
    hmi.expect_text(r"^TO, Add\.\.\.$")
    hmi.shot("forward")
