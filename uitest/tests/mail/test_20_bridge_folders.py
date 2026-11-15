"""Against the PIM bridge (--mail bridge): its accounts and folders, and the main menu's count.

Expects the dev bridge's "Local Folders" (e39-rpi-pim scripts/run-dev.sh) with the
"e39-list" folder from uitest/tools/make-bridge-testmail.py.
"""

import re

import pytest


@pytest.mark.bridge_mail
def test_bridge_folders(hmi):
    hmi.go("MailScreen")
    names = [t.text for t in hmi.items()]
    inbox = next((n for n in names if re.match(r"^LOCAL FOLDERS, (\d+) unread, 📥 inbox", n)), None)
    assert inbox, names
    assert any(re.match(r"^📁 e39-list(, \d+)?$", n) for n in names), "e39-list (make-bridge-testmail.py)"
    assert "📤 sent-mail" in names, names
    hmi.shot("folders")

    # The main menu counts the inbox's unread mail.
    m = re.search(r"inbox(?:, (\d+))?$", inbox)
    unread = int(m.group(1) or 0)
    hmi.go("BMWMainMenu")
    hmi.expect_text(rf"^Mail \({unread}\)$" if unread else r"^Mail$")
