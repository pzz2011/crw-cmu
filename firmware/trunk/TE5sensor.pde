/**
 * 5TE Sensor - EC, Water, Temperature
 *
 * Contains control and update code interfacing with an 5TE sensor
 *
 */

#include "pt.h"

// Define the char code for the Amarino callback
#define RECV_TE_FN 's'

#define SENSOR_RX_PIN 19
#define SENSOR_PWR_PIN 12

static int d, c, t;
static char type;
static int crc = 0;
static int output = 0;

static struct pt teUpdatePt, teReadPt;

void initTE()  {
  Serial1.begin(115200);
  PT_INIT(&teUpdatePt);
  PT_INIT(&teReadPt);
}

// Reads a single ASCII plaintext integer from the serial stream.
// (Note: also reads the first terminating character after integer)
static int readInt(struct pt *pt) {
  PT_BEGIN(pt);
  
  while(1) {
    
    // Wait for incoming data
    PT_WAIT_UNTIL(pt, Serial1.available() > 0);
    byte val = Serial1.read();

    // Increment checksum, exit on termination character
    crc += val;    
    if (val < '0' || val > '9') break;

    // Shift data and add new digit
    output *= 10;
    output += (val - '0');
  }
  
  PT_END(pt);
}

// Converts from raw 5TE sensor value to a floating point Celsius temperature.
float toTemp(const int &rawTemp) {
  return (float)(rawTemp - 400)/10.0;
}

// Converts from raw 5TE sensor value to a floating point mS/cm (dS/m) conductivity.
float toConductivity(const int &rawCond) {
  return ((float)rawCond) / 100.0;
}

// Converts from raw 5TE sensor value to dielectric, as specified in datasheet.
float toDielectric(const int &rawDielectric) {
   return ((float) rawDielectric) / 50.0;
}

// Wrapper function that will call the pseudothreaded code
void updateTE() {
  //teUpdateThread(&teUpdatePt);
}

// Powers up and reads the sensor values from a 5TE environmental sensor, 
// and checks the resulting checksum for validity.  If the data is invalid,
// all values will return zero.
static int teUpdateThread(struct pt *pt)
{
  PT_BEGIN(pt);

  while(1)
  {  
    // Zero out checksum
    crc = 0;
    
    // Turn on sensor
    digitalWrite(SENSOR_PWR_PIN, HIGH);
  
    // Wait for power-up sequence (15ms high)  
    delay(5);
    PT_WAIT_UNTIL(pt, !digitalRead(SENSOR_RX_PIN)); // Wait for HIGH.                      
    PT_WAIT_UNTIL(pt, digitalRead(SENSOR_RX_PIN));  // Wait for LOW.
  
    // Read data values
    readInt(&teReadPt);
    d = output;
    readInt(&teReadPt);
    c = output;
    readInt(&teReadPt);
    t = output;
    
    // Wait for data, then read sensor type ('z' or 'x')
    PT_WAIT_UNTIL(pt, Serial1.available() > 0);
    type = Serial1.read();
    crc += type;
    
    // Verify sensor checksum
    PT_WAIT_UNTIL(pt, Serial1.available() > 0);
    byte checksum = Serial1.read();
    if (checksum != (crc % 64 + 32)) {
      d = 0;
      c = 0;
      t = 0;
      type = '/0';
    }
  
    // Turn off sensor
    digitalWrite(SENSOR_PWR_PIN, LOW);
  
    // Convert and output the returned values
    amarino.send(RECV_TE_FN);
    amarino.send(toDielectric(d));
    amarino.send(toConductivity(c));
    amarino.send(toTemp(t));
    amarino.sendln();
  }
  
  PT_END(pt)
}


