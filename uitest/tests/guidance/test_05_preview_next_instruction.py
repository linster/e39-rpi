""""Preview next instruction" (InGuidanceSubScreen) shows the coming maneuver in the side panel, titled
"Next", until guidance's next update: the current maneuver trumps the preview.

The side panel holds no state of its own: it shows what it was last handed, a preview or guidance's
coming instruction. Between maneuvers guidance sends nothing, so a preview stays; approaching a
maneuver, every GPS fix redraws guidance's view (in the side panel if the setting allows it there,
else nothing); leaving the screen gives the panel back to guidance, without closing a real
instruction. The car is moved with the probe's SetPosition, as GPS would.
"""

import re

import pytest

from _guidance import Route, clear_notifications, drive, fresh, ll, meters, panel_titles, set_up_route

AT_METERS = 35.0  # GuidanceInstructionTracker.AT_METERS: within this the car is "at" a maneuver.


def start_guidance(hmi):
    """A route set up and calculated in the HMI, guidance started: on InGuidanceSubScreen."""
    fresh(hmi)
    set_up_route(hmi, pan_right=12)
    hmi.select(r"^Start Guidance$")
    hmi.wait_for_screen("InGuidanceSubScreen")
    g = hmi.guidance()
    assert len(g.instruction_list) >= 3, f"route too short: {len(g.instruction_list)} instructions"
    return g, Route(g)


def pick_maneuver(g, route):
    """A maneuver between the start and the destination with a stretch of road before its trigger
    radius (so the car can be between maneuvers) and a radius big enough to be inside it without
    being at it."""
    items = g.instruction_list
    for k in range(1, len(items) - 1):
        target, prev = items[k], items[k - 1]
        if target.path_index < 0 or prev.path_index < 0 or target.trigger_radius_meters < 120:
            continue
        if route.last_outside(target) > route.meters_at(prev) + 150:
            return k
    pytest.skip("no maneuver on this route with a stretch before it and a radius of 120 m or more")


def inside(route, instruction):
    """A distance along the route inside the maneuver's trigger radius, well short of being at it."""
    d = route.last_outside(instruction)
    while meters(route.at(d), ll(instruction.location)) > instruction.trigger_radius_meters * 0.7:
        d += 5.0
    assert meters(route.at(d), ll(instruction.location)) > AT_METERS + 30
    return d


def preview(hmi):
    clear_notifications(hmi)
    hmi.select(r"^Preview next instruction$")
    hmi.wait_idle()


def expect_preview_of(hmi, instruction):
    hmi.wait_for(lambda s: s.side_pane_open, "the side panel")
    hmi.wait_idle()
    titles = panel_titles(hmi)
    want = r"Next: Destination" if instruction.kind.startswith("DESTINATION") else rf"Next\s+{instruction.index + 1}/\d+"
    assert len(titles) == 1 and re.fullmatch(want, titles[0], re.I), titles
    hmi.expect_text(re.escape(instruction.text))


def expect_guidance_panel_of(hmi, instruction):
    hmi.wait_idle()
    titles = panel_titles(hmi)
    assert len(titles) == 1 and re.fullmatch(rf"Guidance\s+{instruction.index + 1}/\d+", titles[0], re.I), titles
    hmi.expect_text(re.escape(instruction.text))


def set_side_panel(hmi, label):
    """Map Settings -> Guidance Instructions: "Side panel: <label>" (always, over the map, never)."""
    hmi.go("GuidanceInstructionSettingsScreen")
    hmi.select(rf"Side panel: {label}$")
    hmi.wait_idle()
    hmi.expect_text(rf"^✓ Side panel: {label}$")


@pytest.mark.maps_backend
def test_preview_side_panel_over_the_map(hmi):
    """The default setting: guidance's side panel only over the map, so on this screen guidance's
    view is "nothing" (instructions come as notifications)."""
    g, route = start_guidance(hmi)

    # No position yet: the coming maneuver is the first.
    preview(hmi)
    expect_preview_of(hmi, g.instruction_list[0])
    hmi.shot("preview-first")

    # Drive to just outside a maneuver's radius: everything before it passed, nothing shown.
    k = pick_maneuver(g, route)
    target = g.instruction_list[k]
    before = route.last_outside(target)
    g = drive(hmi, route, 0, before)
    assert (g.next_index, g.shown_index) == (k, -1), (g.next_index, g.shown_index)
    clear_notifications(hmi)
    hmi.wait_for(lambda s: not s.side_pane_open, "no side panel between maneuvers")

    preview(hmi)
    expect_preview_of(hmi, target)
    hmi.shot("preview-between-maneuvers")

    # Still between maneuvers: guidance sends nothing, the preview stays.
    g = drive(hmi, route, before, before + 20)
    assert g.shown_index == -1
    expect_preview_of(hmi, target)

    # Into the maneuver's radius: the maneuver trumps the preview; here that's a notification.
    d = inside(route, target)
    g = drive(hmi, route, before + 20, d)
    assert g.shown_index == k, g.shown_index
    hmi.wait_for(lambda s: not s.side_pane_open, "the preview to give way to the maneuver")
    hmi.wait_for(lambda s: s.notification.visible and target.text in s.notification.content_text,
                 "the maneuver's notification")
    hmi.shot("maneuver-trumps-preview")

    # Previewing while approaching shows the same maneuver, until the next GPS fix.
    preview(hmi)
    expect_preview_of(hmi, target)
    hmi.set_position(*route.at(d + 20))
    hmi.wait_for(lambda s: not s.side_pane_open, "the next fix to replace the preview")


@pytest.mark.maps_backend
def test_preview_side_panel_always(hmi):
    """Side panel always: guidance's view on this screen is the maneuver in the side panel."""
    set_side_panel(hmi, "always")
    try:
        g, route = start_guidance(hmi)
        k = pick_maneuver(g, route)
        target = g.instruction_list[k]
        before = route.last_outside(target)
        drive(hmi, route, 0, before)
        clear_notifications(hmi)

        preview(hmi)
        expect_preview_of(hmi, target)

        # Into the radius: guidance's own panel for the maneuver replaces the preview.
        d = inside(route, target)
        g = drive(hmi, route, before, d)
        assert g.shown_index == k
        expect_guidance_panel_of(hmi, target)
        hmi.shot("guidance-panel")

        # A preview while approaching lasts until the next fix.
        preview(hmi)
        expect_preview_of(hmi, target)
        hmi.set_position(*route.at(d + 20))
        expect_guidance_panel_of(hmi, target)

        # Leaving the screen removes a preview, never guidance's instruction.
        preview(hmi)
        expect_preview_of(hmi, target)
        clear_notifications(hmi)
        hmi.select(r"^Go Back$")
        hmi.wait_for_screen("MapScreen")
        expect_guidance_panel_of(hmi, target)
        hmi.shot("map-after-leaving")

        # Past the maneuver: its panel goes.
        g = drive(hmi, route, d + 20, route.meters_at(target) + 80)
        assert g.next_index > k, g.next_index
        hmi.wait_idle()
        assert not any(re.fullmatch(rf"(Next|Guidance)\s+{target.index + 1}/\d+", t, re.I) for t in panel_titles(hmi)), \
            panel_titles(hmi)
    finally:
        set_side_panel(hmi, "over the map")


@pytest.mark.maps_backend
def test_preview_at_destination(hmi):
    """Past the last instruction there's nothing to preview."""
    g, route = start_guidance(hmi)
    g = drive(hmi, route, 0, route.length, step_m=25.0)
    assert g.next_index == len(g.instruction_list), (g.next_index, len(g.instruction_list))
    clear_notifications(hmi)
    hmi.select(r"^Preview next instruction$")
    hmi.wait_for_notification(r"^No more instructions$")
