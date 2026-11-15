"""Zoom from the map's menu: the scale shows, turns change it, a press ends zooming (the scale
goes away) and the menu comes back."""

from _map import open_map, open_menu, scale, wait_menu_closed


def test_zoom_after_moving_in_menu(hmi):
    open_map(hmi)
    assert scale(hmi) is None, "no scale on the bare map"
    open_menu(hmi)

    # Zoom is the first item: turn away and back so the knob moves while the menu has it
    # (the event that used to end the map's knob collector).
    hmi.right()
    hmi.left()
    hmi.select(r"^Zoom$")
    wait_menu_closed(hmi)
    assert scale(hmi) == "1.6 km", "Zoom shows the scale, from 1.6 km"

    # The press that chose Zoom also gave the map the knob back; the map mustn't take that
    # press too (it would end Zoom mode straight away).
    hmi.right()
    assert scale(hmi) == "3.2 km", "turning right zooms out"
    hmi.left(2)
    assert scale(hmi) == "800 m", "turning left zooms in"
    hmi.shot("zoomed-in")

    # A press ends zooming.
    s = hmi.press()
    assert not s.modal_menu_open
    assert scale(hmi) is None, "the scale goes away after zooming"

    open_menu(hmi)
