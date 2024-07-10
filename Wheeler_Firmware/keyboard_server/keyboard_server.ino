#include <Keyboard.h>  

//https://www.arduino.cc/en/Reference/KeyboardModifiers
void setup() {
  Serial.begin(9600);
  Serial.println("ready");         
}


void loop() {
  String line=serial_readLine_b();
  char h=line[0];
  String cmd=line.substring(2, line.length());
  int code=cmd.toInt();
//  Serial.println("h="+String(h)+" code="+String(code) );
  if(h=='p') {
    on_press(code);
  }else if(h=='h') {
    on_hold(code);
  }else if(h=='r') {
    on_release(code);
  }
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
 
/*
*Read a single line from Serial 
*/
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
