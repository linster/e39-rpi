"""After picking an item from the map's menu, the knob still works on the map and a press
opens the menu again.

The bug: KnobListenerService gated events with takeWhile(listenerEnabled), so the map's first
knob event while its menu was open (main listener disabled) ended the map's knob collector for
good ("knobTurnEvents() flow completed. cause: null" in the log). After choosing Pan the map
ignored the knob: no panning, no way out of Pan, and the menu never came back.
"""

from _map import open_map, open_menu, wait_menu_closed


def test_menu_reopens_after_pan(hmi):
    open_map(hmi)
    open_menu(hmi)
    hmi.shot("menu")

    # "Pan ⬌" is the second item: getting there turns the knob while the menu has it,
    # which is what ended the map's collector.
    hmi.select(r"^Pan ⬌$")
    wait_menu_closed(hmi)
    hmi.right(3)
    hmi.left()
    hmi.shot("panned")

    # A press leaves Pan mode, the next opens the menu again.
    s = hmi.press()
    assert not s.modal_menu_open, "leaving Pan doesn't open the menu yet"
    open_menu(hmi)
    hmi.shot("menu-again")


def test_menu_reopens_after_pan_up_down(hmi):
    open_map(hmi)
    open_menu(hmi)
    hmi.select(r"^Pan ⬍$")
    wait_menu_closed(hmi)
    hmi.left(2)
    hmi.press()
    open_menu(hmi)


def test_menu_reopens_many_times(hmi):
    open_map(hmi)
    for n in range(3):
        open_menu(hmi)
        hmi.select(r"^Pan ⬌$" if n % 2 == 0 else r"^Pan ⬍$")
        wait_menu_closed(hmi)
        hmi.right()
        hmi.press()
    open_menu(hmi)
