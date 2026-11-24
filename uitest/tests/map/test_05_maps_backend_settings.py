"""Map Settings -> Maps Backend: setting the host.

Two bugs this checks:
- The keyboard opened with the host the screen first showed, not the current one (the menu keeps
  its first onClicked): after one change, clearing the current host's length left stray letters.
- A refused host threw on the UI thread instead of showing "Not changed." (the screen's apply {}
  helper, called inside Column { }, resolved to Kotlin's T.apply).

The host, not the port: the numeric keyboard is cut off below the bottom bar at 800x468.
The runner's configuration is a scratch folder; the test puts the original host back.
"""

import re


def server(hmi):
    line = hmi.expect_text(r"^Server: ")[0]
    host, port = re.match(r"^Server: (.*):(\d+)$", line).groups()
    return host, port


def set_host(hmi, current, new):
    hmi.select(r"^Set Host\.\.\.$")
    hmi.wait_for(lambda s: s.keyboard_open, "the keyboard")
    hmi.clear_text(len(current))
    hmi.type_text(new)


def test_set_host_twice_and_refused(hmi):
    hmi.go("MapsBackendSettingsScreen")
    hmi.wait_idle()
    original, port = server(hmi)

    try:
        set_host(hmi, original, "mapsbox")
        hmi.expect_text(rf"^Server: mapsbox:{port}$")

        # The keyboard opens with the current host ("mapsbox"), not the first one.
        set_host(hmi, "mapsbox", "mapsboxtwo")
        hmi.expect_text(rf"^Server: mapsboxtwo:{port}$")
        hmi.shot("host-changed")

        # Not a host: refused with a notification, unchanged.
        set_host(hmi, "mapsboxtwo", "maps box")
        hmi.wait_for_notification(r"^Not changed\.$")
        hmi.expect_text(rf"^Server: mapsboxtwo:{port}$")
    finally:
        host_now, _ = server(hmi)
        if host_now != original:
            set_host(hmi, host_now, original)
    hmi.expect_text(rf"^Server: {re.escape(original)}:{port}$")
