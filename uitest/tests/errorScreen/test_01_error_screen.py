"""The error screen (#78): a crash of the HMI window shows a dark error screen where the HMI window
was, instead of Compose's error dialog (which the knob can't close). After the countdown the HMI
window opens again on the root screen. A second crash soon after exits the HMI (code 1), for
systemd to start it again.

The probe's CrashHmi makes the HMI window throw.

Local target only, with an HMI of its own: the second crash exits it.
"""

import io
import os
import time

import pytest
from PIL import Image

from e39ui.hmi import Hmi
from e39ui.targets.local import LocalTarget

ERROR_SCREEN = "E39 HMI Error"
SECONDS = 3


@pytest.fixture
def own_hmi(pytestconfig, out_dir):
    if pytestconfig.getoption("--target") != "local":
        pytest.skip("local target only: the test exits the HMI")
    target = LocalTarget(os.path.join(out_dir, "error-screen-hmi"), jar=pytestconfig.getoption("--jar"),
                         window_manager_config={"errorScreen": "true", "errorScreenSeconds": SECONDS})
    target.start()
    hmi = Hmi(target.address)
    try:
        target.wait_ready(hmi)
        hmi.wait_idle(timeout_ms=60000)
        hmi.start_test(os.path.join(out_dir, "errorScreen_test_01_error_screen"))
        yield target, hmi
    finally:
        hmi.close()
        target.stop()


def error_screen_text(hmi, timeout_s=10):
    """The error screen's texts, once it shows."""
    deadline = time.time() + timeout_s
    text = ""
    while True:
        try:
            text = hmi.window_text(ERROR_SCREEN)
            if "The HMI crashed" in text:
                return text
        except Exception:
            if time.time() > deadline:
                raise
        if time.time() > deadline:
            raise AssertionError(f"the error screen shows: {text!r}")
        time.sleep(0.2)


def size_of(png):
    return Image.open(io.BytesIO(png)).size


def test_error_screen_then_reopen_then_exit(own_hmi):
    target, hmi = own_hmi
    hmi.go_root()
    hmi.wait_idle()
    root = hmi.state().screen
    hmi_size = size_of(hmi.screenshot_png())

    # Somewhere other than the root screen, to see the reopening go back there.
    hmi.go("ThemeSelectorScreen")
    hmi.wait_for_screen("ThemeSelectorScreen")

    hmi.crash_hmi()

    text = error_screen_text(hmi)
    assert "Back to the main menu in" in text, text
    assert "Crash the HMI (HMI Navigator Debug)" in text, text
    assert not hmi.state().hmi_window_open
    # Where the HMI window was, at its size.
    assert size_of(hmi.screenshot_png(ERROR_SCREEN)) == hmi_size
    hmi.shot("error-screen", window=ERROR_SCREEN)

    # The countdown, then the HMI window again, on the root screen.
    hmi.wait_for(lambda s: s.hmi_window_open and s.screen == root,
                 f"the HMI window open again on {root}", timeout_s=SECONDS + 15)
    hmi.wait_idle(from_now=True)
    hmi.shot("reopened")

    # Again, soon after: the HMI exits for systemd to start it again.
    hmi.crash_hmi()
    text = error_screen_text(hmi)
    assert "The HMI crashed again" in text, text
    assert "Restarting the HMI app in" in text, text
    hmi.shot("crashed-again", window=ERROR_SCREEN)

    process = target.procs[-1]
    assert process.wait(SECONDS + 15) == 1
