"""Against the PIM bridge: sending. The dev bridge has no sending account, so it refuses:
the HMI says so and keeps the draft."""

import pytest


@pytest.mark.bridge_mail
def test_bridge_send_refused(hmi):
    hmi.go("MailScreen")
    hmi.select(r"^✉ New Message$")
    hmi.wait_for_screen("MailComposeScreen")

    # The contact picker's stub offers people from the bridge's mail.
    hmi.select(r"^TO, Add")
    hmi.select(r"^Add from Contacts\.\.\.$")
    hmi.wait_for_screen("ContactPickerScreen")
    hmi.shot("picker")
    hmi.select(r"^Anna\b")
    hmi.wait_for_screen("MailComposeScreen")
    hmi.expect_text(r"^TO, Anna")

    hmi.select(r"^Quick Reply\.\.\.$")
    hmi.select(r"^On my way\.$")
    hmi.select(r"^Send$")
    s = hmi.wait_for_notification(r"^Not sent$")
    hmi.expect_screen("MailComposeScreen")
    hmi.expect_text(r"^MESSAGE, On my way\.$")
    hmi.shot("not-sent")
    print("The bridge said:", s.notification.content_text)

    hmi.select(r"^Cancel$")
    hmi.select(r"^Throw Away$")
    hmi.wait_for_screen("MailScreen")
