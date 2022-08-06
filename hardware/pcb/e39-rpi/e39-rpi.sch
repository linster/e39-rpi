EESchema Schematic File Version 4
EELAYER 30 0
EELAYER END
$Descr A4 11693 8268
encoding utf-8
Sheet 1 1
Title ""
Date "2022-03-21"
Rev ""
Comp "Stefan Martynkiw"
Comment1 ""
Comment2 ""
Comment3 ""
Comment4 ""
$EndDescr
$Comp
L power:+5V #PWR01
U 1 1 580C1B61
P 3050 650
F 0 "#PWR01" H 3050 500 50  0001 C CNN
F 1 "+5V" H 3050 790 50  0000 C CNN
F 2 "" H 3050 650 50  0000 C CNN
F 3 "" H 3050 650 50  0000 C CNN
	1    3050 650 
	1    0    0    -1  
$EndComp
Wire Wire Line
	3050 650  3050 800 
Wire Wire Line
	3050 800  2850 800 
Wire Wire Line
	3050 900  2850 900 
Connection ~ 3050 800 
$Comp
L power:GND #PWR02
U 1 1 580C1D11
P 2950 2850
F 0 "#PWR02" H 2950 2600 50  0001 C CNN
F 1 "GND" H 2950 2700 50  0000 C CNN
F 2 "" H 2950 2850 50  0000 C CNN
F 3 "" H 2950 2850 50  0000 C CNN
	1    2950 2850
	1    0    0    -1  
$EndComp
Wire Wire Line
	2950 1000 2950 1400
Wire Wire Line
	2950 2400 2850 2400
Wire Wire Line
	2950 2200 2850 2200
Connection ~ 2950 2400
Wire Wire Line
	2950 1700 2850 1700
Connection ~ 2950 2200
Wire Wire Line
	2950 1400 2850 1400
Connection ~ 2950 1700
$Comp
L power:GND #PWR03
U 1 1 580C1E01
P 2250 2850
F 0 "#PWR03" H 2250 2600 50  0001 C CNN
F 1 "GND" H 2250 2700 50  0000 C CNN
F 2 "" H 2250 2850 50  0000 C CNN
F 3 "" H 2250 2850 50  0000 C CNN
	1    2250 2850
	1    0    0    -1  
$EndComp
Wire Wire Line
	2250 2700 2350 2700
Wire Wire Line
	2250 1200 2250 2000
Wire Wire Line
	2250 2000 2350 2000
Connection ~ 2250 2700
Connection ~ 2150 800 
Wire Wire Line
	2150 1600 2350 1600
Wire Wire Line
	2150 800  2350 800 
Wire Wire Line
	2150 650  2150 800 
$Comp
L power:+3.3V #PWR04
U 1 1 580C1BC1
P 2150 650
F 0 "#PWR04" H 2150 500 50  0001 C CNN
F 1 "+3.3V" H 2150 790 50  0000 C CNN
F 2 "" H 2150 650 50  0000 C CNN
F 3 "" H 2150 650 50  0000 C CNN
	1    2150 650 
	1    0    0    -1  
$EndComp
Wire Wire Line
	2250 1200 2350 1200
Connection ~ 2250 2000
Wire Wire Line
	1200 1400 2350 1400
Wire Wire Line
	2350 1700 1200 1700
Wire Wire Line
	1200 1800 2350 1800
Wire Wire Line
	1200 1900 2350 1900
Wire Wire Line
	2350 2100 1200 2100
Wire Wire Line
	1200 2500 2350 2500
Wire Wire Line
	1200 2600 2350 2600
Wire Wire Line
	2850 2100 3900 2100
Wire Wire Line
	2850 1800 3900 1800
Text Label 1200 900  0    50   ~ 0
GPIO2(SDA1)
Text Label 1200 1000 0    50   ~ 0
GPIO3(SCL1)
Text Label 1200 1100 0    50   ~ 0
GPIO4(GCLK)
Text Label 1200 1300 0    50   ~ 0
GPIO17(GEN0)
Text Label 1200 1400 0    50   ~ 0
GPIO27(GEN2)
Text Label 1200 1500 0    50   ~ 0
GPIO22(GEN3)
Text Label 1200 1700 0    50   ~ 0
GPIO10(SPI0_MOSI)
Text Label 1200 1800 0    50   ~ 0
GPIO9(SPI0_MISO)
Text Label 1200 1900 0    50   ~ 0
GPIO11(SPI0_SCK)
Text Label 1200 2100 0    50   ~ 0
ID_SD
Text Label 1200 2200 0    50   ~ 0
GPIO5
Text Label 1200 2300 0    50   ~ 0
GPIO6
Text Label 1200 2400 0    50   ~ 0
GPIO13(PWM1)
Text Label 1200 2500 0    50   ~ 0
GPIO19(SPI1_MISO)
Text Label 1200 2600 0    50   ~ 0
GPIO26
Text Label 3900 2600 2    50   ~ 0
GPIO20(SPI1_MOSI)
Text Label 3900 2500 2    50   ~ 0
GPIO16
Text Label 3900 2300 2    50   ~ 0
GPIO12(PWM0)
Text Label 3900 2100 2    50   ~ 0
ID_SC
Text Label 3900 2000 2    50   ~ 0
GPIO7(SPI1_CE_N)
Text Label 3900 1900 2    50   ~ 0
GPIO8(SPI0_CE_N)
Text Label 3900 1800 2    50   ~ 0
GPIO25(GEN6)
Text Label 3900 1600 2    50   ~ 0
GPIO24(GEN5)
Text Label 3900 1500 2    50   ~ 0
GPIO23(GEN4)
Text Label 3900 1300 2    50   ~ 0
GPIO18(GEN1)(PWM0)
Text Label 3900 1200 2    50   ~ 0
GPIO15(RXD0)
Text Label 3900 1100 2    50   ~ 0
GPIO14(TXD0)
Wire Wire Line
	2950 1000 2850 1000
Connection ~ 2950 1400
$Comp
L e39-rpi-rescue:Mounting_Hole-Mechanical MK1
U 1 1 5834FB2E
P 6000 7300
F 0 "MK1" H 6100 7346 50  0000 L CNN
F 1 "M2.5" H 6100 7255 50  0000 L CNN
F 2 "MountingHole:MountingHole_2.7mm_M2.5" H 6000 7300 60  0001 C CNN
F 3 "" H 6000 7300 60  0001 C CNN
	1    6000 7300
	1    0    0    -1  
$EndComp
$Comp
L e39-rpi-rescue:Mounting_Hole-Mechanical MK3
U 1 1 5834FBEF
P 6450 7300
F 0 "MK3" H 6550 7346 50  0000 L CNN
F 1 "M2.5" H 6550 7255 50  0000 L CNN
F 2 "MountingHole:MountingHole_2.7mm_M2.5" H 6450 7300 60  0001 C CNN
F 3 "" H 6450 7300 60  0001 C CNN
	1    6450 7300
	1    0    0    -1  
$EndComp
$Comp
L e39-rpi-rescue:Mounting_Hole-Mechanical MK2
U 1 1 5834FC19
P 6000 7500
F 0 "MK2" H 6100 7546 50  0000 L CNN
F 1 "M2.5" H 6100 7455 50  0000 L CNN
F 2 "MountingHole:MountingHole_2.7mm_M2.5" H 6000 7500 60  0001 C CNN
F 3 "" H 6000 7500 60  0001 C CNN
	1    6000 7500
	1    0    0    -1  
$EndComp
$Comp
L e39-rpi-rescue:Mounting_Hole-Mechanical MK4
U 1 1 5834FC4F
P 6450 7500
F 0 "MK4" H 6550 7546 50  0000 L CNN
F 1 "M2.5" H 6550 7455 50  0000 L CNN
F 2 "MountingHole:MountingHole_2.7mm_M2.5" H 6450 7500 60  0001 C CNN
F 3 "" H 6450 7500 60  0001 C CNN
	1    6450 7500
	1    0    0    -1  
$EndComp
Text Notes 6000 7150 0    50   ~ 0
Mounting Holes
$Comp
L Connector_Generic:Conn_02x20_Odd_Even P1
U 1 1 59AD464A
P 2550 1700
F 0 "P1" H 2600 2817 50  0000 C CNN
F 1 "Conn_02x20_Odd_Even" H 2600 2726 50  0000 C CNN
F 2 "Connector_PinSocket_2.54mm:PinSocket_2x20_P2.54mm_Vertical" H -2300 750 50  0001 C CNN
F 3 "" H -2300 750 50  0001 C CNN
	1    2550 1700
	1    0    0    -1  
$EndComp
Text Label 3900 2700 2    50   ~ 0
GPIO21(SPI1_SCK)
Wire Wire Line
	3050 800  3050 900 
Wire Wire Line
	2950 2400 2950 2500
Wire Wire Line
	2950 2200 2950 2400
Wire Wire Line
	2950 1700 2950 2200
Wire Wire Line
	2250 2700 2250 2850
Wire Wire Line
	2150 800  2150 1600
Wire Wire Line
	2250 2000 2250 2700
Wire Wire Line
	2950 1400 2950 1700
$Comp
L Device:R R5
U 1 1 623544D9
P 5900 1100
F 0 "R5" H 5830 1054 50  0000 R CNN
F 1 "120" H 5830 1145 50  0000 R CNN
F 2 "Resistor_THT:R_Axial_DIN0309_L9.0mm_D3.2mm_P15.24mm_Horizontal" V 5830 1100 50  0001 C CNN
F 3 "~" H 5900 1100 50  0001 C CNN
	1    5900 1100
	-1   0    0    1   
$EndComp
$Comp
L Device:R R6
U 1 1 6235F56F
P 6200 1100
F 0 "R6" H 6130 1054 50  0000 R CNN
F 1 "120" H 6130 1145 50  0000 R CNN
F 2 "Resistor_THT:R_Axial_DIN0309_L9.0mm_D3.2mm_P15.24mm_Horizontal" V 6130 1100 50  0001 C CNN
F 3 "~" H 6200 1100 50  0001 C CNN
	1    6200 1100
	-1   0    0    1   
$EndComp
Wire Wire Line
	5900 1250 5900 1450
Wire Wire Line
	5900 950  5900 750 
Wire Wire Line
	6200 1250 6200 1450
Wire Wire Line
	6200 950  6200 750 
$Comp
L Device:R RR7
U 1 1 623794BA
P 6700 1100
F 0 "RR7" H 6770 1146 50  0000 L CNN
F 1 "499" H 6770 1055 50  0000 L CNN
F 2 "Resistor_THT:R_Axial_DIN0309_L9.0mm_D3.2mm_P15.24mm_Horizontal" V 6630 1100 50  0001 C CNN
F 3 "~" H 6700 1100 50  0001 C CNN
	1    6700 1100
	1    0    0    -1  
$EndComp
$Comp
L Device:R RR6
U 1 1 62379A8B
P 6950 1100
F 0 "RR6" H 7020 1146 50  0000 L CNN
F 1 "1K" H 7020 1055 50  0000 L CNN
F 2 "Resistor_THT:R_Axial_DIN0309_L9.0mm_D3.2mm_P15.24mm_Horizontal" V 6880 1100 50  0001 C CNN
F 3 "~" H 6950 1100 50  0001 C CNN
	1    6950 1100
	1    0    0    -1  
$EndComp
$Comp
L Device:R RR5
U 1 1 62379D43
P 7200 1100
F 0 "RR5" H 7270 1146 50  0000 L CNN
F 1 "2K" H 7270 1055 50  0000 L CNN
F 2 "Resistor_THT:R_Axial_DIN0309_L9.0mm_D3.2mm_P15.24mm_Horizontal" V 7130 1100 50  0001 C CNN
F 3 "~" H 7200 1100 50  0001 C CNN
	1    7200 1100
	1    0    0    -1  
$EndComp
$Comp
L Device:R RR4
U 1 1 6237A0A4
P 7450 1100
F 0 "RR4" H 7520 1146 50  0000 L CNN
F 1 "4K" H 7520 1055 50  0000 L CNN
F 2 "Resistor_THT:R_Axial_DIN0309_L9.0mm_D3.2mm_P15.24mm_Horizontal" V 7380 1100 50  0001 C CNN
F 3 "~" H 7450 1100 50  0001 C CNN
	1    7450 1100
	1    0    0    -1  
$EndComp
$Comp
L Device:R RR3
U 1 1 6237A573
P 7700 1100
F 0 "RR3" H 7770 1146 50  0000 L CNN
F 1 "8K" H 7770 1055 50  0000 L CNN
F 2 "Resistor_THT:R_Axial_DIN0309_L9.0mm_D3.2mm_P15.24mm_Horizontal" V 7630 1100 50  0001 C CNN
F 3 "~" H 7700 1100 50  0001 C CNN
	1    7700 1100
	1    0    0    -1  
$EndComp
Wire Wire Line
	6700 1250 6700 1400
Wire Wire Line
	6700 1400 6950 1400
Wire Wire Line
	6950 1400 6950 1250
Connection ~ 6700 1400
Wire Wire Line
	6700 1400 6700 1500
Wire Wire Line
	6950 1400 7200 1400
Wire Wire Line
	7200 1400 7200 1250
Connection ~ 6950 1400
Wire Wire Line
	7200 1400 7450 1400
Wire Wire Line
	7450 1400 7450 1250
Connection ~ 7200 1400
Wire Wire Line
	7450 1400 7700 1400
Wire Wire Line
	7700 1400 7700 1250
Connection ~ 7450 1400
Wire Wire Line
	6700 950  6700 750 
Wire Wire Line
	6950 950  6950 750 
Wire Wire Line
	7200 950  7200 750 
Wire Wire Line
	7450 950  7450 750 
Wire Wire Line
	7700 950  7700 750 
$Comp
L Device:R RG7
U 1 1 623879F2
P 8200 1100
F 0 "RG7" H 8270 1146 50  0000 L CNN
F 1 "499" H 8270 1055 50  0000 L CNN
F 2 "Resistor_THT:R_Axial_DIN0309_L9.0mm_D3.2mm_P15.24mm_Horizontal" V 8130 1100 50  0001 C CNN
F 3 "~" H 8200 1100 50  0001 C CNN
	1    8200 1100
	1    0    0    -1  
$EndComp
$Comp
L Device:R RG6
U 1 1 62387BD1
P 8450 1100
F 0 "RG6" H 8520 1146 50  0000 L CNN
F 1 "1K" H 8520 1055 50  0000 L CNN
F 2 "Resistor_THT:R_Axial_DIN0309_L9.0mm_D3.2mm_P15.24mm_Horizontal" V 8380 1100 50  0001 C CNN
F 3 "~" H 8450 1100 50  0001 C CNN
	1    8450 1100
	1    0    0    -1  
$EndComp
$Comp
L Device:R RG5
U 1 1 62387F3E
P 8700 1100
F 0 "RG5" H 8770 1146 50  0000 L CNN
F 1 "2K" H 8770 1055 50  0000 L CNN
F 2 "Resistor_THT:R_Axial_DIN0309_L9.0mm_D3.2mm_P15.24mm_Horizontal" V 8630 1100 50  0001 C CNN
F 3 "~" H 8700 1100 50  0001 C CNN
	1    8700 1100
	1    0    0    -1  
$EndComp
$Comp
L Device:R RG4
U 1 1 62388164
P 8950 1100
F 0 "RG4" H 9020 1146 50  0000 L CNN
F 1 "4K" H 9020 1055 50  0000 L CNN
F 2 "Resistor_THT:R_Axial_DIN0309_L9.0mm_D3.2mm_P15.24mm_Horizontal" V 8880 1100 50  0001 C CNN
F 3 "~" H 8950 1100 50  0001 C CNN
	1    8950 1100
	1    0    0    -1  
$EndComp
$Comp
L Device:R RG3
U 1 1 623883EB
P 9200 1100
F 0 "RG3" H 9270 1146 50  0000 L CNN
F 1 "8K" H 9270 1055 50  0000 L CNN
F 2 "Resistor_THT:R_Axial_DIN0309_L9.0mm_D3.2mm_P15.24mm_Horizontal" V 9130 1100 50  0001 C CNN
F 3 "~" H 9200 1100 50  0001 C CNN
	1    9200 1100
	1    0    0    -1  
$EndComp
$Comp
L Device:R RG2
U 1 1 62388665
P 9450 1100
F 0 "RG2" H 9520 1146 50  0000 L CNN
F 1 "16K" H 9520 1055 50  0000 L CNN
F 2 "Resistor_THT:R_Axial_DIN0309_L9.0mm_D3.2mm_P15.24mm_Horizontal" V 9380 1100 50  0001 C CNN
F 3 "~" H 9450 1100 50  0001 C CNN
	1    9450 1100
	1    0    0    -1  
$EndComp
Wire Wire Line
	8200 1400 8450 1400
Wire Wire Line
	8450 1400 8450 1250
Wire Wire Line
	8450 1400 8700 1400
Connection ~ 8450 1400
Wire Wire Line
	8700 1400 8950 1400
Wire Wire Line
	8950 1400 8950 1250
Connection ~ 8700 1400
Wire Wire Line
	8950 1400 9200 1400
Wire Wire Line
	9200 1400 9200 1250
Connection ~ 8950 1400
Wire Wire Line
	9200 1400 9450 1400
Wire Wire Line
	9450 1400 9450 1250
Connection ~ 9200 1400
Wire Wire Line
	8200 1400 8200 1500
Connection ~ 8200 1400
Wire Wire Line
	8200 950  8200 750 
Wire Wire Line
	8450 950  8450 750 
Wire Wire Line
	8700 950  8700 750 
Wire Wire Line
	8950 950  8950 750 
Wire Wire Line
	9200 950  9200 750 
Wire Wire Line
	9450 950  9450 750 
$Comp
L Device:R RB7
U 1 1 623A087A
P 9950 1100
F 0 "RB7" H 10020 1146 50  0000 L CNN
F 1 "499" H 10020 1055 50  0000 L CNN
F 2 "Resistor_THT:R_Axial_DIN0309_L9.0mm_D3.2mm_P15.24mm_Horizontal" V 9880 1100 50  0001 C CNN
F 3 "~" H 9950 1100 50  0001 C CNN
	1    9950 1100
	1    0    0    -1  
$EndComp
$Comp
L Device:R RB6
U 1 1 623A0D22
P 10200 1100
F 0 "RB6" H 10270 1146 50  0000 L CNN
F 1 "1K" H 10270 1055 50  0000 L CNN
F 2 "Resistor_THT:R_Axial_DIN0309_L9.0mm_D3.2mm_P15.24mm_Horizontal" V 10130 1100 50  0001 C CNN
F 3 "~" H 10200 1100 50  0001 C CNN
	1    10200 1100
	1    0    0    -1  
$EndComp
$Comp
L Device:R RB5
U 1 1 623A107C
P 10450 1100
F 0 "RB5" H 10520 1146 50  0000 L CNN
F 1 "2K" H 10520 1055 50  0000 L CNN
F 2 "Resistor_THT:R_Axial_DIN0309_L9.0mm_D3.2mm_P15.24mm_Horizontal" V 10380 1100 50  0001 C CNN
F 3 "~" H 10450 1100 50  0001 C CNN
	1    10450 1100
	1    0    0    -1  
$EndComp
$Comp
L Device:R RB4
U 1 1 623A1311
P 10700 1100
F 0 "RB4" H 10770 1146 50  0000 L CNN
F 1 "4k" H 10770 1055 50  0000 L CNN
F 2 "Resistor_THT:R_Axial_DIN0309_L9.0mm_D3.2mm_P15.24mm_Horizontal" V 10630 1100 50  0001 C CNN
F 3 "~" H 10700 1100 50  0001 C CNN
	1    10700 1100
	1    0    0    -1  
$EndComp
$Comp
L Device:R RB3
U 1 1 623A166F
P 10950 1100
F 0 "RB3" H 11020 1146 50  0000 L CNN
F 1 "8K" H 11020 1055 50  0000 L CNN
F 2 "Resistor_THT:R_Axial_DIN0309_L9.0mm_D3.2mm_P15.24mm_Horizontal" V 10880 1100 50  0001 C CNN
F 3 "~" H 10950 1100 50  0001 C CNN
	1    10950 1100
	1    0    0    -1  
$EndComp
Wire Wire Line
	9950 1250 9950 1400
Wire Wire Line
	9950 1400 10200 1400
Wire Wire Line
	10200 1400 10200 1250
Wire Wire Line
	10200 1400 10450 1400
Wire Wire Line
	10450 1400 10450 1250
Connection ~ 10200 1400
Wire Wire Line
	10450 1400 10700 1400
Wire Wire Line
	10700 1400 10700 1250
Connection ~ 10450 1400
Wire Wire Line
	10700 1400 10950 1400
Wire Wire Line
	10950 1400 10950 1250
Connection ~ 10700 1400
Wire Wire Line
	9950 1400 9950 1500
Connection ~ 9950 1400
Wire Wire Line
	9950 950  9950 750 
Wire Wire Line
	10200 950  10200 750 
Wire Wire Line
	10450 950  10450 750 
Wire Wire Line
	10700 950  10700 750 
Wire Wire Line
	10950 950  10950 750 
$Comp
L Transistor_BJT:BC548 Q1
U 1 1 623CA470
P 9000 2550
F 0 "Q1" V 9328 2550 50  0000 C CNN
F 1 "BC548" V 9237 2550 50  0000 C CNN
F 2 "Package_TO_SOT_THT:TO-92_Inline" H 9200 2475 50  0001 L CIN
F 3 "http://www.fairchildsemi.com/ds/BC/BC547.pdf" H 9000 2550 50  0001 L CNN
	1    9000 2550
	0    -1   -1   0   
$EndComp
Wire Wire Line
	9000 2750 9000 3050
$Comp
L Device:R RC2
U 1 1 623F42B9
P 9500 2450
F 0 "RC2" V 9707 2450 50  0000 C CNN
F 1 "680" V 9616 2450 50  0000 C CNN
F 2 "Resistor_THT:R_Axial_DIN0309_L9.0mm_D3.2mm_P15.24mm_Horizontal" V 9430 2450 50  0001 C CNN
F 3 "~" H 9500 2450 50  0001 C CNN
	1    9500 2450
	0    -1   -1   0   
$EndComp
Wire Wire Line
	8850 3050 9000 3050
$Comp
L Device:R RC1
U 1 1 623D2BD0
P 8700 3050
F 0 "RC1" V 8493 3050 50  0000 C CNN
F 1 "1K" V 8584 3050 50  0000 C CNN
F 2 "Resistor_THT:R_Axial_DIN0309_L9.0mm_D3.2mm_P15.24mm_Horizontal" V 8630 3050 50  0001 C CNN
F 3 "~" H 8700 3050 50  0001 C CNN
	1    8700 3050
	0    1    1    0   
$EndComp
Wire Wire Line
	8400 2450 8800 2450
Wire Wire Line
	8550 3050 8400 3050
Wire Wire Line
	9200 2450 9350 2450
Wire Wire Line
	9650 2450 9850 2450
Wire Wire Line
	9850 2450 9850 3100
Wire Wire Line
	9850 2450 9850 1950
Connection ~ 9850 2450
$Comp
L Device:CP CC1
U 1 1 6241A050
P 8950 1950
F 0 "CC1" V 8695 1950 50  0000 C CNN
F 1 "CP100uF" V 8786 1950 50  0000 C CNN
F 2 "Capacitor_THT:CP_Radial_D5.0mm_P2.00mm" H 8988 1800 50  0001 C CNN
F 3 "~" H 8950 1950 50  0001 C CNN
	1    8950 1950
	0    1    1    0   
$EndComp
Wire Wire Line
	9100 1950 9850 1950
Wire Wire Line
	8800 1950 8400 1950
Wire Wire Line
	10550 2900 10550 3100
Wire Wire Line
	10250 2900 10250 3100
Wire Wire Line
	950  900  2350 900 
Wire Wire Line
	950  1000 2350 1000
Wire Wire Line
	950  1100 2350 1100
Wire Wire Line
	950  1300 2350 1300
Wire Wire Line
	950  1500 2350 1500
Wire Wire Line
	950  2200 2350 2200
Wire Wire Line
	950  2300 2350 2300
Wire Wire Line
	950  2400 2350 2400
Wire Wire Line
	2850 1100 4250 1100
Wire Wire Line
	2850 1200 4250 1200
Wire Wire Line
	2850 1500 4250 1500
Wire Wire Line
	2850 1600 4250 1600
Wire Wire Line
	2850 1900 4250 1900
Wire Wire Line
	2850 2000 4250 2000
Wire Wire Line
	2850 2300 4250 2300
Wire Wire Line
	2850 2500 2950 2500
Wire Wire Line
	2850 2600 4250 2600
Wire Wire Line
	2850 2700 4250 2700
$Comp
L 2022-03-19_16-28-06:TS3V330D U3
U 1 1 62488A47
P 4100 4050
F 0 "U3" H 5200 4437 60  0000 C CNN
F 1 "TS3V330D" H 5200 4331 60  0000 C CNN
F 2 "Package_SO:SOIC-16_3.9x9.9mm_P1.27mm" H 5200 4290 60  0001 C CNN
F 3 "" H 4100 4050 60  0000 C CNN
	1    4100 4050
	1    0    0    -1  
$EndComp
Connection ~ 2950 2500
Wire Wire Line
	2950 2500 2950 2850
Wire Wire Line
	2950 2500 4250 2500
Text Notes 7950 2700 2    50   ~ 0
TODO Rev2: Design NTSC -> RGB circuit with LM1881 sync separator.\nPort for Backup Camera inputs goes here.\nGround Mux_1_in for local override.
$Comp
L 2022-03-19_16-59-52:1-963539-1 J_BMBT1
U 1 1 624F6933
P 2000 3550
F 0 "J_BMBT1" H 2528 2753 60  0000 L CNN
F 1 "1-963539-1" H 2528 2647 60  0000 L CNN
F 2 "ul_1-963539-1:1-963539-1" H 2400 2690 60  0001 C CNN
F 3 "" H 2000 3550 60  0000 C CNN
	1    2000 3550
	1    0    0    -1  
$EndComp
$Comp
L 2022-03-19_16-59-52:1-963539-1 J_Upstream1
U 1 1 624F9A07
P 1050 3550
F 0 "J_Upstream1" H 1292 3837 60  0000 C CNN
F 1 "1-963539-1" H 1292 3731 60  0000 C CNN
F 2 "ul_1-963539-1:1-963539-1" H 1450 2690 60  0001 C CNN
F 3 "" H 1050 3550 60  0000 C CNN
	1    1050 3550
	-1   0    0    -1  
$EndComp
Wire Wire Line
	1050 3650 2000 3650
Wire Wire Line
	1050 3750 1300 3750
Wire Wire Line
	1300 3750 1300 3450
Connection ~ 1300 3750
Wire Wire Line
	1300 3750 2000 3750
Wire Wire Line
	1050 3850 2000 3850
Wire Wire Line
	1050 5250 2000 5250
Wire Wire Line
	1050 4750 2000 4750
Wire Wire Line
	1050 4550 2000 4550
Wire Wire Line
	1050 4250 2000 4250
Wire Wire Line
	1050 3950 1150 3950
Wire Wire Line
	1050 4050 1150 4050
Wire Wire Line
	1050 4150 1150 4150
Wire Wire Line
	2000 3950 1900 3950
Wire Wire Line
	2000 4050 1900 4050
Wire Wire Line
	2000 4150 1900 4150
Wire Wire Line
	2000 4850 1900 4850
Wire Wire Line
	2000 4950 1900 4950
Wire Wire Line
	2000 5050 1900 5050
Wire Wire Line
	1050 4850 1150 4850
Wire Wire Line
	1050 4950 1150 4950
Wire Wire Line
	1050 5050 1150 5050
Text HLabel 1900 4850 0    50   Output ~ 0
Gs_bmbt
Text HLabel 1900 4950 0    50   Output ~ 0
Bs_bmbt
Text HLabel 1900 5050 0    50   Output ~ 0
Rs_bmbt
Wire Wire Line
	1050 4650 2000 4650
Connection ~ 1450 4450
Wire Wire Line
	1450 4450 2000 4450
Wire Wire Line
	1050 4450 1450 4450
Wire Wire Line
	1450 4450 1450 3450
Wire Wire Line
	4100 4750 3950 4750
Text Notes 1700 3400 0    50   ~ 0
Where's the switched ACC 12v?
Text Label 5900 900  0    50   ~ 0
V
Text Label 6200 900  0    50   ~ 0
H
Text Label 1050 900  0    50   ~ 0
V
Text Label 1050 1000 0    50   ~ 0
H
Text Label 1050 1100 0    50   ~ 0
B3
Text Label 1050 1300 0    50   ~ 0
G7
Text Label 1050 1500 0    50   ~ 0
R5
Text Label 1050 2200 0    50   ~ 0
B4
Text Label 1050 2300 0    50   ~ 0
B5
Text Label 1050 2400 0    50   ~ 0
G3
Text Label 4050 1100 0    50   ~ 0
G4
Text Label 4050 1200 0    50   ~ 0
G5
Text Label 4050 1500 0    50   ~ 0
R6
Text Label 4050 1600 0    50   ~ 0
R7
Text Label 4050 1900 0    50   ~ 0
B7
Text Label 4050 2000 0    50   ~ 0
B6
Text Label 4050 2300 0    50   ~ 0
G2
Text Label 4050 2500 0    50   ~ 0
G6
Text Label 4050 2600 0    50   ~ 0
R3
Text Label 4050 2700 0    50   ~ 0
R4
Text Label 5900 1400 0    50   ~ 0
VGAV
Text Label 6200 1400 0    50   ~ 0
VGAH
Text Label 6700 1500 0    50   ~ 0
VGAR
Text Label 6700 900  0    50   ~ 0
R7
Text Label 6950 900  0    50   ~ 0
R6
Text Label 7200 900  0    50   ~ 0
R5
Text Label 7450 900  0    50   ~ 0
R4
Text Label 7700 900  0    50   ~ 0
R3
Text Label 8200 900  0    50   ~ 0
G7
Text Label 8450 900  0    50   ~ 0
G6
Text Label 8700 900  0    50   ~ 0
G5
Text Label 8950 900  0    50   ~ 0
G4
Text Label 9200 900  0    50   ~ 0
G3
Text Label 9450 900  0    50   ~ 0
G2
Text Label 8200 1500 0    50   ~ 0
VGAG
Text Label 9950 900  0    50   ~ 0
B7
Text Label 10200 900  0    50   ~ 0
B6
Text Label 10450 900  0    50   ~ 0
B5
Text Label 10700 900  0    50   ~ 0
B4
Text Label 10950 900  0    50   ~ 0
B3
Text Label 9950 1500 0    50   ~ 0
VGAB
Text Label 8650 1950 2    50   ~ 0
VGAG
Text Label 8650 2450 2    50   ~ 0
VGAH
Text Label 8500 3050 2    50   ~ 0
VGAV
Text Label 9850 3050 0    50   ~ 0
PiG
Text Label 10250 3100 0    50   ~ 0
PiR
Text Label 10550 3100 0    50   ~ 0
PiB
Text Label 10250 2900 2    50   ~ 0
VGAR
Text Label 10550 2900 2    50   ~ 0
VGAB
Text Label 1450 3450 0    50   ~ 0
C_GND
Text Label 1300 3450 1    50   ~ 0
IBUS
Text Label 1250 3150 0    50   ~ 0
+12V_AlwaysHot
Text Label 3950 4750 0    50   ~ 0
C_GND
Text Label 1100 3950 0    50   ~ 0
G_up
Text Label 1100 4050 0    50   ~ 0
B_up
Text Label 1100 4150 0    50   ~ 0
R_up
Text Label 1100 4850 0    50   ~ 0
Gs_up
Text Label 1100 4950 0    50   ~ 0
Bs_up
Text Label 1100 5050 0    50   ~ 0
Rs_up
Wire Wire Line
	1050 4350 2000 4350
Text Label 1900 3950 2    50   ~ 0
G_bmbt
Text Label 1900 4050 2    50   ~ 0
B_bmbt
Text Label 1900 4150 2    50   ~ 0
R_bmbt
Text Label 7800 4700 0    50   ~ 0
C_GND
Wire Wire Line
	7800 4700 8250 4700
Wire Wire Line
	8250 4300 7850 4300
Wire Wire Line
	8250 4600 7850 4600
Text Label 7850 4300 0    50   ~ 0
G_bmbt
Text Label 7850 4600 0    50   ~ 0
B_bmbt
Wire Wire Line
	10450 4500 11000 4500
Text Label 10850 4700 2    50   ~ 0
R_bmbt
Wire Wire Line
	10450 4700 10850 4700
$Comp
L 2022-03-19_16-28-06:TS3V330D U4
U 1 1 62487A9F
P 8250 4000
F 0 "U4" H 9350 4387 60  0000 C CNN
F 1 "TS3V330D" H 9350 4281 60  0000 C CNN
F 2 "Package_SO:SOIC-16_3.9x9.9mm_P1.27mm" H 9350 4240 60  0001 C CNN
F 3 "" H 8250 4000 60  0000 C CNN
	1    8250 4000
	1    0    0    -1  
$EndComp
Wire Wire Line
	8200 1250 8200 1400
Wire Wire Line
	8700 1250 8700 1400
Wire Wire Line
	8250 4500 7850 4500
Wire Wire Line
	8250 4100 7850 4100
Text Label 7850 4100 0    50   ~ 0
G_up
Text Label 7850 4500 0    50   ~ 0
B_up
Text Label 10600 4500 0    50   ~ 0
R_up
Wire Wire Line
	7550 4200 7550 5000
Wire Wire Line
	7550 5000 3350 5000
Wire Wire Line
	3350 5000 3350 4350
Wire Wire Line
	3350 4350 4100 4350
Wire Wire Line
	7550 4200 8250 4200
Wire Wire Line
	7600 4400 7600 5050
Wire Wire Line
	7600 5050 3400 5050
Wire Wire Line
	3400 5050 3400 4650
Wire Wire Line
	3400 4650 4100 4650
Wire Wire Line
	7600 4400 8250 4400
Wire Wire Line
	6300 4750 6450 4750
Wire Wire Line
	6450 4750 6450 5100
Wire Wire Line
	6450 5100 11000 5100
Wire Wire Line
	11000 5100 11000 4600
Wire Wire Line
	10450 4600 11000 4600
Wire Wire Line
	6000 2900 6000 3150
Wire Wire Line
	6250 2900 6250 3150
Wire Wire Line
	6550 2900 6550 3150
Text Label 6000 3100 0    50   ~ 0
CamG
Text Label 6250 3100 0    50   ~ 0
CamR
Text Label 6550 3100 0    50   ~ 0
CamB
Wire Wire Line
	4100 4150 3700 4150
Wire Wire Line
	4100 4250 3700 4250
Wire Wire Line
	4100 4450 3700 4450
Wire Wire Line
	4100 4550 3700 4550
Wire Wire Line
	6300 4550 6750 4550
Wire Wire Line
	6300 4650 6750 4650
Text Label 3750 4150 0    50   ~ 0
CamG
Text Label 3750 4250 0    50   ~ 0
PiG
Text Label 3750 4450 0    50   ~ 0
CamB
Text Label 3750 4550 0    50   ~ 0
PiB
Text Label 6550 4550 0    50   ~ 0
CamR
Text Label 6550 4650 0    50   ~ 0
PiR
Wire Wire Line
	1050 5150 1200 5150
Text Label 1200 5150 0    50   ~ 0
TVM_backup_cam_sw
Text Label 4600 7600 2    50   ~ 0
Prog_Reset
Wire Wire Line
	2850 1300 4250 1300
Text Label 4050 1300 0    50   ~ 0
Prog_Reset
Text Label 4550 7450 2    50   ~ 0
Prog_SCK
Text Label 4550 7300 2    50   ~ 0
Prog_MOSI
Text Label 4550 7150 2    50   ~ 0
Prog_MISO
$Comp
L 2022-03-21_21-19-33:OSTVN04A150 J1
U 1 1 62DB1647
P 1050 2750
F 0 "J1" H 1292 3037 60  0000 C CNN
F 1 "OSTVN04A150" H 1292 2931 60  0000 C CNN
F 2 "OSTVN04A150:OSTVN04A150" H 1450 2490 60  0001 C CNN
F 3 "" H 1050 2750 60  0000 C CNN
	1    1050 2750
	-1   0    0    -1  
$EndComp
Wire Wire Line
	1250 3150 1150 3150
Wire Wire Line
	1050 2750 1250 2750
Wire Wire Line
	1050 2850 1250 2850
Wire Wire Line
	1050 2950 1250 2950
Text Label 1100 2750 0    50   ~ 0
+12V_AlwaysHot
Text Label 1150 2950 0    50   ~ 0
C_GND
$Comp
L 2022-03-21_21-19-33:OSTVN04A150 J2
U 1 1 62E38657
P 9950 5800
F 0 "J2" H 10478 5703 60  0000 L CNN
F 1 "OSTVN04A150" H 10478 5597 60  0000 L CNN
F 2 "OSTVN04A150:OSTVN04A150" H 10350 5540 60  0001 C CNN
F 3 "" H 9950 5800 60  0000 C CNN
	1    9950 5800
	1    0    0    -1  
$EndComp
Wire Wire Line
	9950 5800 9700 5800
Wire Wire Line
	9950 5900 9700 5900
Wire Wire Line
	9950 6000 9700 6000
Wire Wire Line
	9950 6100 9700 6100
$Comp
L 2022-03-21_21-19-33:OSTVN04A150 J3
U 1 1 62E8042B
P 6800 2000
F 0 "J3" H 7328 1903 60  0000 L CNN
F 1 "OSTVN04A150" H 7328 1797 60  0000 L CNN
F 2 "OSTVN04A150:OSTVN04A150" H 7200 1740 60  0001 C CNN
F 3 "" H 6800 2000 60  0000 C CNN
	1    6800 2000
	1    0    0    -1  
$EndComp
Wire Wire Line
	6800 2000 6450 2000
Wire Wire Line
	6800 2100 6450 2100
Wire Wire Line
	6800 2200 6450 2200
Wire Wire Line
	6800 2300 6450 2300
Wire Wire Line
	4100 4050 4100 3850
Wire Wire Line
	4100 3850 3700 3850
Text Label 3700 3850 0    50   ~ 0
Mux_1_IN
Text Label 6450 2000 0    50   ~ 0
CamG
Text Label 6450 2100 0    50   ~ 0
CamR
Text Label 6450 2200 0    50   ~ 0
CamB
Text Label 6450 2300 0    50   ~ 0
Mux_1_IN
Text Label 9700 5800 0    50   ~ 0
RUN
Text Label 9700 5900 0    50   ~ 0
Global_EN
Text Label 9900 6000 2    50   ~ 0
Prog_Reset
Text Label 1150 3050 0    50   ~ 0
IBUS
Text Label 9850 6100 2    50   ~ 0
Mux_2_IN
Wire Wire Line
	8250 4000 8150 4000
Wire Wire Line
	8150 4000 8150 3800
Wire Wire Line
	8150 3800 7850 3800
Text Label 8100 3800 2    50   ~ 0
Mux_2_IN
Wire Wire Line
	5350 6850 5650 6850
Text Label 5350 6850 2    50   ~ 0
3V3Lin
Wire Wire Line
	1050 3050 1250 3050
Wire Wire Line
	1050 3550 1150 3550
Wire Wire Line
	1150 3150 1150 3550
Connection ~ 1150 3550
Wire Wire Line
	1150 3550 2000 3550
$Comp
L Jumper:Jumper_3_Bridged12 JP2
U 1 1 6328FBE4
P 8100 5750
F 0 "JP2" H 8100 5954 50  0000 C CNN
F 1 "Jumper_3_Bridged12" H 8100 5863 50  0000 C CNN
F 2 "" H 8100 5750 50  0001 C CNN
F 3 "~" H 8100 5750 50  0001 C CNN
	1    8100 5750
	1    0    0    -1  
$EndComp
$Comp
L Jumper:Jumper_3_Bridged12 JP3
U 1 1 6329026D
P 8100 6450
F 0 "JP3" H 8100 6654 50  0000 C CNN
F 1 "Jumper_3_Bridged12" H 8100 6563 50  0000 C CNN
F 2 "" H 8100 6450 50  0001 C CNN
F 3 "~" H 8100 6450 50  0001 C CNN
	1    8100 6450
	1    0    0    -1  
$EndComp
$Comp
L Device:Jumper JP1
U 1 1 632E5A93
P 6750 4150
F 0 "JP1" H 6750 4414 50  0000 C CNN
F 1 "Jumper" H 6750 4323 50  0000 C CNN
F 2 "" H 6750 4150 50  0001 C CNN
F 3 "~" H 6750 4150 50  0001 C CNN
	1    6750 4150
	1    0    0    -1  
$EndComp
Wire Wire Line
	6300 4150 6450 4150
Wire Wire Line
	7050 4150 7200 4150
Text Label 7050 4150 0    50   ~ 0
C_GND
$Comp
L Device:Jumper JP4
U 1 1 6331921D
P 10800 4100
F 0 "JP4" H 10800 4364 50  0000 C CNN
F 1 "Jumper" H 10800 4273 50  0000 C CNN
F 2 "" H 10800 4100 50  0001 C CNN
F 3 "~" H 10800 4100 50  0001 C CNN
	1    10800 4100
	1    0    0    -1  
$EndComp
Wire Wire Line
	10500 4100 10450 4100
Wire Wire Line
	11100 4100 11200 4100
Text Label 11100 4100 0    50   ~ 0
C_GND
$Comp
L 2022-03-21_21-19-33:OSTVN04A150 J4
U 1 1 63337494
P 4800 7200
F 0 "J4" V 4989 7328 60  0000 L CNN
F 1 "OSTVN04A150" V 5095 7328 60  0000 L CNN
F 2 "CONN_OSTVN04A150_OST" H 5200 6940 60  0001 C CNN
F 3 "" H 4800 7200 60  0000 C CNN
	1    4800 7200
	1    0    0    -1  
$EndComp
Wire Wire Line
	4800 7150 4800 7200
Wire Wire Line
	4150 7150 4800 7150
Wire Wire Line
	4150 7300 4800 7300
Wire Wire Line
	4800 7450 4800 7400
Wire Wire Line
	4150 7450 4800 7450
Wire Wire Line
	4800 7600 4800 7500
Wire Wire Line
	4150 7600 4800 7600
Text Label 1100 2850 0    50   ~ 0
+12V_AlwaysHot
$Comp
L 2022-03-21_21-19-33:OSTVN04A150 J?
U 1 1 633CF0A6
P 2600 7200
F 0 "J?" H 3128 7103 60  0000 L CNN
F 1 "OSTVN04A150" H 3128 6997 60  0000 L CNN
F 2 "CONN_OSTVN04A150_OST" H 3000 6940 60  0001 C CNN
F 3 "" H 2600 7200 60  0000 C CNN
	1    2600 7200
	1    0    0    -1  
$EndComp
Wire Wire Line
	1100 5550 1250 5550
Wire Wire Line
	1250 5550 1250 5700
Wire Wire Line
	1250 5850 1400 5850
Wire Wire Line
	1250 5700 1100 5700
Connection ~ 1250 5700
Wire Wire Line
	1250 5700 1250 5850
Wire Wire Line
	1250 5850 1100 5850
Connection ~ 1250 5850
Text Label 1100 5550 2    50   ~ 0
Gs_up
Text Label 1100 5700 2    50   ~ 0
Bs_up
Text Label 1100 5850 2    50   ~ 0
Rs_up
Text Label 1400 5850 0    50   ~ 0
s_up
Wire Wire Line
	1700 5850 2100 5850
Wire Wire Line
	2100 5850 2100 5700
Wire Wire Line
	2100 5550 2300 5550
Wire Wire Line
	2100 5700 2300 5700
Connection ~ 2100 5700
Wire Wire Line
	2100 5700 2100 5550
Wire Wire Line
	2100 5850 2300 5850
Connection ~ 2100 5850
Text Label 1700 5850 0    50   ~ 0
s_bmbt
Text Label 2300 5550 0    50   ~ 0
Gs_bmbt
Text Label 2300 5700 0    50   ~ 0
Bs_bmbt
Text Label 2300 5850 0    50   ~ 0
Rs_bmbt
Wire Wire Line
	1250 6050 1650 6050
Wire Wire Line
	1650 6050 1650 6250
Connection ~ 1650 6050
Wire Wire Line
	1650 6050 2100 6050
Text Label 1800 6050 0    50   ~ 0
s_bmbt
Text Label 1250 6050 0    50   ~ 0
s_up
Text Label 1650 6250 0    50   ~ 0
C_GND
$EndSCHEMATC
