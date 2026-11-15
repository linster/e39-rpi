"""Mail's contact picker: recent people first, then the address book; search finds the rest."""

import pytest


@pytest.mark.fake_contacts
@pytest.mark.fake_mail
def test_contact_picker(hmi):
    hmi.go("MailScreen")
    hmi.select(r"^✉ New Message$")
    hmi.wait_for_screen("MailComposeScreen")
    hmi.select(r"^TO, Add")
    hmi.select(r"^Add from Contacts\.\.\.$")
    hmi.wait_for_screen("ContactPickerScreen")

    rows = [t.text for t in hmi.items()]
    assert rows[:3] == ["Cancel", "SEARCH, Type a name or address...", "TYPE AN ADDRESS, e.g. anna@example.com"], rows[:3]
    # Recent mail first: Anna, who's also in the address book.
    assert rows[3] == "Anna Kowalski, anna@example.com · Recent · Personal", rows[3]
    assert any(r.endswith("· Recent") and "Prairie Euro Garage" in r for r in rows), rows
    # Then the address book, by name; and how many more there are.
    assert "Alex Abbott, alex.abbott@club.example.org · Members" in rows, rows
    assert rows[-1].endswith("more: search to find them"), rows[-1]
    hmi.shot("picker")

    # Someone not in recent mail, by search.
    hmi.select(r"^SEARCH")
    hmi.type_text("adams")
    hmi.expect_text(r"^Choose Recipient · \"adams\"$|^TO: Choose Recipient · \"adams\"$")
    rows = [t.text for t in hmi.items()][3:]
    assert rows == ["Zoë Adams, zoe@example.co.uk · Personal"], rows
    hmi.shot("search")
    hmi.select(r"^Zoë Adams")
    hmi.wait_for_screen("MailComposeScreen")
    hmi.expect_text(r"^TO, Zoë Adams$")
