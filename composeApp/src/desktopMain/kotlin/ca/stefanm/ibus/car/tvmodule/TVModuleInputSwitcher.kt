package ca.stefanm.ibus.car.tvmodule

import ca.stefanm.ibus.annotations.services.PlatformServiceInfo
import ca.stefanm.ibus.car.di.ConfiguredCarModule
import ca.stefanm.ibus.car.platform.LongRunningService
import ca.stefanm.ibus.car.platform.TvModuleAnnounceSim
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject
import javax.inject.Named

class TVModuleInputSwitcher @Inject constructor(

) {

    //https://github.com/piersholt/wilhelm-docs/blob/master/bmbt/4f.md


    enum class LCD_POWER { OFF, ON}
    fun setLcdPower(power : LCD_POWER) {

    }


}

@PlatformServiceInfo(
    name = "NavigationAnnounceService",
    description = "Responds to Navigation Announce Requests from GT so that the TV module " +
            "passes through the RPI Video."
)
@TvModuleAnnounceSim
class NavigationAnnounceService @Inject constructor(
    @Named(ConfiguredCarModule.SERVICE_COROUTINE_SCOPE) private val coroutineScope: CoroutineScope,
    @Named(ConfiguredCarModule.SERVICE_COROUTINE_DISPATCHER) parsingDispatcher: CoroutineDispatcher
) : LongRunningService(coroutineScope, parsingDispatcher) {
    override suspend fun doWork() {
//https://github.com/piersholt/wilhelm-docs/blob/master/02.md#gt-0x3b
    }
}