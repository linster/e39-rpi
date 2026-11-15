"""Editing the quick replies: add (typed), move, delete."""

import pytest


@pytest.mark.fake_mail
def test_quick_replies(hmi):
    hmi.go("QuickRepliesScreen")
    hmi.select(r"^Add Quick Reply\.\.\.$")
    hmi.wait_for(lambda s: s.keyboard_open, "the keyboard")
    hmi.type_text("Be there soon")
    names = [t.text for t in hmi.items()]
    assert names[-1] == "Be there soon", names
    hmi.shot("added")

    hmi.select(r"^Be there soon$")
    hmi.wait_for(lambda s: s.side_pane_open, "the reply's menu")
    names = [t.text for t in hmi.items()]
    assert names == ["Go Back", "Edit...", "Move Up", "Delete"], names
    hmi.select(r"^Move Up$")
    names = [t.text for t in hmi.items()]
    assert names[-2:] == ["Be there soon", "Thanks!"], names

    hmi.select(r"^Be there soon$")
    hmi.select(r"^Delete$")
    hmi.expect_text(r"^Be there soon$", present=False)
