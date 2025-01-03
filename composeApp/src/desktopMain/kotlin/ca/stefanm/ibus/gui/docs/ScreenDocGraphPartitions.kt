package ca.stefanm.ibus.gui.docs

import ca.stefanm.ibus.annotations.screenflow.ScreenDoc



@ScreenDoc.GraphPartition(
    partitionName = "allPartitions",
    description = "A graph partition that contains all others."
)
annotation class BaseScreenDocPartition

@ScreenDoc.GraphPartition(
    partitionName = "Bluetooth",
    description = "Screens for configuring Bluetooth Pairing to a phone."
)
@BaseScreenDocPartition
annotation class BluetoothScreenDocPartition



/*** Navigation Screen Partitions */

@ScreenDoc.GraphPartition(
    partitionName = "POI",
    description = "Points of Interest management screens"
)
@BaseScreenDocPartition
annotation class PoiScreenDocPartition


@ScreenDoc.GraphPartition(
    partitionName = "Map",
    description = "Map Screens"
)
@BaseScreenDocPartition
annotation class MapScreenDocPartition


@PoiScreenDocPartition
@MapScreenDocPartition
@ScreenDoc.GraphPartition(
    partitionName = "Navigation",
    description = "All screens related to navigation"
)
@BaseScreenDocPartition
annotation class NavigationScreenDocPartition

@ScreenDoc.GraphPartition(
    partitionName = "Guidance",
    description = "Screens shown when setting up or in a route guidance"
)
@BaseScreenDocPartition
annotation class GuidanceScreenDocPartition

@ScreenDoc.GraphPartition(
    partitionName = "CarPlatform",
    description = "All screens for configuring the Car Platform"
)
@BaseScreenDocPartition
annotation class CarPlatformScreenDocPartition


