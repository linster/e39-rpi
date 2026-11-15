"""A contact's details: picture and name, numbers, e-mail, address, birthday, nickname."""

import pytest


@pytest.mark.fake_contacts
def test_details(hmi):
    hmi.go("ContactsScreen")
    hmi.select(r"^☰$")
    hmi.select(r"^Personal")
    hmi.select(r"Anna Kowalski")
    hmi.wait_for_screen("ContactDetailsScreen")
    rows = [t.text for t in hmi.items()]
    assert rows == [
        "Go Back",
        "Anna Kowalski, Personal · Home server",
        "MOBILE ★, +1 306 555 0123",
        "HOME, (306) 555-0177",
        "E-MAIL (HOME) ★, anna@example.com",
        "HOME ADDRESS, 123 Main St\nRegina SK S4P 1A1\nCanada",
        "BIRTHDAY, May 17, 1990",
        "NICKNAME, Annie",
    ], rows
    hmi.shot("anna")

    hmi.back()
    hmi.wait_for_screen("ContactsScreen")
    hmi.select(r"René Lévesque")
    hmi.wait_for_screen("ContactDetailsScreen")
    # Title and organization under the name.
    # No picture: initials; title and organization under the name.
    hmi.expect_text(r"^RL, René Lévesque, Mechanic, Garage Lévesque, Personal · Home server$")
