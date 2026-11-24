"""The Edit Connection side pane (issue 63): the connection's details stay above, the actions
scroll with the knob below them, and Go Back (first, selected when it opens) closes the pane
without changing the connection.

Uses the NetworkManager of the machine the HMI runs on; it only looks, and only presses Go Back.
"""

import re

import pytest

PANE_HEIGHT = 468  # the local target's window


def _visible(item):
    return item.y >= 0 and item.y + item.height <= PANE_HEIGHT


# The first connection, and the first Wi-Fi one (an SSID line, maybe Change Password: taller).
@pytest.mark.parametrize("which", [r".", r"\(Wi-Fi\)"], ids=["first", "wifi"])
def test_pane_scrolls_and_go_back_changes_nothing(hmi, which):
    hmi.go("EditConnectionScreen")
    hmi.wait_for_screen("EditConnectionScreen")
    hmi.wait_idle()

    before = [t.text for t in hmi.items()]
    connections = [t for t in before if t != "Go Back" and re.search(which, t)]
    if not connections:
        pytest.skip(f"NetworkManager has no saved connection matching {which!r} here")
    name = re.match(r"^(.*) \(", connections[0]).group(1)

    hmi.select(re.escape(connections[0]))
    hmi.wait_for(lambda s: s.side_pane_open, "the side pane")
    hmi.wait_idle()
    hmi.shot("pane-open")

    rows = [t.text for t in hmi.items()]
    assert rows[0] == "Go Back", rows
    assert not any(r.startswith("Name: ") for r in rows), f"the details are a knob stop: {rows}"
    assert rows[-1] == "Delete", rows
    assert "Rename" in rows, rows
    assert rows.count("Go Back") == 1, rows
    assert hmi.selected() == "Go Back", hmi.items()
    hmi.expect_text(rf"^Name: {re.escape(name)}$")

    # Every row comes into view as the knob reaches it.
    for i in range(1, len(rows)):
        hmi.right()
        items = hmi.items()
        sel = [t for t in items if t.selected]
        assert sel and sel[0].text == rows[i], (i, [t.text for t in items if t.selected])
        assert _visible(sel[0]), (sel[0].text, sel[0].y, sel[0].height)
        hmi.expect_text(r"^Name: ")
    hmi.shot("pane-bottom")

    # And back up to Go Back.
    hmi.left(len(rows) - 1)
    go_back = [t for t in hmi.items() if t.selected]
    assert go_back and go_back[0].text == "Go Back" and _visible(go_back[0]), go_back
    hmi.shot("pane-top-again")

    hmi.press()
    hmi.wait_for(lambda s: not s.side_pane_open, "the side pane to close")
    s = hmi.state()
    assert s.screen == "EditConnectionScreen", s.screen
    assert s.main_knob_listener, "knob didn't go back to the list"
    hmi.wait_idle()
    assert [t.text for t in hmi.items()] == before
    hmi.shot("closed")
