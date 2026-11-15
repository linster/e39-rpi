"""From a contact's e-mail address: Write Mail opens a new message to them."""

import pytest


@pytest.mark.fake_contacts
@pytest.mark.fake_mail
def test_write_mail(hmi):
    hmi.go("ContactsScreen")
    hmi.select(r"^☰$")
    hmi.select(r"^Personal")
    hmi.select(r"Anna Kowalski")
    hmi.wait_for_screen("ContactDetailsScreen")
    hmi.select(r"anna@example\.com$")
    hmi.wait_for(lambda s: s.side_pane_open, "the e-mail's actions")
    assert [t.text for t in hmi.items()] == ["Go Back", "Write Mail"]
    hmi.select(r"^Write Mail$")
    hmi.wait_for_screen("MailComposeScreen")
    hmi.expect_text(r"^New Message$")
    hmi.expect_text(r"^TO, Anna Kowalski$")
    hmi.shot("compose")
    # Back (nothing written): to the contact.
    hmi.select(r"^Cancel$")
    hmi.wait_for_screen("ContactDetailsScreen")
