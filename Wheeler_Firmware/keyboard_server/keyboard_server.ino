#include <Wire.h>

#define OV7670_ADDR 0x21

// Camera pins
#define VSYNC_PIN 2
#define HREF_PIN 3
#define PCLK_PIN 4
#define XCLK_PIN 5
#define D0_PIN 6
#define D1_PIN 7
#define D2_PIN 8
#define D3_PIN 9
#define D4_PIN 10
#define D5_PIN 11
#define D6_PIN 12
#define D7_PIN 13

// Wire (I2C) pins
#define SDA_PIN A4
#define SCL_PIN A5

// Image settings
#define IMAGE_WIDTH 160
#define IMAGE_HEIGHT 120

// Frame counter
volatile uint32_t frame_count = 0;
volatile bool frame_flag = false;

void setup() {
  Serial.begin(2000000);
  while (!Serial) {
    ; // Wait for serial port to connect. Needed for native USB port only
  }
  
  Serial.println("OV7670 Initialization Starting");
  
  // Initialize Wire (I2C)
  Wire.begin();
  
  // Set up camera pins
  pinMode(VSYNC_PIN, INPUT);
  pinMode(HREF_PIN, INPUT);
  pinMode(PCLK_PIN, INPUT);
  pinMode(XCLK_PIN, OUTPUT);
  for (int i = 0; i < 8; i++) {
    pinMode(D0_PIN + i, INPUT);
  }
  
  // Set up Wire pins (not strictly necessary as Wire.begin() does this, but included for clarity)
  pinMode(SDA_PIN, INPUT_PULLUP);
  pinMode(SCL_PIN, INPUT_PULLUP);
  
  // Generate clock for OV7670
  // Using Timer2 for 8MHz output on XCLK_PIN (Digital Pin 5)
  TCCR2A = _BV(COM2A1) | _BV(COM2B1) | _BV(WGM21) | _BV(WGM20);
  TCCR2B = _BV(WGM22) | _BV(CS20);
  OCR2A = 1; // 8MHz
  
  Serial.println("Initializing OV7670");
  // Initialize OV7670
  init_OV7670();
  
  // Set up external interrupt for VSYNC
  attachInterrupt(digitalPinToInterrupt(VSYNC_PIN), vsync_handler, RISING);
  
  Serial.println("OV7670 initialization complete");
}

void loop() {
  if (frame_flag) {
    Serial.println("Capturing frame");
    capture_frame();
    frame_flag = false;
  }
}

void vsync_handler() {
  frame_flag = true;
  frame_count++;
}

void capture_frame() {
  // Send start of frame marker
  Serial.write(0xFF);
  
  // Send frame number
  Serial.write((uint8_t*)&frame_count, 4);
  
  // Capture and send frame data line by line
  for (int y = 0; y < IMAGE_HEIGHT; y++) {
    // Wait for start of line
    while (digitalRead(HREF_PIN) == LOW);
    
    for (int x = 0; x < IMAGE_WIDTH; x++) {
      while (digitalRead(PCLK_PIN) == LOW);
      
      // Read pixel data
      uint8_t pixel = 0;
      for (int i = 0; i < 8; i++) {
        pixel |= (digitalRead(D0_PIN + i) << i);
      }
      Serial.write(pixel);
      
      while (digitalRead(PCLK_PIN) == HIGH);
    }
    
    // Wait for end of line
    while (digitalRead(HREF_PIN) == HIGH);
  }
  
  // Send end of frame marker
  Serial.write(0xFE);
  
  // Print debug info
  Serial.print("Frame ");
  Serial.print(frame_count);
  Serial.println(" sent");
}

void init_OV7670() {
  // Reset all registers
  write_register(0x12, 0x80);
  delay(100);
  
  // Set clock prescaler
  write_register(0x11, 0x01);
  
  // Set QVGA resolution
  write_register(0x0c, 0x04);
  write_register(0x3e, 0x19);
  write_register(0x70, 0x3a);
  write_register(0x71, 0x35);
  write_register(0x72, 0x11);
  write_register(0x73, 0xf0);
  write_register(0xa2, 0x02);
  
  // Set YUV422 color space
  write_register(0x12, 0x00);
  write_register(0x8c, 0x00);
  write_register(0x04, 0x00);
  write_register(0x40, 0xd0);
  write_register(0x14, 0x48);
  write_register(0x4f, 0x80);
  write_register(0x50, 0x80);
  write_register(0x51, 0x00);
  write_register(0x52, 0x22);
  write_register(0x53, 0x5e);
  write_register(0x54, 0x80);
  write_register(0x3d, 0x40);
  
  // Additional settings for better image quality
  write_register(0xb0, 0x84);
  write_register(0xb1, 0x0c);
  write_register(0xb2, 0x0e);
  write_register(0xb3, 0x82);
  
  Serial.println("OV7670 registers initialized");
}

void write_register(uint8_t reg, uint8_t value) {
  Wire.beginTransmission(OV7670_ADDR);
  Wire.write(reg);
  Wire.write(value);
  int error = Wire.endTransmission();
  if (error != 0) {
    Serial.print("Error writing register ");
    Serial.print(reg, HEX);
    Serial.print(": ");
    Serial.println(error);
  }å
  delay(1);
}