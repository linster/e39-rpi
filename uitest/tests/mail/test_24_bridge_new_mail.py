"""Against the PIM bridge: a message arriving in its inbox (a file in its maildir, as a
mail server's delivery would be) shows a notification and counts. Needs --bridge-maildir.

The bridge waits until a folder has been quiet for 30 s before reporting new mail."""

import email.utils
import os
import time

import pytest


@pytest.mark.bridge_mail
def test_bridge_new_mail(hmi, bridge_maildir):
    hmi.go("MailScreen")
    before = next(t.text for t in hmi.items() if "📥 inbox" in t.text)
    name = f"{int(time.time())}.uitest-newmail.e39"
    path = os.path.join(bridge_maildir, "inbox", "new", name)
    with open(path, "w") as f:
        f.write("From: Anna Kowalski <anna@example.com>\n"
                "To: Stefan <stefan@example.com>\n"
                "Subject: Are you close? (UI test)\n"
                f"Date: {email.utils.formatdate(localtime=True)}\n"
                f"Message-ID: <{name}@uitest>\n\n"
                "We're at the restaurant already.\n")
    try:
        s = hmi.wait_for_notification(r"^New mail from Anna Kowalski$", timeout_s=90)
        assert s.notification.content_text == "Are you close? (UI test)"
        hmi.shot("notification")
        hmi.wait_for(lambda s: any("📥 inbox" in t.text and t.text != before for t in hmi.items()),
                     "the inbox's count to change")
    finally:
        # The maildir resource may have moved it to cur/ by now.
        for sub in ("new", "cur"):
            for f in os.listdir(os.path.join(bridge_maildir, "inbox", sub)):
                if f.startswith(name):
                    os.remove(os.path.join(bridge_maildir, "inbox", sub, f))
