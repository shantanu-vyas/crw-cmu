
int ledPin = 13; // LED connected to digital pin 13

void setup()  
{
  delay(1000);

  // Setup the LED
  pinMode(ledPin, OUTPUT);

  // Open the bluetooth device at 38400 (the AT config baud rate)
  Serial.begin(38400);
  Serial.flush();
  
  // Confirm that we are connected first (send plain AT, wait for response)
  Serial.flush();
  Serial.print("AT\r\n");
  digitalWrite(ledPin, LOW);

  while (Serial.available() <= 0);
  if (Serial.read() == 'O') {
    while (Serial.available() <= 0);
    if (Serial.read() == 'K') {
      digitalWrite(ledPin, HIGH);
    }
  }

  // Now send renaming command
  Serial.flush();
  Serial.print("AT+NAME=AIRBOAT-HW\r\n");
  digitalWrite(ledPin, LOW);
  
  while (Serial.available() <= 0);
  if (Serial.read() == 'O') {
    while (Serial.available() <= 0);
    if (Serial.read() == 'K') {
      digitalWrite(ledPin, HIGH);
    }
  }

  // Now send baud rate command
  Serial.flush();
  Serial.print("AT+UART=115200,0,0\r\n");
  digitalWrite(ledPin, LOW);

  while (Serial.available() <= 0);
  if (Serial.read() == 'O') {
    while (Serial.available() <= 0);
    if (Serial.read() == 'K') {
      digitalWrite(ledPin, HIGH);
    }
  }

  // At this point, the LED should be constantly on.  This indicates
  // a successful update.  Now restart the port at 115kbaud and talk.  
  Serial.end();
  Serial.begin(115200);
}

void loop() 
{
  delay(500); 
  Serial.print("PING");
  delay(500); 
}
