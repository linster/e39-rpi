"""The contact list pages 100 at a time and keeps its place while a contact is open."""

import pytest


@pytest.mark.fake_contacts
def test_contact_paging(hmi):
    hmi.go("ContactsScreen")
    hmi.select(r"^▶$")
    hmi.expect_text(r"· 101–200 of 259$")
    hmi.select(r"^▶$")
    hmi.expect_text(r"· 201–259 of 259$")
    # The last page: ▶ does nothing.
    hmi.select(r"^▶$")
    hmi.expect_text(r"· 201–259 of 259$")
    hmi.shot("last-page")

    hmi.select(r"Zimmer|Young|Xu")
    hmi.wait_for_screen("ContactDetailsScreen")
    hmi.back()
    hmi.wait_for_screen("ContactsScreen")
    hmi.expect_text(r"· 201–259 of 259$")
    hmi.select(r"^◀$")
    hmi.expect_text(r"· 101–200 of 259$")
