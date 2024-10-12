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

int mouse_speed = 3;
int encoderPosCount = 0, encoderPosCount2 = 0, encoderPosCount3 = 0; 
int pinALast, pinALast2, pinALast3, btnLast, btn2Last, mVal_last;  
int aVal, aVal2, aVal3, bVal, bVal2, mVal;
int mouse_mode=0;
int mode_val;

void init_pins(int pa, int pb) {
  pinMode (pa,INPUT);
  pinMode (pb,INPUT);
}


void setup() {
  Serial.begin(9600);
  init_pins(pinA, pinB);
  init_pins(pinA2, pinB2);
  init_pins(pinA3, pinB3);
  pinMode(btn, INPUT);
  pinMode(btn2, INPUT);
  pinMode(mouse_mode_pin, INPUT_PULLUP); 

  pinMode(pin_h, OUTPUT);
  pinMode(pin_b, OUTPUT);

  pinALast = digitalRead(pinA); 
  pinALast2 = digitalRead(pinA2);    
  pinALast3 = digitalRead(pinA3); 
  // Serial.println("Three coders v2: direction data");
  Serial.println("Ready");
  beep();
  haptic();
  beep();
  mouse_speed = EEPROM.read(EEPROM_location_address_mouse_speed);
  if(mouse_speed==0){
    mouse_speed=3;
    EEPROM.write(EEPROM_location_address_mouse_speed,mouse_speed);
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
  
   if(Serial.available()){
//      char ch=Serial.read();
      String line = serial_readLine_b();
//      Serial.println("ln="+ln);
      // if(mouse_mode==0){
      if(line=="h") {
        haptic();
      }else if(line=="b") {
        beep();
      }
      // }

      char h=line[0];
      String cmd=line.substring(2, line.length());
      int code=cmd.toInt();
      Serial.println("h="+String(h)+" code="+String(code) );
      // if(h=='p') {
      //   on_press(code);
      // }else if(h=='h') {
      //   on_hold(code);
      // }else if(h=='r') {
      //   on_release(code);
      // }
      if(h=='x') {
        Mouse.move(code, 0, 0);
      }else if(h=='y') {
        Mouse.move(0, code, 0);
      }else if(h=='l') {
        Mouse.press(MOUSE_LEFT);
        delay(20);
        Mouse.release(MOUSE_LEFT);
      }else if(h=='r') {
        Mouse.press(MOUSE_RIGHT);
        delay(20);
        Mouse.release(MOUSE_RIGHT);
      }
   }
  
  boolean anychange=false;
  
   aVal = digitalRead(pinA);
   aVal2 = digitalRead(pinA2);
   aVal3 = digitalRead(pinA3);
   bVal = digitalRead(btn);
   bVal2 = digitalRead(btn2); 
   mode_val = digitalRead(mouse_mode_pin); 
   mVal = 0;
   if (bVal == 1 && bVal2 == 1){
    mVal = 1;
   }
   else{
    mVal =0;
   }

   if(mode_val==1){
    if(mode_val!=mouse_mode){
      mouse_mode = 1;
      EEPROM.write(EEPROM_location_address_mouse_mode, mouse_mode);
    }
    // if(mouse_mode==0){
    //   mouse_mode = 1;
    // }
    // else if(mouse_mode==1){
    //   mouse_mode = 0;
    // }
   }
   else{
    if(mode_val!=mouse_mode){
      mouse_mode = 0;
      EEPROM.write(EEPROM_location_address_mouse_mode, mouse_mode);
    }
   }
   

   if(bVal!=btnLast) { 
      anychange=true;
   }
   if(bVal2!=btn2Last) { 
      anychange=true;
   }
   if(mVal!=mVal_last) { 
      anychange=true;
   }
   
   
   if (aVal != pinALast){ 
     det_rot();
//     Serial.print("Encoder Position: ");
//     Serial.println(encoderPosCount);
     anychange=true;
   } 
  if (aVal2 != pinALast2){ 
     det_rot2();
//     Serial.print("Encoder2 Position: ");
//     Serial.println(encoderPosCount2);
     anychange=true;
   } 
   if (aVal3 != pinALast3){ 
     det_rot3();
//     Serial.print("Encoder3 Position: ");
//     Serial.println(encoderPosCount3);
     anychange=true;
   }
   if(anychange) {
    Serial.println("enc,"+String(encoderPosCount)+","+String(encoderPosCount2)+","+String(encoderPosCount3)+","+String(bVal)+","+String(bVal) +","+String(bVal2)+","+String(mVal) );
    // if(mouse_mode==1){
      // Serial.println("enc,"+String(encoderPosCount)+","+String(encoderPosCount2)+","+String(encoderPosCount3)+","+String(bVal)+","+String(bVal) +","+String(bVal2) );
    // }
    // else{
    //   Serial.println();
    //   if(encoderPosCount!=0) {
    //     Mouse.move(encoderPosCount*mouse_speed, 0, 0);
    //   }
    //   if(encoderPosCount2!=0) {
    //     Mouse.move(0, encoderPosCount2*mouse_speed, 0);
    //   }
    //   if(encoderPosCount3!=0) {
    //     mouse_speed = mouse_speed + encoderPosCount3;
    //     if(mouse_speed<1){
    //       mouse_speed = 1;
    //     }
    //     if(mouse_speed>200){
    //       mouse_speed = 200;
    //     }
    //     EEPROM.write(EEPROM_location_address_mouse_speed,mouse_speed);
    //   }
    //   if(bVal==1) {
    //     Mouse.press(MOUSE_LEFT);
    //     delay(20);
    //     Mouse.release(MOUSE_LEFT);
    //   }
    //   if(bVal2==1) {
    //     Mouse.press(MOUSE_RIGHT);
    //     delay(20);
    //     Mouse.release(MOUSE_RIGHT);
    //   }
    // }  
   }
   pinALast = aVal;
   pinALast2 = aVal2;
   pinALast3 = aVal3;
   btnLast=bVal;
   btn2Last=bVal2; 
   mVal_last = mVal;
   delay(2);

  //  if(bVal==1 && bVal2==1){
  //   delay(100);
  //  }
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
