#define RECV_DO_FN 'o'

char doReading[5];
static String sensorstring = "";

void initDO() {  
  Serial3.begin(38400);                // Enable serial port
  Serial3.print("\rC\r");              // The command "C" will tell the stamp to take continues readings
}

void updateDO() {
  while (Serial3.available()) {       
  char inchar= (char)Serial3.read();   // Get the char we just received
  sensorstring += inchar;              // Add it to the inputString
       
  if (inchar == '\r') {                //if a string from the Atlas Scientific product has been recived in its entierty
      sensorstring.toCharArray(doReading, 5);
      amarino.send(RECV_DO_FN);
      amarino.send(doReading);
      amarino.sendln();
      sensorstring = "";
      break;
   }
  } 
}
