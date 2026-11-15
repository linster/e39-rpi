"""The reader opens with the knob scrolling the text; a press gives the knob to the toolbar;
leaving gives it back to the main listener."""

import pytest

LONG = "\n".join(f"Line {n}: the quick brown fox jumps over the lazy dog." for n in range(1, 61))


def body_y(hmi):
    return next(t.y for t in hmi.texts() if t.text.startswith("Line 1:"))


@pytest.mark.fake_mail
def test_scroll_mode(hmi):
    hmi.deliver_mail(subject="A long message", text=LONG)
    hmi.wait_for_notification(r"^New mail from")
    hmi.go("MailScreen")
    hmi.select(r"Inbox, 3$")
    hmi.wait_for_screen("MessageListScreen")
    hmi.select(r"A long message")
    s = hmi.wait_for_screen("MessageReaderScreen")
    assert s.main_aux_knob_listener and not s.main_knob_listener, "the reader opens scrolling"
    hmi.expect_text(r"^↕ Press: menu$")
    top = body_y(hmi)
    hmi.shot("top")

    hmi.right(5)
    scrolled = body_y(hmi)
    assert scrolled < top, f"turning right scrolls down ({top} -> {scrolled})"
    hmi.next_track()
    assert body_y(hmi) < scrolled, "next track pages down"
    hmi.shot("scrolled")
    hmi.left(3)
    hmi.prev_track()

    # A press: the knob moves to the toolbar.
    s = hmi.press()
    assert s.main_knob_listener and not s.main_aux_knob_listener
    hmi.expect_text(r"^↕ Scroll$")
    assert hmi.selected() is not None
    hmi.shot("toolbar")

    # And back to scrolling.
    s = hmi.select(r"^↕ Scroll$")
    assert s.main_aux_knob_listener and not s.main_knob_listener

    # Leaving while scrolling gives the knob back to the main listener.
    hmi.back()
    s = hmi.wait_for_screen("MessageListScreen")
    assert s.main_knob_listener and not s.main_aux_knob_listener
    hmi.right()
    assert hmi.selected() is not None, "the list takes the knob again"
