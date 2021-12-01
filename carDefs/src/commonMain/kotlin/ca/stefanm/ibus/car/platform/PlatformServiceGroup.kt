package ca.stefanm.ibus.car.platform

data class PlatformServiceGroup(
    val name : String,
    val description: String,
    val children : List<PlatformService>
)