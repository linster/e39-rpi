"""From a message: Save Sender as Contact fills in the name and address."""

import pytest


@pytest.mark.fake_contacts
@pytest.mark.fake_mail
def test_save_sender(hmi):
    hmi.go("MailScreen")
    hmi.select(r"Car club, 1$")
    hmi.wait_for_screen("MessageListScreen")
    hmi.select(r"Spring meet in Regina")
    hmi.wait_for_screen("MessageReaderScreen")
    hmi.press()
    hmi.select(r"^☰$")
    hmi.select(r"^Save Sender as Contact$")
    hmi.wait_for_screen("ContactEditScreen")
    hmi.expect_text(r"^GIVEN NAME, Saskatchewan BMW$")
    hmi.expect_text(r"^FAMILY NAME, Club$")
    hmi.expect_text(r"^E-MAIL ★, news@skbmwclub\.example\.com$")
    hmi.shot("from-mail")
    hmi.select(r"^Save$")
    hmi.wait_for_screen("ContactDetailsScreen")
    hmi.wait_for_notification(r"^Contact saved$")
    hmi.expect_text(r"^Saskatchewan BMW Club$")
