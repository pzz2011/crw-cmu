#define F_CPU 2000000

#include "led.h"
#include "serial.h"

#include <avr/io.h>
#include <util/delay.h>

int main(void) 
{
  int max = -10000;

  init_led();
  init_serial(BAUD_9600);

  // Enable the TTL on one line
  PORTJ.DIRSET = PIN4_bm | PIN5_bm;
  PORTJ.OUTSET = PIN4_bm;
  PORTJ.OUTCLR = PIN5_bm;

  while(1) {

    // Setup conversion
    PORTA.DIR = 0; // Configure PORTA as input
    ADCA.CTRLA |= 0x1; // Enable adc
    ADCA.CTRLB = ADC_CONMODE_bm | ADC_RESOLUTION_12BIT_gc;
    ADCA.REFCTRL = ADC_REFSEL_INT1V_gc | 0x02;
    ADCA.PRESCALER = ADC_PRESCALER_DIV4_gc; // clk/16
    ADCA.CH0.CTRL = ADC_CH_INPUTMODE_DIFFWGAIN_gc | ADC_CH_GAIN_64X_gc;
    ADCA.CH0.MUXCTRL = ADC_CH_MUXPOS_PIN4_gc | ADC_CH_MUXNEG_PIN0_gc;
    
    // Start conversion on channel 0
    ADCA.CH0.CTRL |= ADC_CH_START_bm;
    while(!ADCA.CH0.INTFLAGS);

    int result = ADCA.CH0RES;

    led_on();
    _delay_ms(200);
//    printf("%d\n", result + 200);
    led_off();
    _delay_ms(200);
  }
}
