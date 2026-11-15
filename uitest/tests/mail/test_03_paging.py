"""The message list pages 100 messages at a time and keeps its page while a message is read."""

import pytest


@pytest.mark.fake_mail
def test_paging(hmi):
    hmi.go("MailScreen")
    hmi.select(r"E39 list")
    hmi.wait_for_screen("MessageListScreen")
    hmi.expect_text(r"· 1–100 of 250$")
    hmi.expect_text(r"#250")
    hmi.shot("page-1")

    hmi.select(r"^Older ▶$")
    hmi.expect_text(r"· 101–200 of 250$")
    hmi.expect_text(r"#150")
    hmi.select(r"^Older ▶$")
    hmi.expect_text(r"· 201–250 of 250$")
    # The last page: no "Older" any more.
    hmi.expect_text(r"^Older ▶$", present=False)
    hmi.shot("page-3")

    # Reading a message and coming back keeps the page.
    hmi.select(r"#50\b")
    hmi.wait_for_screen("MessageReaderScreen")
    hmi.back()
    hmi.wait_for_screen("MessageListScreen")
    hmi.expect_text(r"· 201–250 of 250$")

    hmi.select(r"^◀ Newer$")
    hmi.expect_text(r"· 101–200 of 250$")
