
#include <avr/interrupt.h>
#include <avr/sleep.h>

int seconds_rpi_shutdown = 45;

bool carAlreadyTurnedOn = false;

int blinkPin = 2; // change to 0 to see on the programmer board.

/* This is the pin that is connected across the relay
 * that is turned on when ACC is hot.
 */
int switchedAccPowerPin = 1;

/* This is connected to GPIO27 on the pi. 
 * This should be tied high when the pi is on, 
 * and pulled to ground when we want to turn it off.
 */
int rpiGpio27 = 3;

/* This is connected to PEN (power enable) on the
 * RPI. When this is grounded, the pi is comatose.
 */
int rpiPen = 4;

/* This is the pi SCL pin on the Pi. When we want to boot
 * up the Pi, we need to ground it briefly. 
 */
int rpiScl = 0;

void turnOnPi() {

  pinMode(rpiScl, OUTPUT);
  
  //PEN Should be low now, so we can setup all the pins.
  digitalWrite(rpiGpio27, HIGH);
  
  digitalWrite(rpiPen, HIGH);
  delay(50);

  digitalWrite(rpiScl, HIGH);
  delay(1000);
  digitalWrite(rpiScl, LOW);
  delay(1000);
  digitalWrite(rpiScl, HIGH); 

  digitalWrite(blinkPin, HIGH);

  pinMode(rpiScl, INPUT);
}

void turnOffPi() {
  digitalWrite(rpiGpio27, LOW);
  delay(1000 * seconds_rpi_shutdown); //Wait for device to shutdown
  digitalWrite(rpiPen, LOW);

  digitalWrite(blinkPin, LOW);

  pinMode(rpiScl, OUTPUT);
}

void setup()
{
  pinMode(blinkPin, OUTPUT);

  pinMode(rpiGpio27, OUTPUT);
  pinMode(rpiPen, OUTPUT);
  pinMode(rpiScl, OUTPUT);

  pinMode(switchedAccPowerPin, INPUT_PULLUP);

  ADCSRA = 0;  // disable ADC

// https://thewanderingengineer.com/2014/08/11/pin-change-interrupts-on-attiny85/
// http://www.nongnu.org/avr-libc/user-manual/group__avr__sleep.html

  digitalWrite(blinkPin, LOW);
  digitalWrite(rpiScl, HIGH); 
}

void updateCarState() {
  bool carIsNowOn = false;
  if (digitalRead(switchedAccPowerPin) == LOW) {
    //The car is now on because the pin is grounded because the relay is closed.
    carIsNowOn = true;
  } else {
    carIsNowOn = false;
  }

  if (carIsNowOn) {
    if (!carAlreadyTurnedOn) {
      turnOnPi();
    }
  } else {
    //Car is now off
    if (carAlreadyTurnedOn) {
      turnOffPi();
    }
  }

  carAlreadyTurnedOn = carIsNowOn;
}

void loop()
{
  updateCarState();

  //Here is where we need to put the car into power-save mode for a bit.
  //TODO
}
