"""The reader's actions: mark unread, flag, move to a folder, delete."""

import pytest


def open_actions(hmi):
    hmi.select(r"^☰$")
    hmi.wait_for(lambda s: s.side_pane_open, "the actions")


@pytest.mark.fake_mail
def test_actions(hmi):
    hmi.go("MailScreen")
    hmi.select(r"Inbox, 2$")
    hmi.wait_for_screen("MessageListScreen")
    hmi.select(r"Oil change reminder")
    hmi.wait_for_screen("MessageReaderScreen")
    hmi.press()

    open_actions(hmi)
    names = [t.text for t in hmi.items()]
    # One recipient: no Reply All.
    assert names == ["Go Back", "Forward", "Mark Unread", "Flag", "Move to Folder...", "Delete"], names
    hmi.shot("actions")
    hmi.select(r"^Mark Unread$")
    hmi.expect_text(r"● Unread")

    open_actions(hmi)
    hmi.select(r"^Flag$")
    hmi.expect_text(r"⚑ Flagged")

    open_actions(hmi)
    hmi.select(r"^Move to Folder\.\.\.$")
    hmi.wait_for(lambda s: s.side_pane_open, "the folders")
    names = [t.text for t in hmi.items()]
    assert "📁 Inbox / Car club" in names and not any("Deleted Items" in n for n in names), names
    hmi.shot("move-to")
    hmi.select(r"Car club")
    hmi.wait_for_notification(r"^Moved to Car club$")
    # On to the next message in the list.
    hmi.expect_text(r"^Insurance documents$")

    hmi.select(r"^Back$")
    hmi.wait_for_screen("MessageListScreen")
    hmi.expect_text(r"Oil change reminder", present=False)
    hmi.select(r"^Back$")
    hmi.wait_for_screen("MailScreen")
    hmi.select(r"Car club, 2$")
    hmi.wait_for_screen("MessageListScreen")
    hmi.expect_text(r"^● Prairie Euro Garage, .*, Oil change reminder,\s+⚑$")

    hmi.select(r"Oil change reminder")
    hmi.wait_for_screen("MessageReaderScreen")
    hmi.press()
    open_actions(hmi)
    hmi.select(r"^Delete$")
    hmi.wait_for_notification(r"^Moved to the trash$")
    hmi.go("MailScreen")
    hmi.select(r"Trash")
    hmi.wait_for_screen("MessageListScreen")
    hmi.expect_text(r"Oil change reminder")
