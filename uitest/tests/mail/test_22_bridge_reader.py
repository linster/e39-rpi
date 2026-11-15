"""Against the PIM bridge: the reader's actions, each undone again (the bridge's mail stays as it was)."""

import pytest


def actions(hmi):
    hmi.select(r"^☰$")
    hmi.wait_for(lambda s: s.side_pane_open, "the actions")


@pytest.mark.bridge_mail
def test_bridge_reader_actions(hmi):
    hmi.go("MailScreen")
    hmi.select(r"📥 inbox")
    hmi.wait_for_screen("MessageListScreen")
    hmi.select(r"Oil change reminder")
    hmi.wait_for_screen("MessageReaderScreen")
    hmi.expect_text(r"^From: Garage Service")
    hmi.press()  # the knob to the toolbar

    actions(hmi)
    hmi.select(r"^Mark Unread$")
    hmi.expect_text(r"● Unread")
    actions(hmi)
    hmi.select(r"^Mark Read$")
    hmi.expect_text(r"● Unread", present=False)

    actions(hmi)
    hmi.select(r"^Flag$")
    hmi.expect_text(r"⚑ Flagged")
    hmi.shot("flagged")
    actions(hmi)
    hmi.select(r"^Remove Flag$")
    hmi.expect_text(r"⚑ Flagged", present=False)

    # No trash folder in Local Folders: the bridge refuses, the message stays.
    actions(hmi)
    hmi.select(r"^Delete$")
    hmi.wait_for_notification(r"^Not deleted$")
    hmi.expect_text(r"^Oil change reminder$")

    # Move to e39-list, then back.
    actions(hmi)
    hmi.select(r"^Move to Folder\.\.\.$")
    hmi.wait_for(lambda s: s.side_pane_open, "the folders")
    hmi.select(r"e39-list$")
    hmi.wait_for_notification(r"^Moved to e39-list$")
    hmi.go("MailScreen")
    hmi.select(r"^📁 e39-list")
    hmi.wait_for_screen("MessageListScreen")
    hmi.wait_for(lambda s: any("Oil change reminder" in t.text for t in hmi.texts()), "the moved message in e39-list")
    hmi.select(r"Oil change reminder")
    hmi.wait_for_screen("MessageReaderScreen")
    hmi.press()
    actions(hmi)
    hmi.select(r"^Move to Folder\.\.\.$")
    hmi.select(r"inbox$")
    hmi.wait_for_notification(r"^Moved to inbox$")
    hmi.go("MailScreen")
    hmi.select(r"📥 inbox")
    hmi.wait_for_screen("MessageListScreen")
    hmi.wait_for(lambda s: any("Oil change reminder" in t.text for t in hmi.texts()), "the message back in the inbox")


@pytest.mark.bridge_mail
def test_bridge_newer_older(hmi):
    hmi.go("MailScreen")
    hmi.select(r"📥 inbox")
    hmi.wait_for_screen("MessageListScreen")
    hmi.select(r"November newsletter")
    hmi.wait_for_screen("MessageReaderScreen")
    hmi.press()
    hmi.select(r"^▼ Older$")
    hmi.expect_text(r"^Oil change reminder$")
    hmi.select(r"^▲ Newer$")
    hmi.expect_text(r"^November newsletter$")
