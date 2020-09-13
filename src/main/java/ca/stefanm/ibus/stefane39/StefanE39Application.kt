package ca.stefanm.ibus.stefane39

import ca.stefanm.ibus.lib.platform.LongRunningService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

class StefanE39Application @Inject constructor(
    coroutineScope: CoroutineScope,
    parsingDispatcher: CoroutineDispatcher
): LongRunningService(coroutineScope, parsingDispatcher) {

    override suspend fun doWork() {

    }

}