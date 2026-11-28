"""The menu picks an address book (or all of them); the choice is kept."""

import pytest


@pytest.mark.fake_contacts
def test_address_books(hmi):
    hmi.go("ContactsScreen")
    hmi.select(r"^☰$")
    hmi.wait_for(lambda s: s.side_pane_open, "the menu")
    books = [t.text for t in hmi.items()]
    assert books == ["Go Back", "New Contact", "All Address Books · 260", "Personal (Home server) · 4", "Contacts (Google) · 5",
                     "Company (Work directory) · 1", "Members (Car club) · 250", "Accounts..."], books
    hmi.select(r"^Personal")
    hmi.expect_text(r"^Contacts · Personal · 1–4 of 4$")
    rows = [t.text for t in hmi.items()][5:]
    assert rows == ["A, Zoë Adams, +44 7700 900123", "K, Anna Kowalski, +1 306 555 0123",
                    "L, René Lévesque, Garage Lévesque", "P, Prairie Euro Garage, 555-0199"], rows
    hmi.shot("personal")

    # Kept when coming back from a contact.
    hmi.select(r"Anna Kowalski")
    hmi.wait_for_screen("ContactDetailsScreen")
    hmi.back()
    hmi.wait_for_screen("ContactsScreen")
    hmi.expect_text(r"^Contacts · Personal · 1–4 of 4$")

    hmi.select(r"^☰$")
    hmi.select(r"^All Address Books")
    hmi.expect_text(r"^Contacts · All · 1–100 of 260$")
