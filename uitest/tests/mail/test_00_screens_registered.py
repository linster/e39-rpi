import pytest

"""Every screen is registered with the navigator (as the HMI Navigator Debug window checks),
including the mail app's."""

MAIL_SCREENS = [
    "ca.stefanm.ibus.gui.pim.email.views.MailScreen",
    "ca.stefanm.ibus.gui.pim.email.views.MessageListScreen",
    "ca.stefanm.ibus.gui.pim.email.views.MessageReaderScreen",
    "ca.stefanm.ibus.gui.pim.email.views.MailComposeScreen",
    "ca.stefanm.ibus.gui.pim.contacts.picker.ContactPickerScreen",
    "ca.stefanm.ibus.gui.pim.email.views.settings.MailSettingsScreen",
    "ca.stefanm.ibus.gui.pim.email.views.settings.MailMuteScreen",
    "ca.stefanm.ibus.gui.pim.email.views.settings.QuickRepliesScreen",
]


def test_screens_registered(hmi):
    registered, unregistered = hmi.list_screens()
    assert unregistered == [], f"@AutoDiscover screens missing from Navigator.kt: {unregistered}"
    missing = [s for s in MAIL_SCREENS if s not in registered]
    assert missing == [], missing


@pytest.mark.fake_mail
def test_back_stack(hmi):
    hmi.go("MailScreen")
    hmi.select(r"E39 list")
    hmi.wait_for_screen("MessageListScreen")
    s = hmi.state()
    assert "folderId=6" in s.screen_parameters, s.screen_parameters
    assert [e.screen for e in s.back_stack_entries][:1] == ["MailScreen"], s.back_stack_entries
