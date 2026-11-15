"""Against the PIM bridge: paging through 250 messages with the bridge's page tokens."""

import pytest


@pytest.mark.bridge_mail
def test_bridge_paging(hmi):
    hmi.go("MailScreen")
    hmi.select(r"^📁 e39-list")
    hmi.wait_for_screen("MessageListScreen")
    hmi.expect_text(r"· 1–100 of 250$")
    hmi.expect_text(r"\(#250\)")
    hmi.shot("page-1")

    hmi.select(r"^Older ▶$")
    hmi.expect_text(r"· 101–200 of 250$")
    hmi.expect_text(r"\(#150\)")
    hmi.select(r"^Older ▶$")
    hmi.expect_text(r"· 201–250 of 250$")
    hmi.expect_text(r"\(#1\)$")
    hmi.expect_text(r"^Older ▶$", present=False)
    hmi.shot("page-3")

    # Reading a message (already read) and coming back keeps the page.
    hmi.select(r"\(#20\)$")
    hmi.wait_for_screen("MessageReaderScreen")
    hmi.expect_text(r"Message 20 of the E39 mailing list")
    hmi.back()
    hmi.wait_for_screen("MessageListScreen")
    hmi.expect_text(r"· 201–250 of 250$")

    hmi.select(r"^◀ Newer$")
    hmi.select(r"^◀ Newer$")
    hmi.expect_text(r"· 1–100 of 250$")
