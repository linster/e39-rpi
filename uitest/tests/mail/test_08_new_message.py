"""Writing a new message: a recipient from the contact picker, a typed subject, a quick reply, send."""

import pytest


@pytest.mark.fake_mail
def test_new_message(hmi):
    hmi.go("MailScreen")
    hmi.select(r"^✉ New Message$")
    hmi.wait_for_screen("MailComposeScreen")
    hmi.expect_text(r"^New Message$")

    hmi.select(r"^TO, Add")
    hmi.wait_for(lambda s: s.side_pane_open, "the recipients")
    hmi.select(r"^Add from Contacts\.\.\.$")
    hmi.wait_for_screen("ContactPickerScreen")
    hmi.shot("picker")
    hmi.select(r"^Anna Kowalski, anna@example\.com · Recent")  # (not "Type an Address, e.g. anna@...")
    hmi.wait_for_screen("MailComposeScreen")
    hmi.expect_text(r"^TO, Anna Kowalski$")

    hmi.select(r"^SUBJECT, ")
    hmi.wait_for(lambda s: s.keyboard_open, "the keyboard")
    hmi.type_text("Hi Anna")
    hmi.expect_text(r"^SUBJECT, Hi Anna$")

    hmi.select(r"^Quick Reply\.\.\.$")
    hmi.wait_for(lambda s: s.side_pane_open, "the quick replies")
    hmi.select(r"^On my way\.$")
    hmi.expect_text(r"^MESSAGE, On my way\.$")
    hmi.shot("ready")

    hmi.select(r"^Send$")
    hmi.wait_for_screen("MailScreen")
    hmi.wait_for_notification(r"^Mail sent$")

    hmi.select(r"📤 Sent$")
    hmi.wait_for_screen("MessageListScreen")
    hmi.expect_text(r"^To: Anna Kowalski, .*, Hi Anna$")
