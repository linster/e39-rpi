package ca.stefanm.ibus.gui.map.mapScreen

sealed interface MapOverlayState {
    object NoOverlay : MapOverlayState
    object ModifyViewMenu : MapOverlayState
    object GuidanceMenu : MapOverlayState
    object PoiMenu : MapOverlayState
    object PanLeftRight : MapOverlayState
    object PanUpDown : MapOverlayState
    object ChangeZoom : MapOverlayState
}