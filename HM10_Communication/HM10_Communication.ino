/**
 * @file HM10_Communication.ino
 * @author Omegaki113r
 * @brief
 * @version 0.1
 * @date 2021-03-07
 *
 * @copyright Copyright (c) 2021
 *
 */

/**
 * @brief
 *          HM10's RX pin is connected to Pin8 of Arduino Uno
 *          HM10's TX pin is connected to Pin9 of Arduino Uno
 *          To Test whether the code is working i am using nRF Connect app in apple app store
 *
 */

#include <SoftwareSerial.h>
SoftwareSerial HM10_UART(9, 8);

void setup()
{

  Serial.begin(115200); // Start up the serial port so that we can see the incoming data in the serial monitor
  while (!Serial)
  { // Waiting till the serial port starts
    delay(500);
  }
  Serial.println("Serial Port Started");

  HM10_UART.begin(9600); // I have never had success using baudrates other than 9600. since i am using
                         // 9600 baudrate in HM10 we need to set 9600 baudrate in the Serial port as well
  while (!HM10_UART)
  {
    delay(500);
  }
  Serial.println("HM10_UART started");
}

void loop()
{

  String _recievedString = "";
  while (HM10_UART.available())
  {                                                    // continously checking the HM10 to see if there are incoming data
    _recievedString = HM10_UART.readString(); // read the data from HM10 as a String
    Serial.println((_recievedString));
    Serial.flush();
    // HM10_UART.println("packet "+ _recievedString); // send a confirmation message back through HM10
  }
  if (_recievedString != "")
  {
    Serial.println("Recieved: " + _recievedString); // Printout the data to Serial Monitor
  }
  while (Serial.available())
  {
    HM10_UART.print(Serial.readStringUntil('\n'));
  }
  delay(50);
}
