"""Shared steps for "Navigate" from a contact's address (#59), with the sample geocoder."""

import re
import time


def open_contact(hmi, book, name):
    hmi.go("ContactsScreen")
    hmi.select(r"^☰$")
    hmi.select(book)
    hmi.select(name)
    hmi.wait_for_screen("ContactDetailsScreen")


def wait_text(hmi, pattern, timeout_s=10):
    """Waits for some text on screen matching `pattern` (the panes open one after another)."""
    deadline = time.time() + timeout_s
    while True:
        texts = [t.text for t in hmi.texts()]
        if any(re.search(pattern, t, re.I) for t in texts):
            return hmi.wait_idle(from_now=True)
        if time.time() > deadline:
            raise AssertionError(f"no text matching {pattern!r}; texts: {texts}")
        time.sleep(0.1)


def navigate(hmi, address_row):
    """The address row, then Navigate in its pane."""
    hmi.select(address_row)
    wait_text(hmi, r"^Navigate$")
    hmi.select(r"^Navigate$")


def near(point, lat, lng):
    return abs(point.latitude - lat) < 1e-6 and abs(point.longitude - lng) < 1e-6
