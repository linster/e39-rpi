"""Map Settings -> GPS: the LocationRepository's position, source and state, and gpsd's host and port.

The position comes from set_position (the probe -> FakeLocationService -> the LocationRepository),
as a location service's would. The runner's HMI doesn't start GpsdService, and its configuration is
a scratch folder, so changing gpsd's port here doesn't touch the laptop's config.
"""

import re


def open_gps_settings(hmi):
    hmi.go("MapSettingsMainScreen")
    hmi.select(r"^GPS$")
    hmi.wait_for_screen("GpsSettingsScreen")
    hmi.wait_idle()


def test_shows_the_position(hmi):
    open_gps_settings(hmi)
    hmi.shot("opened")

    # A position, as a location service would report it.
    hmi.set_position(45.3470, -75.9190)
    hmi.wait_idle(from_now=True)
    hmi.expect_text(r"^Source: FakeLocationService$")
    hmi.expect_text(r"^Status: Fix \(3D\)$")
    hmi.expect_text(r"^Position: 45\.34700, -75\.91900$")
    hmi.expect_text(r"^Last fix: \d\d:\d\d:\d\d UTC$")
    hmi.expect_text(r"^Satellites: unknown$")
    hmi.expect_text(r"^gpsd: localhost:2947$")
    hmi.shot("fix")

    # It follows new positions while open.
    hmi.set_position(45.3500, -75.9200)
    hmi.wait_idle(from_now=True)
    hmi.expect_text(r"^Position: 45\.35000, -75\.92000$")


def test_set_gpsd_host_and_reset(hmi):
    # The host, not the port: the numeric keyboard (Set gpsd Port...) is cut off below the bottom
    # bar at 800x468 (no 0 key, Return half hidden), so the knob can't use it.
    open_gps_settings(hmi)
    hmi.expect_text(r"^gpsd: localhost:2947$")

    hmi.select(r"^Set gpsd Host\.\.\.$")
    hmi.wait_for(lambda s: s.keyboard_open, "the keyboard")
    hmi.clear_text(len("localhost"))
    hmi.type_text("gpsbox")
    hmi.expect_text(r"^gpsd: gpsbox:2947$")
    hmi.shot("host-gpsbox")

    # Not a host: refused, unchanged.
    hmi.select(r"^Set gpsd Host\.\.\.$")
    hmi.wait_for(lambda s: s.keyboard_open, "the keyboard")
    hmi.clear_text(len("gpsbox"))
    hmi.type_text("gps box")
    hmi.wait_for_notification(r"^Not changed\.$")
    hmi.expect_text(r"^gpsd: gpsbox:2947$")

    hmi.select(r"^Reset to localhost:2947$")
    hmi.wait_idle(from_now=True)
    hmi.expect_text(r"^gpsd: localhost:2947$")
