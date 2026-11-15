"""The knob's item stays highlighted when a SmoothScroll screen changes while it's shown
(a notification, new counts, a toggled label).

Known to fail: SmoothScroll numbers its rows from the highest knob ID allocated so far,
re-read at every layout, so a second layout recreates every row; AllocatedIdManager.removeId
then leaves the knob on an ID that's gone (TODO.md, "KnobListenerService"). Strict: once
fixed, this passes unexpectedly and fails the run; remove the xfail then.
"""

import pytest


@pytest.mark.fake_mail
@pytest.mark.xfail(strict=True, reason="SmoothScroll relayout loses the knob's item (TODO.md)")
def test_highlight_survives_new_mail(hmi):
    hmi.go("MailScreen")
    hmi.right(2)
    before = hmi.selected()
    assert before is not None
    hmi.deliver_mail(subject="While you look")
    hmi.wait_for_notification(r"^New mail from")
    assert hmi.selected() is not None, "no item highlighted after new mail arrived"


@pytest.mark.fake_mail
def test_highlight_survives_toggle(hmi):
    # Passes thanks to the mail screens' workaround (key() on the rows: a
    # changed label rebuilds the list, the knob back on its first row).
    hmi.go("MailSettingsScreen")
    hmi.select(r"Notify About New Mail$")
    assert hmi.selected() is not None, "no item highlighted after toggling a setting"
