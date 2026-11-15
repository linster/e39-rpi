"""The reader's ▲ Newer / ▼ Older go through the list page it was opened from."""

import pytest


@pytest.mark.fake_mail
def test_newer_older(hmi):
    hmi.go("MailScreen")
    hmi.select(r"Inbox, 2$")
    hmi.wait_for_screen("MessageListScreen")
    hmi.select(r"Oil change reminder")
    hmi.wait_for_screen("MessageReaderScreen")
    hmi.expect_text(r"^Oil change reminder$")
    hmi.press()  # the knob to the toolbar

    hmi.select(r"^▼ Older$")
    hmi.expect_text(r"^Insurance documents$")
    hmi.select(r"^▲ Newer$")
    hmi.select(r"^▲ Newer$")
    hmi.expect_text(r"^Road trip plans$")
    # The newest: nowhere newer to go.
    hmi.expect_text(r"^▲$")
    hmi.shot("newest")
