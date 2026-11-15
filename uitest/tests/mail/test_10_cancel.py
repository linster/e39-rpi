"""Cancel asks before throwing away what was written."""

import pytest


@pytest.mark.fake_mail
def test_cancel_asks(hmi):
    hmi.go("MailScreen")
    hmi.select(r"^✉ New Message$")
    hmi.wait_for_screen("MailComposeScreen")
    hmi.select(r"^Quick Reply\.\.\.$")
    hmi.select(r"^Thanks!$")
    hmi.expect_text(r"^MESSAGE, Thanks!$")

    hmi.select(r"^Cancel$")
    hmi.wait_for(lambda s: s.side_pane_open, "the question")
    hmi.expect_text(r"^Throw Away This Message\?$")
    hmi.shot("question")
    hmi.select(r"^Keep Writing$")
    hmi.wait_for(lambda s: not s.side_pane_open, "the question to close")
    hmi.expect_text(r"^MESSAGE, Thanks!$")



@pytest.mark.fake_mail
@pytest.mark.xfail(strict=True, reason="after the question closes, the highlighted row and the row a press acts "
                                       "on differ: Cancel highlighted, Send runs (TODO.md, SmoothScroll)")
def test_cancel_again_after_keep_writing(hmi):
    hmi.go("MailScreen")
    hmi.select(r"^✉ New Message$")
    hmi.wait_for_screen("MailComposeScreen")
    hmi.select(r"^Quick Reply\.\.\.$")
    hmi.select(r"^Thanks!$")
    hmi.select(r"^Cancel$")
    hmi.select(r"^Keep Writing$")
    hmi.wait_for(lambda s: not s.side_pane_open, "the question to close")
    # The knob shows no item now; a turn shows Cancel highlighted...
    hmi.select(r"^Cancel$")
    # ...and the press should ask again.
    hmi.wait_for(lambda s: s.side_pane_open, "the question", timeout_s=3)
    hmi.select(r"^Throw Away$")
    hmi.wait_for_screen("MailScreen")
