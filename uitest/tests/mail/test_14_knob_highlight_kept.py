"""The knob's item stays highlighted when a SmoothScroll screen changes while it's shown
(a notification, new counts, a toggled label).

This used to fail: SmoothScroll numbered its rows from the highest knob ID allocated so far,
re-read at every layout, so a second layout recreated every row, and
AllocatedIdManager.removeId left the knob on an ID that was gone.
"""

import pytest


@pytest.mark.fake_mail
def test_highlight_survives_new_mail(hmi):
    hmi.go("MailScreen")
    hmi.right(3)
    before = hmi.selected()
    assert before == "📁 Car club, 1", before
    hmi.deliver_mail(subject="While you look")
    hmi.wait_for_notification(r"^New mail from")
    # Same row (the inbox count above it changed).
    assert hmi.selected() == "📁 Car club, 1", hmi.selected()
    # And a press acts on it.
    hmi.press()
    hmi.wait_for_screen("MessageListScreen")
    hmi.expect_text(r"Inbox / Car club")


@pytest.mark.fake_mail
def test_highlight_survives_toggle(hmi):
    hmi.go("MailSettingsScreen")
    hmi.select(r"Notify About New Mail$")
    # The knob stays on the toggled row.
    assert hmi.selected() == "[ ] Notify About New Mail", hmi.selected()
