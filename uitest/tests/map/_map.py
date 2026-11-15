"""Helpers for the map screen's tests."""

MENU_ITEMS = [r"^Zoom$", r"^Pan ⬌$", r"^Pan ⬍$", r"^Guidance$", r"^POIs$", r"^Main Menu$"]

# MapScale's display strings, smallest to largest (the knob's zoom order).
SCALES = ["25 m", "50 m", "100 m", "200 m", "400 m", "800 m", "1.6 km", "3.2 km", "7 km",
          "25 km", "50 km", "100 km", "200 km"]


def open_map(hmi):
    """The map in browsing mode, no overlay, the main knob listener taking the knob."""
    s = hmi.go("MapScreen")
    assert not s.modal_menu_open, "the map opens without its menu"
    assert s.main_knob_listener, "the map listens to the knob"
    return s


def open_menu(hmi):
    """A press on the bare map opens its view menu (Zoom, Pan, ...)."""
    hmi.press()
    s = hmi.wait_for(lambda s: s.modal_menu_open, "the map's menu to open")
    for item in MENU_ITEMS:
        hmi.expect_text(item)
    assert not s.main_knob_listener, "the menu takes the knob from the map"
    return s


def wait_menu_closed(hmi):
    s = hmi.wait_for(lambda s: not s.modal_menu_open, "the map's menu to close")
    hmi.wait_idle()
    assert s.main_knob_listener, "the map gets the knob back"
    return hmi.state()


def scale(hmi):
    """The scale shown on the map (e.g. "1.6 km"; only in Zoom mode), or None."""
    found = [t.text for t in hmi.texts() if t.text in SCALES]
    assert len(found) <= 1, f"expected at most one scale label, got {found}"
    return found[0] if found else None
