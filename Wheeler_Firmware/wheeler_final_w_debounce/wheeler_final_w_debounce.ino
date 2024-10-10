#include "Mouse.h"
#include <Keyboard.h> 
#include <EEPROM.h>

// Encoder pins
#define pinA 10 
#define pinB 16   
#define pinA2 6 
#define pinB2 7  
#define pinA3 4   
#define pinB3 5  

// Button pins
#define btn  9
#define btn2 8 

// Mouse mode pin
#define mouse_mode_pin 2 

// Haptic and buzzer pin
#define pin_h 15
#define pin_b 14

#define EEPROM_location_address_mouse_speed 0
#define EEPROM_location_address_mouse_mode 1

// Debounce parameters
#define DEBOUNCE_DELAY 20

int mouse_speed = 3;
int encoderPosCount = 0, encoderPosCount2 = 0, encoderPosCount3 = 0; 
int pinALast, pinALast2, pinALast3, btnLast, btn2Last;  
int aVal, aVal2, aVal3, bVal, bVal2;
int mouse_mode = 0;
int mode_val;

// Debounce variables
unsigned long lastDebounceTime = 0;
unsigned long lastDebounceTimeBtn = 0;
unsigned long lastDebounceTimeBtn2 = 0;
unsigned long lastDebounceTimeMode = 0;

int lastEncoderState = LOW;
int lastEncoderState2 = LOW;
int lastEncoderState3 = LOW;
int lastBtnState = LOW;
int lastBtn2State = LOW;
int lastModeState = LOW;

void init_pins(int pa, int pb) {
  pinMode(pa, INPUT_PULLUP);
  pinMode(pb, INPUT_PULLUP);
}

void setup() {
  Serial.begin(9600);
  init_pins(pinA, pinB);
  init_pins(pinA2, pinB2);
  init_pins(pinA3, pinB3);
  pinMode(btn, INPUT_PULLUP);
  pinMode(btn2, INPUT_PULLUP);
  pinMode(mouse_mode_pin, INPUT_PULLUP); 

  pinMode(pin_h, OUTPUT);
  pinMode(pin_b, OUTPUT);

  pinALast = digitalRead(pinA); 
  pinALast2 = digitalRead(pinA2);    
  pinALast3 = digitalRead(pinA3); 
  Serial.println("Ready");
  beep();
  haptic();
  beep();
  mouse_speed = EEPROM.read(EEPROM_location_address_mouse_speed);
  if(mouse_speed == 0) {
    mouse_speed = 3;
    EEPROM.write(EEPROM_location_address_mouse_speed, mouse_speed);
  }
  Serial.println("Mouse speed is set to: ");
  Serial.print(mouse_speed);
  Serial.println();

  mouse_mode = EEPROM.read(EEPROM_location_address_mouse_mode);
  Serial.println("Mouse mode: ");
  Serial.print(mouse_mode);
  Serial.println();

  Mouse.begin();
}

void loop() {
  encoderPosCount = 0;
  encoderPosCount2 = 0;
  encoderPosCount3 = 0; 
  
  if(Serial.available()) {
    String line = serial_readLine_b();
    if(mouse_mode == 0) {
      if(line == "h") {
        haptic();
      } else if(line == "b") {
        beep();
      }
    }

    char h = line[0];
    String cmd = line.substring(2, line.length());
    int code = cmd.toInt();
    Serial.println("h=" + String(h) + " code=" + String(code));
    if(h == 'p') {
      on_press(code);
    } else if(h == 'h') {
      on_hold(code);
    } else if(h == 'r') {
      on_release(code);
    } else if(h == 'x') {
      Mouse.move(code, 0, 0);
    } else if(h == 'y') {
      Mouse.move(0, code, 0);
    } else if(h == 'l') {
      Mouse.press(MOUSE_LEFT);
      delay(20);
      Mouse.release(MOUSE_LEFT);
    } else if(h == 'r') {
      Mouse.press(MOUSE_RIGHT);
      delay(20);
      Mouse.release(MOUSE_RIGHT);
    }
  }
  
  boolean anychange = false;
  
  // Debounce encoder 1
  int reading = digitalRead(pinA);
  if (reading != lastEncoderState) {
    lastDebounceTime = millis();
  }
  if ((millis() - lastDebounceTime) > DEBOUNCE_DELAY) {
    if (reading != aVal) {
      aVal = reading;
      det_rot();
      anychange = true;
    }
  }
  lastEncoderState = reading;

  // Debounce encoder 2
  reading = digitalRead(pinA2);
  if (reading != lastEncoderState2) {
    lastDebounceTime = millis();
  }
  if ((millis() - lastDebounceTime) > DEBOUNCE_DELAY) {
    if (reading != aVal2) {
      aVal2 = reading;
      det_rot2();
      anychange = true;
    }
  }
  lastEncoderState2 = reading;

  // Debounce encoder 3
  reading = digitalRead(pinA3);
  if (reading != lastEncoderState3) {
    lastDebounceTime = millis();
  }
  if ((millis() - lastDebounceTime) > DEBOUNCE_DELAY) {
    if (reading != aVal3) {
      aVal3 = reading;
      det_rot3();
      anychange = true;
    }
  }
  lastEncoderState3 = reading;

  // Debounce button 1
  reading = digitalRead(btn);
  if (reading != lastBtnState) {
    lastDebounceTimeBtn = millis();
  }
  if ((millis() - lastDebounceTimeBtn) > DEBOUNCE_DELAY) {
    if (reading != bVal) {
      bVal = reading;
      anychange = true;
    }
  }
  lastBtnState = reading;

  // Debounce button 2
  reading = digitalRead(btn2);
  if (reading != lastBtn2State) {
    lastDebounceTimeBtn2 = millis();
  }
  if ((millis() - lastDebounceTimeBtn2) > DEBOUNCE_DELAY) {
    if (reading != bVal2) {
      bVal2 = reading;
      anychange = true;
    }
  }
  lastBtn2State = reading;

  // Debounce mouse mode switch
  reading = digitalRead(mouse_mode_pin);
  if (reading != lastModeState) {
    lastDebounceTimeMode = millis();
  }
  if ((millis() - lastDebounceTimeMode) > DEBOUNCE_DELAY) {
    if (reading != mode_val) {
      mode_val = reading;
      if (mode_val != mouse_mode) {
        mouse_mode = mode_val;
        EEPROM.write(EEPROM_location_address_mouse_mode, mouse_mode);
      }
    }
  }
  lastModeState = reading;

  if(anychange) {
    Serial.println("enc," + String(encoderPosCount) + "," + String(encoderPosCount2) + "," + String(encoderPosCount3) + "," + String(bVal) + "," + String(bVal) + "," + String(bVal2));
  }

  pinALast = aVal;
  pinALast2 = aVal2;
  pinALast3 = aVal3;
  btnLast=bVal;
  btn2Last=bVal2; 
  
  delay(2);
}

void on_hold(int keycode) {
  Keyboard.begin();  
  Keyboard.press(keycode);
  Keyboard.end();
}
void on_release(int keycode) {
  Keyboard.begin();  
  Keyboard.release(keycode); 
  Keyboard.end();
}
void on_press(int keycode) {
  Keyboard.begin();  
  Keyboard.write(keycode);
  Keyboard.end();
}

void beep() {
  digitalWrite(pin_b, HIGH);
  delay(50);
  digitalWrite(pin_b, LOW);
}
void haptic() {
  digitalWrite(pin_h, HIGH);
  delay(70);
  digitalWrite(pin_h, LOW);
}

void det_rot() {
  if (digitalRead(pinB) != aVal) {
       encoderPosCount ++;
     } else {
       encoderPosCount--;
     }
}
void det_rot2() {
  if (digitalRead(pinB2) != aVal2) {
       encoderPosCount2 ++;
     } else {
       encoderPosCount2--;
     }
}
void det_rot3() {
  if (digitalRead(pinB3) != aVal3) {
       encoderPosCount3 ++;
     } else {
       encoderPosCount3--;
     }
}

String serial_readLine_b() {
  String line="";

  while(true) {
    if(Serial.available()) {
       char ch=Serial.read(); 
       if(ch=='\n') {
         break;
       }else {
         line+=ch;
       }
    }
  }
  //line+='\0';
  return line;
}