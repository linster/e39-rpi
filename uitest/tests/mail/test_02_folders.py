"""The folder list: accounts with unread counts, folders indented, a folder opens its messages."""

import pytest


@pytest.mark.fake_mail
def test_folder_list(hmi):
    hmi.go("MailScreen")
    texts = [t.text for t in hmi.items()]
    assert texts[:2] == ["Go Back", "✉ New Message"]
    assert texts[-1] == "⚙ Mail Settings"
    # The account's name and unread count ride on its first folder.
    assert "HOME, 6 unread, 📥 Inbox, 2" in texts
    assert "WORK, 1 unread, 📥 Inbox, 1" in texts
    assert "📁 Car club, 1" in texts
    hmi.shot("folders")

    hmi.select(r"Car club")
    s = hmi.wait_for_screen("MessageListScreen")
    hmi.expect_text(r"^Home: Inbox / Car club · 1–2 of 2$")
    hmi.expect_text(r"Spring meet in Regina")
    hmi.shot("car-club")

    hmi.select(r"^Back$")
    hmi.wait_for_screen("MailScreen")
