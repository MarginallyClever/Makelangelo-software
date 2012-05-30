
#define S1_PIN (A3)
#define S2_PIN (A5)
#define SWITCH_HALF (512)

int switch1,switch2;

//------------------------------------------------------------------------------
static void readSwitches() {
  // get the current switch state
  switch1=analogRead(S1_PIN) > SWITCH_HALF;
  switch2=analogRead(S2_PIN) > SWITCH_HALF;
}


//------------------------------------------------------------------------------
void setup() {
  digitalWrite(S1_PIN,HIGH);
  digitalWrite(S2_PIN,HIGH);
}


//------------------------------------------------------------------------------
void loop() {
  readSwitches();
  
  digitalWrite(13,(switch1&switch2) ? HIGH : LOW );
}


