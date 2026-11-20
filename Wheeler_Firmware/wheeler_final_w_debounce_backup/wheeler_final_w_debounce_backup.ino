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
#define btn  3
#define btn2 2 

// Mouse mode pin
#define mouse_mode_pin 8

// Haptic and buzzer pin
#define pin_h 15
#define pin_b 14

#define EEPROM_location_address_mouse_speed 0
#define EEPROM_location_address_mouse_mode 1

// Debounce parameters (buttons and mode switch only; encoders use the state machine below)
#define DEBOUNCE_DELAY 20

// Encoder noise filter
// While a wheel is moving, a step in the opposite direction is only accepted after
// REVERSE_CONFIRM_STEPS consecutive steps that way. This removes single wrong-direction
// steps from worn/bouncy contacts. After REVERSE_IDLE_MS without movement, the first
// step in either direction is accepted immediately, so deliberate reversals are not delayed.
// Wheel direction: 1 = normal, -1 = flipped
#define ENC1_DIR -1
#define ENC2_DIR -1
#define ENC3_DIR -1

#define REVERSE_CONFIRM_STEPS 2
#define REVERSE_IDLE_MS 150

// Half-step quadrature state machine (based on Ben Buxton's rotary encoder table).
// A step is only reported after a valid A/B sequence, so contact bounce on one line
// moves back and forth between neighbouring states without producing a count.
#define R_START       0x0
#define R_CCW_BEGIN   0x1
#define R_CW_BEGIN    0x2
#define R_START_M     0x3
#define R_CW_BEGIN_M  0x4
#define R_CCW_BEGIN_M 0x5
#define DIR_CW  0x10
#define DIR_CCW 0x20

const unsigned char ttable[6][4] = {
  // R_START (11)
  {R_START_M,           R_CW_BEGIN,     R_CCW_BEGIN,  R_START},
  // R_CCW_BEGIN
  {R_START_M | DIR_CCW, R_START,        R_CCW_BEGIN,  R_START},
  // R_CW_BEGIN
  {R_START_M | DIR_CW,  R_CW_BEGIN,     R_START,      R_START},
  // R_START_M (00)
  {R_START_M,           R_CCW_BEGIN_M,  R_CW_BEGIN_M, R_START},
  // R_CW_BEGIN_M
  {R_START_M,           R_START_M,      R_CW_BEGIN_M, R_START | DIR_CW},
  // R_CCW_BEGIN_M
  {R_START_M,           R_CCW_BEGIN_M,  R_START_M,    R_START | DIR_CCW},
};

struct Encoder {
  uint8_t a, b;
  unsigned char state;
  int lastDir;
  int pending;
  unsigned long lastStepMs;
};

Encoder enc1 = {pinA,  pinB,  R_START, 0, 0, 0};
Encoder enc2 = {pinA2, pinB2, R_START, 0, 0, 0};
Encoder enc3 = {pinA3, pinB3, R_START, 0, 0, 0};

int mouse_speed = 3;
int encoderPosCount = 0, encoderPosCount2 = 0, encoderPosCount3 = 0; 
int btnLast, btn2Last;
int bVal, bVal2;
int mouse_mode = 0;
int mode_val;

// Debounce variables
unsigned long lastDebounceTimeBtn = 0;
unsigned long lastDebounceTimeBtn2 = 0;
unsigned long lastDebounceTimeMode = 0;

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
#if mouse_mode_pin != btn2
  pinMode(mouse_mode_pin, INPUT_PULLUP);
#endif

  pinMode(pin_h, OUTPUT);
  pinMode(pin_b, OUTPUT);

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
  
  update_outputs();

  if(Serial.available()) {
    String line = serial_readLine_b();
    if(line == "h" || line == "b") {
      // Always honour beep/haptic; PCAuto decides when to send them.
      if(line == "h") {
        haptic();
      } else {
        beep();
      }
    } else if(line == "l") {
      Mouse.click(MOUSE_LEFT);
    } else if(line == "r") {
      Mouse.click(MOUSE_RIGHT);
    } else {
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
      }
    }
  }
  
  boolean anychange = false;
  
  // Encoders
  encoderPosCount = ENC1_DIR * read_encoder(enc1);
  encoderPosCount2 = ENC2_DIR * read_encoder(enc2);
  encoderPosCount3 = ENC3_DIR * read_encoder(enc3);
  if (encoderPosCount != 0 || encoderPosCount2 != 0 || encoderPosCount3 != 0) {
    anychange = true;
  }

  // Debounce button 1
  int reading = digitalRead(btn);
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

#if mouse_mode_pin != btn2
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
#endif

  if(anychange) {
    Serial.println("enc," + String(encoderPosCount) + "," + String(encoderPosCount2) + "," + String(encoderPosCount3) + "," + String(bVal) + "," + String(bVal) + "," + String(bVal2));
  }

  btnLast=bVal;
  btn2Last=bVal2; 
  
  delay(1);
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

// Beep and haptic are non-blocking so the encoders keep being read while they run.
#define BEEP_MS 50
#define HAPTIC_MS 250
unsigned long beep_until = 0;
unsigned long haptic_until = 0;

void beep() {
  digitalWrite(pin_b, HIGH);
  beep_until = millis() + BEEP_MS;
}
void haptic() {
  digitalWrite(pin_h, HIGH);
  haptic_until = millis() + HAPTIC_MS;
}
void update_outputs() {
  unsigned long now = millis();
  if(beep_until && (long)(now - beep_until) >= 0) {
    digitalWrite(pin_b, LOW);
    beep_until = 0;
  }
  if(haptic_until && (long)(now - haptic_until) >= 0) {
    digitalWrite(pin_h, LOW);
    haptic_until = 0;
  }
}

// Returns +1, -1 or 0 for one encoder. +1 matches the old det_rot() direction.
int read_encoder(Encoder &e) {
  unsigned char pins = (digitalRead(e.b) << 1) | digitalRead(e.a);
  e.state = ttable[e.state & 0x0f][pins];
  unsigned char result = e.state & 0x30;
  if (!result) {
    return 0;
  }
  int dir = (result == DIR_CW) ? 1 : -1;

  unsigned long now = millis();
  boolean idle = (now - e.lastStepMs) > REVERSE_IDLE_MS;
  e.lastStepMs = now;

  if (dir == e.lastDir || e.lastDir == 0 || idle) {
    e.lastDir = dir;
    e.pending = 0;
    return dir;
  }

  // Direction flipped while moving: wait for confirmation before accepting it.
  e.pending++;
  if (e.pending >= REVERSE_CONFIRM_STEPS) {
    e.lastDir = dir;
    e.pending = 0;
    return dir;
  }
  return 0;
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