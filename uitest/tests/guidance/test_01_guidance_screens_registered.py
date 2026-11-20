"""The guidance screens are registered with the navigator."""

GUIDANCE_SCREENS = [
    "ca.stefanm.ibus.gui.map.guidance.setupScreens.GuidanceSetupScreen",
    "ca.stefanm.ibus.gui.map.guidance.setupScreens.SetupRouteSubScreen",
    "ca.stefanm.ibus.gui.map.guidance.setupScreens.InGuidanceSubScreen",
    "ca.stefanm.ibus.gui.map.guidance.setupScreens.RouteTerminatedSubScreen",
    "ca.stefanm.ibus.gui.map.poi.PoiSelectorScreen",
]


def test_guidance_screens_registered(hmi):
    registered, _ = hmi.list_screens()
    missing = [s for s in GUIDANCE_SCREENS if s not in registered]
    assert missing == [], missing
