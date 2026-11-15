"""The message list's menu: unread only, and marking the page read."""

import pytest


@pytest.mark.fake_mail
def test_unread_only_and_mark_read(hmi):
    hmi.go("MailScreen")
    hmi.select(r"E39 list, 3$")
    hmi.wait_for_screen("MessageListScreen")

    hmi.select(r"^☰$")
    hmi.wait_for(lambda s: s.side_pane_open, "the menu")
    names = [t.text for t in hmi.items()]
    assert names == ["Go Back", "New Message", "Show Unread Only", "Mark Page as Read"], names
    hmi.shot("menu")

    hmi.select(r"^Show Unread Only$")
    hmi.expect_text(r"· unread · 1–3 of 3$")
    rows = [t.text for t in hmi.items() if "[e39]" in t.text]
    assert len(rows) == 3 and all(r.startswith("● ") for r in rows), rows
    hmi.shot("unread-only")

    hmi.select(r"^☰$")
    hmi.select(r"^Mark Page as Read$")
    hmi.expect_text(r"^No unread messages$")
    hmi.shot("all-read")

    hmi.select(r"^Back$")
    hmi.wait_for_screen("MailScreen")
    hmi.expect_text(r"^📁 E39 list$")
