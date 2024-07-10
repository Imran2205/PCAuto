int pinA = 4;   
int pinB = 5;   
int pinA2 = 6;   
int pinB2 = 7;   
int pinA3 = 16;   
int pinB3 = 10;  

int btn=8;
int btn2=9; 
  

int encoderPosCount = 0, encoderPosCount2 = 0, encoderPosCount3 = 0; 
int pinALast, pinALast2, pinALast3, btnLast, btn2Last ;  
int aVal, aVal2, aVal3, bVal, bVal2 ;

int pin_h=15, pin_b=14;
void init_pins(int pa, int pb) {
  pinMode (pa,INPUT);
  pinMode (pb,INPUT);
}
 void setup() { 
   init_pins(pinA, pinB);
   init_pins(pinA2, pinB2);
   init_pins(pinA3, pinB3);
   pinMode(btn, INPUT);
   pinMode(btn2, INPUT); 

   pinMode(pin_h, OUTPUT);
   pinMode(pin_b, OUTPUT);
   
   pinALast = digitalRead(pinA); 
   pinALast2 = digitalRead(pinA2);    
   pinALast3 = digitalRead(pinA3); 
   Serial.begin (9600);
   Serial.println("Ready\n");
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
 void loop() {

   if(Serial.available()){
      char ch=Serial.read();
      if(ch=='h') {
        digitalWrite(pin_h, HIGH);
        delay(100);
        digitalWrite(pin_h, LOW);
      }else if(ch=='b') {
        digitalWrite(pin_b, HIGH);
        delay(50);
        digitalWrite(pin_b, LOW);
      }
   }
  
  boolean anychange=false;
  
   aVal = digitalRead(pinA);
   aVal2 = digitalRead(pinA2);
   aVal3 = digitalRead(pinA3);
   bVal=digitalRead(btn);
   bVal2=digitalRead(btn2); 
   if(bVal!=btnLast) { 
      anychange=true;
   }
   if(bVal2!=btn2Last) { 
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
    Serial.println("enc,"+String(encoderPosCount)+","+String(encoderPosCount2)+","+String(encoderPosCount3)+","+String(bVal)+","+String(bVal) +","+String(bVal2) );
   }
   pinALast = aVal;
   pinALast2 = aVal2;
   pinALast3 = aVal3;
   btnLast=bVal;
   btn2Last=bVal2; 
   delay(2);
 } 
