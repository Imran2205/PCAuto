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

// Buttons are wired active-HIGH with external pull-downs (same as mouse_hid_combined_interp):
// pressed reads HIGH. If your buttons switch to GND instead, use INPUT_PULLUP and LOW.
#define BTN_PIN_MODE INPUT
#define BTN_PRESSED  HIGH

// Long press: held this long (and not part of a both-button press) -> reported once as e1/e2
#define LONG_PRESS_MS 1000
// Short haptic pulse when a long press is recognised, so the user knows they can let go
#define LONG_PRESS_HAPTIC 1

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
#define ENC1_DIR 1
#define ENC2_DIR 1
#define ENC3_DIR 1

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
int mouse_mode = 0;
int mode_val;

// Debounce variables
unsigned long lastDebounceTimeMode = 0;
int lastModeState = LOW;

struct Button {
  uint8_t pin;
  int pressed;              // debounced state: 1 = pressed
  int lastReading;
  unsigned long lastDebounceMs;
  unsigned long pressStartMs;
  boolean longFired;        // long press already reported for this press
  boolean blocked;          // part of a both-button press: no long press for this press
};

Button button1 = {btn,  0, !BTN_PRESSED, 0, 0, false, false};
Button button2 = {btn2, 0, !BTN_PRESSED, 0, 0, false, false};
int mVal = 0;                    // 1 from both buttons pressed until both are released
int eVal1 = 0, eVal2 = 0;        // 1 in the line where a long press is recognised

void init_pins(int pa, int pb) {
  pinMode(pa, INPUT_PULLUP);
  pinMode(pb, INPUT_PULLUP);
}

void setup() {
  Serial.begin(9600);
  init_pins(pinA, pinB);
  init_pins(pinA2, pinB2);
  init_pins(pinA3, pinB3);
  pinMode(btn, BTN_PIN_MODE);
  pinMode(btn2, BTN_PIN_MODE);
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
  Keyboard.begin();
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
    } else if(line == "a") {
      Keyboard.releaseAll();
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

  // Buttons
  if (update_button(button1)) anychange = true;
  if (update_button(button2)) anychange = true;

  // Both buttons: starts when both are held and stays on until BOTH are released,
  // so a brief slip of one finger does not end and restart it.
  if (!mVal && button1.pressed && button2.pressed) {
    mVal = 1;
    anychange = true;
    button1.blocked = true;
    button2.blocked = true;
  } else if (mVal && !button1.pressed && !button2.pressed) {
    mVal = 0;
    anychange = true;
  }

  // Long presses
  if (check_long_press(button1)) {
    eVal1 = 1;
    anychange = true;
  }
  if (check_long_press(button2)) {
    eVal2 = 1;
    anychange = true;
  }

#if mouse_mode_pin != btn2
  // Debounce mouse mode switch
  int reading = digitalRead(mouse_mode_pin);
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

  // enc,wheel1,wheel2,wheel3,button1,button2,both,long1,long2  (same layout as mouse_hid_combined_interp)
  if(anychange) {
    Serial.println("enc," + String(encoderPosCount) + "," + String(encoderPosCount2) + "," + String(encoderPosCount3) + "," + String(button1.pressed) + "," + String(button2.pressed) + "," + String(mVal) + "," + String(eVal1) + "," + String(eVal2));
    eVal1 = 0;
    eVal2 = 0;
  }
  
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

// Debounces one button. Returns true when its pressed state changed.
boolean update_button(Button &bt) {
  int reading = digitalRead(bt.pin);
  unsigned long now = millis();
  if (reading != bt.lastReading) {
    bt.lastDebounceMs = now;
  }
  bt.lastReading = reading;
  if ((now - bt.lastDebounceMs) <= DEBOUNCE_DELAY) {
    return false;
  }
  int pressed = (reading == BTN_PRESSED) ? 1 : 0;
  if (pressed == bt.pressed) {
    return false;
  }
  bt.pressed = pressed;
  if (pressed) {
    bt.pressStartMs = now;
    bt.longFired = false;
    bt.blocked = (mVal == 1);  //re-pressed during a both-button press: still part of it
  }
  return true;
}

// True once per press, when the button has been held LONG_PRESS_MS on its own.
boolean check_long_press(Button &bt) {
  if (!bt.pressed || bt.blocked || bt.longFired) {
    return false;
  }
  if (millis() - bt.pressStartMs < LONG_PRESS_MS) {
    return false;
  }
  bt.longFired = true;
#if LONG_PRESS_HAPTIC
  haptic();
#endif
  return true;
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