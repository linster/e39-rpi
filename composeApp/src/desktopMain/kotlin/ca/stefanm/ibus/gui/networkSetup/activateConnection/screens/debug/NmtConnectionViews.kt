package ca.stefanm.ibus.gui.networkSetup.activateConnection.screens.debug

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.types.Nmt
import ca.stefanm.ibus.gui.networkSetup.activateConnection.ui.connectionList.ConnectionListItemViews
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilderScope
import org.freedesktop.networkmanager.Device
import kotlin.collections.forEach

fun Map<Device, List<Nmt.NmtConnectConnection>>.mapToViews() : List<@Composable KnobObserverBuilderScope.(Int, Int) -> Unit> {

    val results = mutableListOf<@Composable KnobObserverBuilderScope.(Int, Int) -> Unit>()

    keys.forEach { device ->
        //Put the header
        results.add(
            { allocatedIndex, currentIndex ->
                ConnectionListItemViews.ConnectionListDivider(
                    device.objectPath
                )
            }
        )
        this[device]?.forEach { conn ->
            results.add(
                { allocatedIndex, currentIndex ->


                    ConnectionListItemViews.Connection(
                        connectionName = conn.ssid ?: "<unknown>",
                        strength = conn.ap?.strength?.toInt() ?: 0,
                        modifier = Modifier,
                        isConnected = conn.apIsactive == true,
                        chipOrientation = ItemChipOrientation.W,
                        isSelected = allocatedIndex == currentIndex,
                        onClicked = CallWhen(currentIndexIs = allocatedIndex) {

                        }
                    )
                }
            )
        }

    }

    return    results

}
