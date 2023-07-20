package ca.stefanm.ca.stefanm.ibus.gui.docs

import ca.stefanm.ibus.annotations.screenflow.ScreenDoc



@ScreenDoc.GraphPartition(
    partitionName = "Bluetooth",
    description = "Screens for configuring Bluetooth Pairing to a phone."
)
annotation class BluetoothScreenDocPartition





/*** Navigation Screen Partitions */

@ScreenDoc.GraphPartition(
    partitionName = "POI",
    description = "Points of Interest management screens"
)
annotation class PoiScreenDocPartition


@ScreenDoc.GraphPartition(
    partitionName = "Map",
    description = "Map Screens"
)
annotation class MapScreenDocPartition


@PoiScreenDocPartition
@MapScreenDocPartition
@ScreenDoc.GraphPartition(
    partitionName = "Navigation",
    description = "All screens related to navigation"
)
annotation class NavigationScreenDocPartition

@ScreenDoc.GraphPartition(
    partitionName = "Guidance",
    description = "Screens shown when setting up or in a route guidance"
)
annotation class GuidanceScreenDocPartition

@ScreenDoc.GraphPartition(
    partitionName = "CarPlatform",
    description = "All screens for configuring the Car Platform"
)
annotation class CarPlatformScreenDocPartition


