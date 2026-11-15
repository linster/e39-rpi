"""A–Z: the letters that have contacts, with counts; picking one starts the page at its first contact."""

import pytest


@pytest.mark.fake_contacts
def test_jump_to_letter(hmi):
    hmi.go("ContactsScreen")
    hmi.select(r"^A–Z$")
    hmi.wait_for(lambda s: s.side_pane_open, "the letters")
    letters = [t.text for t in hmi.items()]
    assert letters[0] == "Go Back"
    assert letters[1] == "A · 11", letters
    # Zoë Adams files under A: no Z.
    assert [l.split(" ")[0] for l in letters[1:]] == [chr(c) for c in range(ord("A"), ord("Z"))], letters
    hmi.shot("letters")

    hmi.select(r"^K · ")
    hmi.expect_text(r"^Contacts · All · 104–203 of 259$")
    first = [t.text for t in hmi.items()][5]
    assert first == "K, Alex Klein, +1 306 555 1100", first
    hmi.shot("k")
