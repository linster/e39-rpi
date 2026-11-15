"""A new contact: names and a number typed (the telephone keyboard), saved, then shown."""

import pytest


@pytest.mark.fake_contacts
def test_new_contact(hmi):
    hmi.go("ContactsScreen")
    hmi.select(r"^☰$")
    hmi.select(r"^New Contact$")
    hmi.wait_for_screen("ContactEditScreen")
    hmi.expect_text(r"^New Contact$")
    # More than one writable address book: which one.
    hmi.expect_text(r"^ADDRESS BOOK, Personal \(Home server\)$")

    hmi.select(r"^GIVEN NAME")
    hmi.type_text("Tow")
    hmi.select(r"^FAMILY NAME")
    hmi.type_text("Truck")
    hmi.select(r"^Add Phone Number\.\.\.$")
    hmi.wait_for(lambda s: s.keyboard_open, "the keyboard")
    hmi.type_text("3065550144")
    hmi.expect_text(r"^MOBILE ★, 3065550144$")
    hmi.shot("ready")

    hmi.select(r"^Save$")
    hmi.wait_for_screen("ContactDetailsScreen")
    hmi.wait_for_notification(r"^Contact saved$")
    hmi.expect_text(r"^Tow Truck$")
    hmi.expect_text(r"^MOBILE ★, 3065550144$")
    hmi.shot("saved")

    # In the list.
    hmi.back()
    hmi.wait_for_screen("ContactsScreen")
    hmi.select(r"^A–Z$")
    hmi.select(r"^T · ")
    hmi.expect_text(r"Tow Truck, 3065550144$")
