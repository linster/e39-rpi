"""Mail settings: notification toggles and muted accounts."""

import pytest


@pytest.mark.fake_mail
def test_settings(hmi):
    hmi.go("MailScreen")
    hmi.select(r"Mail Settings$")
    hmi.wait_for_screen("MailSettingsScreen")
    hmi.expect_text(r"\[X\] Notify About New Mail$")
    hmi.expect_text(r"\[X\] Notify When Mail Is Sent or Fails$")
    hmi.expect_text(r"^Quick Replies\.\.\. \(6\)$")
    hmi.shot("settings")

    hmi.select(r"Notify About New Mail$")
    hmi.expect_text(r"\[ \] Notify About New Mail$")
    # The knob stays on the row it toggled.
    # The knob stays on the row it toggled.
    assert hmi.selected() == "[ ] Notify About New Mail", hmi.selected()
    hmi.select(r"Notify About New Mail$")
    hmi.expect_text(r"\[X\] Notify About New Mail$")

    hmi.select(r"^Muted Accounts\.\.\.$")
    hmi.wait_for_screen("MailMuteScreen")
    hmi.expect_text(r"\[X\] Home$")
    hmi.expect_text(r"\[X\] Work$")
    hmi.select(r"Work$")
    hmi.expect_text(r"\[ \] Work$")
    hmi.shot("muted")
    hmi.select(r"^Go Back$")
    hmi.wait_for_screen("MailSettingsScreen")
    hmi.expect_text(r"^Muted Accounts\.\.\. \(1\)$")
