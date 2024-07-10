#include "Mouse.h"
 

void setup() { 
  Mouse.begin();
  Serial.begin(9600);
  //Serial.println("hw mouse: x=3 , y=3, l, r");
  Serial.println("ready");  
}

 
void loop() {
  String line=serial_readLine_b();
  char h=line[0];
  String cmd=line.substring(2, line.length()); 
  int code=cmd.toInt();
  Serial.println("code="+ String(code) );
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
  
    //Mouse.move(xDistance, yDistance, 0);
   

//Mouse.isPressed(MOUSE_LEFT))
//      Mouse.press(MOUSE_LEFT);
//Mouse.isPressed(MOUSE_LEFT))
//   Mouse.release(MOUSE_LEFT);

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
