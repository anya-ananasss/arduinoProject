#include <TimeLib.h>

#include <stDHT.h>
DHT sens(DHT11);

#define LED_PIN 4
#define BUZZER_PIN 7
#define SENSOR_PIN 9
boolean silentMode = false;
boolean buzzerWorking = false;
void setup() {
  pinMode(LED_PIN, OUTPUT);
  pinMode(BUZZER_PIN, OUTPUT);
  pinMode(SENSOR_PIN, INPUT);


  Serial.begin(57600);
}

void loop() {

  int h = 0;
  int H = 0;
  int t = 0;
  int T = 0;


  if (Serial.available() > 0) {
    String data = Serial.readStringUntil('\n');
    if (data.length() > 0) {
      //работа с данными из java
      int nums[5];
      char *str = strdup(data.c_str());
      char *token = strtok(str, " ");

      int i = 0;
      while (token != NULL) {
        nums[i] = atoi(token);
        token = strtok(NULL, " ");
        i++;
      }
      if (nums[0] == 1) {
        digitalWrite(13, HIGH);
        silentMode = true;
      }
      if (nums[0] == 0) {
        digitalWrite(13, LOW);
        silentMode = false;
      }

      h = nums[1];
      H = nums[2];
      t = nums[3];
      T = nums[4];


      int realH = sens.readHumidity(SENSOR_PIN);
      int realT = sens.readTemperature(SENSOR_PIN);

      if (isConditionBad(realH, realT, h, H, t, T)) {
        digitalWrite(LED_PIN, HIGH);
        if (!silentMode) {
          if (!buzzerWorking) {
            digitalWrite(BUZZER_PIN, !HIGH);
            buzzerWorking = true;
            delay(1000);
            digitalWrite(BUZZER_PIN, !LOW);
          }
        } else {
          digitalWrite(LED_PIN, HIGH);
        }
      } else {
        digitalWrite(LED_PIN, LOW);
        buzzerWorking = false;
      }

      Serial.print(realH);
      Serial.print(" ");
      Serial.println(realT);
      delay(1000);
    } else {
      digitalWrite(LED_PIN, HIGH);
      delay(500);
      digitalWrite(LED_PIN, LOW);
      delay(500);
    }
  } else {
    digitalWrite (BUZZER_PIN, !LOW);
    digitalWrite(LED_PIN, HIGH);
    delay(500);
    digitalWrite(LED_PIN, LOW);
    delay(500);
  }
  //светодиод будет мигать, если произошла ошибка и данные не поступили
}

boolean isConditionBad(int h, int t, int idealHlow, int idealHhigh, int idealTlow, int idealThigh) {
  boolean badConditions = false;
  if (h < idealHlow || h > idealHhigh || t < idealTlow || t > idealThigh) {
    badConditions = true;
  }
  return badConditions;
}
