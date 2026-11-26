"""The Theme Designer debug window (#65): starting from a theme, the presets, and the live preview
on the HMI window, which ends when the box is unticked or the window closes. Nothing is saved:
the HMI's own theme is back afterwards.

The probe opens the window and clicks its buttons; the HMI window shows the theme being edited.
"""

import io

from PIL import Image

DESIGNER = "Theme Designer"
PREVIEW = "Show on the HMI window (not saved)"


def background(hmi):
    return hmi.state().theme_menu_background


def wait_background(hmi, hex_colour):
    hmi.wait_for(lambda s: s.theme_menu_background == hex_colour, f"the HMI's menu background {hex_colour}")
    hmi.wait_idle(from_now=True)


def share_of(png, hex_colour):
    """How much of a screenshot is `hex_colour` ("#RRGGBBAA"), within 2 per channel."""
    want = tuple(int(hex_colour[i:i + 2], 16) for i in (1, 3, 5))
    img = Image.open(io.BytesIO(png)).convert("RGB")
    pixels = list(img.getdata())
    near = sum(1 for p in pixels if all(abs(a - b) <= 2 for a, b in zip(p, want)))
    return near / len(pixels)


def assert_hmi_shows(hmi, hex_colour):
    share = share_of(hmi.screenshot_png(), hex_colour)
    assert share > 0.2, f"the HMI window is {share:.0%} {hex_colour}, the previewed menu background"


def test_theme_designer_previews_on_the_hmi(hmi):
    chosen = hmi.state().theme
    hmi.open_debug_window(DESIGNER)
    try:
        text = hmi.window_text(DESIGNER)
        for expected in ("Start from a theme", "Generate from a base colour (the menu background)",
                         "Light (BMW Blue)", "Dark (Goose Green)", "Colours", "menuBackground", PREVIEW):
            assert expected in text, f"{expected!r} in the designer"
        hmi.shot("designer", window=DESIGNER)

        # Not previewing yet: the HMI keeps its theme.
        assert hmi.state().theme == chosen
        assert share_of(hmi.screenshot_png(), "#005229FF") < 0.01, "no Goose Green on the HMI yet"

        hmi.click(DESIGNER, PREVIEW)
        hmi.wait_for(lambda s: s.theme == "ThemeDesignerPreview", "the preview on the HMI")

        # Starting from a theme shows its colours on the HMI.
        hmi.click(DESIGNER, "Goose Green")
        wait_background(hmi, "#005229FF")
        assert_hmi_shows(hmi, "#005229FF")
        hmi.shot("hmi-goose-green")

        hmi.click(DESIGNER, "Revolution Orange")
        wait_background(hmi, "#AD6500FF")
        assert_hmi_shows(hmi, "#AD6500FF")
        light = hmi.screenshot_png()

        # A preset regenerates the colours from the base (the background, which stays).
        hmi.click(DESIGNER, "Dark (Goose Green)")
        hmi.wait_idle(from_now=True)
        assert background(hmi) == "#AD6500FF", "the base colour is the background"
        dark = hmi.screenshot_png()
        assert_hmi_shows(hmi, "#AD6500FF")
        assert dark != light, "the dark preset changes the other colours on the HMI"
        hmi.shot("hmi-orange-dark-preset")
        hmi.shot("designer-orange-dark-preset", window=DESIGNER)

        # Unticking ends the preview.
        hmi.click(DESIGNER, PREVIEW)
        hmi.wait_for(lambda s: s.theme == chosen, "the HMI's own theme back")
        hmi.wait_idle(from_now=True)
        assert share_of(hmi.screenshot_png(), "#AD6500FF") < 0.01, "no orange on the HMI after the preview"

        # So does closing the window while previewing.
        hmi.click(DESIGNER, PREVIEW)
        hmi.wait_for(lambda s: s.theme == "ThemeDesignerPreview", "the preview again")
    finally:
        hmi.close_debug_window(DESIGNER)
    hmi.wait_for(lambda s: s.theme == chosen, "the HMI's own theme after closing the designer")
