package ca.stefanm.ca.stefanm.ibus.gui.debug.windows.commsDebug

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import ca.stefanm.e39.proto.ConfigProtoOuterClass.ConfigProto
import ca.stefanm.e39.proto.configProto
import ca.stefanm.ibus.gui.debug.windows.NestingCard
import ca.stefanm.ibus.gui.debug.windows.NestingCardHeader


@Composable
fun ConfigMessageBuilderPane(
    setFromMessage : ConfigProto?,
    onMessageRequestToSend : (ConfigProto) -> Unit
) {

    LaunchedEffect(Unit) {
        println("ConfigMessageBuilderPane setFromMessage $setFromMessage")
    }

    val rpiFwGitCommitHash = remember {
        mutableStateOf(setFromMessage?.rpiFwGitCommitHash ?: "")
    }

    val isIbusLogOutputEnabled = remember {
        mutableStateOf(setFromMessage?.isIbusLogOutputEnabled ?: true)
    }

    val enabledMaxLogLevelForIbusLog = remember {
        mutableStateOf(
            setFromMessage?.enabledMaxLogLevelForIbusLog ?: ConfigProto.LoggingLevels.Logging_NotSet
        )
    }
    val enabledMaxLogLevelForPrintfLog = remember {
        mutableStateOf(
            setFromMessage?.enabledMaxLogLevelForPrintfLog ?: ConfigProto.LoggingLevels.Logging_NotSet
        )
    }

    val alwaysTurnOnRpiOnStatup = remember {
        mutableStateOf(
            setFromMessage?.alwaysTurnOnRpiOnStatup ?: true
        )
    }

    val alwaysTurnOnScreenOnIbusActivity = remember {
        mutableStateOf(
            setFromMessage?.alwaysTurnOnScreenOnIbusActivity ?: true
        )
    }

    val scanProgramOnBoot = remember {
        mutableStateOf(
            setFromMessage?.scanProgramOnBoot ?: ConfigProto.ScanProgram.Scan_Program_NotSet
        )
    }

    val videoSourceOnBoot = remember {
        mutableStateOf(
            setFromMessage?.videoSourceOnBoot ?: ConfigProto.VideoSource.RVC
        )
    }

    val sendBMBTEncodingPacketOnBootup = remember {
        mutableStateOf(
            setFromMessage?.sendBMBTEncodingPacketOnBootup ?: false
        )
    }

    val videoEncoding = remember {
        mutableStateOf(
            setFromMessage?.videoEncoding ?: ConfigProto.VideoEncoding.NTSC
        )
    }

    val aspectRatio = remember {
        mutableStateOf(
            setFromMessage?.aspectRatio ?: ConfigProto.AspectRatio.FourThree
        )
    }


    NestingCard {

        NestingCardHeader("Config Message Builder")

        TextField(
            value = rpiFwGitCommitHash.value,
            onValueChange = { rpiFwGitCommitHash.value = it},
            label = { Text("rpiFwGitCommitHash")}
        )

        Row {
            NestingCard {
                NestingCardHeader("enabledMaxLogLevelForIbusLog")
                Column(Modifier.selectableGroup()) {
                    ConfigProto.LoggingLevels.values().forEach {
                        Row {
                            RadioButton(
                                selected = enabledMaxLogLevelForIbusLog.value == it,
                                onClick = { enabledMaxLogLevelForIbusLog.value = it },
                            )
                            Text(text = it.name)
                        }
                    }
                }
            }
            NestingCard {
                NestingCardHeader("enabledMaxLogLevelForPrintfLog")
                Column(Modifier.selectableGroup()) {
                    ConfigProto.LoggingLevels.values().forEach {
                        Row {
                            RadioButton(
                                selected = enabledMaxLogLevelForPrintfLog.value == it,
                                onClick = { enabledMaxLogLevelForPrintfLog.value = it },
                            )
                            Text(text = it.name)
                        }
                    }
                }
            }

            NestingCard {
                NestingCardHeader("ScanProgramOnBoot")
                Column(Modifier.selectableGroup()) {
                    ConfigProto.ScanProgram.values().forEach {
                        Row {
                            RadioButton(
                                selected = scanProgramOnBoot.value == it,
                                onClick = { scanProgramOnBoot.value = it },
                            )
                            Text(text = it.name)
                        }
                    }
                }
            }
        }

        NestingCard {
            NestingCardHeader("sendBMBTEncodingPacketOnBootup")
            Row {
                Checkbox(
                    checked = sendBMBTEncodingPacketOnBootup.value,
                    onCheckedChange = { sendBMBTEncodingPacketOnBootup.value = it }
                )
                Text("sendBMBTEncodingPacketOnBootup?")
            }
        }

        Row {
            NestingCard {
                NestingCardHeader("videoEncoding")
                Column(Modifier.selectableGroup()) {
                    ConfigProto.VideoEncoding.values().forEach {
                        Row {
                            RadioButton(
                                selected = videoEncoding.value == it,
                                onClick = { videoEncoding.value = it },
                            )
                            Text(text = it.name)
                        }
                    }
                }
            }
            NestingCard {
                NestingCardHeader("aspectRatio")
                Column(Modifier.selectableGroup()) {
                    ConfigProto.AspectRatio.values().forEach {
                        Row {
                            RadioButton(
                                selected = aspectRatio.value == it,
                                onClick = { aspectRatio.value = it },
                            )
                            Text(text = it.name)
                        }
                    }
                }
            }
        }


        Button(onClick = {
            onMessageRequestToSend(
                configProto {
                    this.rpiFwGitCommitHash = rpiFwGitCommitHash.value
                    this.isIbusLogOutputEnabled = isIbusLogOutputEnabled.value
                    this.enabledMaxLogLevelForIbusLog = enabledMaxLogLevelForIbusLog.value
                    this.enabledMaxLogLevelForPrintfLog = enabledMaxLogLevelForPrintfLog.value
                    this.alwaysTurnOnRpiOnStatup = alwaysTurnOnRpiOnStatup.value
                    this.alwaysTurnOnScreenOnIbusActivity = alwaysTurnOnScreenOnIbusActivity.value
                    this.scanProgramOnBoot = scanProgramOnBoot.value
                    this.videoSourceOnBoot = videoSourceOnBoot.value
                    this.sendBMBTEncodingPacketOnBootup = sendBMBTEncodingPacketOnBootup.value
                    this.videoEncoding = videoEncoding.value
                    this.aspectRatio = aspectRatio.value
                }
            )
        }) {
            Text("Send")
        }
    }

}