"""Leaving the map from its menu and coming back: the knob works on the map again."""

from _map import open_map, open_menu


def test_main_menu_then_back_to_map(hmi):
    open_map(hmi)
    open_menu(hmi)
    hmi.select(r"^Main Menu$")
    s = hmi.wait_for(lambda s: s.screen != "MapScreen" and not s.modal_menu_open, "the main menu")
    assert s.main_knob_listener, "the main menu gets the knob"
    hmi.wait_idle()
    hmi.right()
    assert hmi.selected() is not None, "the main menu takes the knob"

    open_map(hmi)
    open_menu(hmi)
