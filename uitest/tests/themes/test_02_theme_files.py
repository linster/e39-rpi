"""Theme files (#65): a theme file in the configuration folder's themes/ shows up in the HMI's
Theme Selector in both sizes, without a restart, and choosing it shows its colours. A change to
the file shows at once; a broken file is skipped (BMW Blue meanwhile) and the choice comes back
when it's fixed.

Local target only: the test writes into the HMI's (scratch) configuration folder.
"""

import io
import os

import pytest
from PIL import Image

NAME = "UitestPurple"
PURPLE = "#522964FF"
DARKER = "#3A1D47FF"


def theme_file(background):
    return f"""name = "{NAME}"
friendlyName = "Uitest Purple"
colors {{
  menuBackground = "{background}"
}}
"""


def share_of(png, hex_colour):
    """How much of a screenshot is `hex_colour` ("#RRGGBBAA"), within 2 per channel."""
    want = tuple(int(hex_colour[i:i + 2], 16) for i in (1, 3, 5))
    pixels = list(Image.open(io.BytesIO(png)).convert("RGB").getdata())
    return sum(1 for p in pixels if all(abs(a - b) <= 2 for a, b in zip(p, want))) / len(pixels)


def wait_theme(hmi, name, background):
    hmi.wait_for(lambda s: s.theme == name and s.theme_menu_background == background,
                 f"the HMI showing {name} with background {background}")
    hmi.wait_idle(from_now=True)
    share = share_of(hmi.screenshot_png(), background)
    assert share > 0.2, f"the HMI window is {share:.0%} {background}"


def test_theme_file_in_the_theme_selector(hmi, target):
    config_dir = getattr(target, "config_dir", None)
    if config_dir is None:
        pytest.skip("needs the local target (writes into the HMI's configuration folder)")
    themes = os.path.join(config_dir, "themes")
    os.makedirs(themes, exist_ok=True)
    path = os.path.join(themes, f"{NAME}.conf")
    original = hmi.state().theme

    try:
        with open(path, "w") as f:
            f.write(theme_file(PURPLE))

        # Both sizes, in the HMI's Theme Selector (the folder is watched: no restart).
        def listed():
            hmi.go("ThemeSelectorScreen")
            hmi.wait_idle()
            return [t.text for t in hmi.items()]
        names = listed()
        for _ in range(20):
            if "Uitest Purple (2X)" in names:
                break
            hmi.go_root()
            names = listed()
        assert "Uitest Purple (2X)" in names and "Uitest Purple" in names, names
        hmi.shot("theme-selector")

        hmi.select(r"^Uitest Purple \(2X\)$")
        wait_theme(hmi, f"{NAME}DoubledPixels", PURPLE)
        hmi.shot("uitest-purple")

        # The file changed: the HMI follows.
        with open(path, "w") as f:
            f.write(theme_file(DARKER))
        wait_theme(hmi, f"{NAME}DoubledPixels", DARKER)

        # Broken: skipped, BMW Blue meanwhile; the choice is kept.
        with open(path, "w") as f:
            f.write("this { isn't")
        hmi.wait_for(lambda s: s.theme == "BmwBlueDoubledPixels", "BMW Blue while the file is broken")

        # Fixed: back.
        with open(path, "w") as f:
            f.write(theme_file(PURPLE))
        wait_theme(hmi, f"{NAME}DoubledPixels", PURPLE)
    finally:
        if os.path.exists(path):
            os.remove(path)
        hmi.go("ThemeSelectorScreen")
        hmi.wait_idle()
        hmi.select(r"^BMW Blue \(2X\)$")
    hmi.wait_for(lambda s: s.theme == original, f"the theme back to {original}")
