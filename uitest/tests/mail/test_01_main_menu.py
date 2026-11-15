"""The main menu's Mail entry shows the unread inbox count and opens the mail app."""

import pytest


@pytest.mark.fake_mail
def test_main_menu_mail_entry(hmi):
    hmi.go("BMWMainMenu")
    # Sample mail: 2 unread in Home's inbox, 1 in Work's.
    hmi.expect_text(r"^Mail \(3\)$")
    hmi.shot("main-menu")

    hmi.select(r"^Mail \(3\)$")
    hmi.wait_for_screen("MailScreen")
    hmi.expect_text(r"^MAIL · 7 UNREAD$")
    hmi.shot("mail")

    # Reading a message lowers the count.
    hmi.select(r"Inbox, 2$")
    hmi.wait_for_screen("MessageListScreen")
    hmi.select(r"Road trip plans")
    hmi.wait_for_screen("MessageReaderScreen")
    hmi.go("BMWMainMenu")
    hmi.expect_text(r"^Mail \(2\)$")
