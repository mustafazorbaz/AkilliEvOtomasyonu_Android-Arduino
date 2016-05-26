#include <Servo.h>  /* Servo kutuphanesi projeye dahil edildi */
Servo servoNesnesi;  /* servo motor nesnesi yaratildi */
int ledK=2,ledM=3,ledS=4,role=5,servo=9;
int Buzzer = 8;

int deger='0';
int sure=30;

void setup() {                
  Serial.begin(9600);
  
  pinMode(ledK, OUTPUT);  
  pinMode(ledM, OUTPUT);
  pinMode(ledS, OUTPUT);  
  pinMode(role, OUTPUT);
  
  digitalWrite(ledK,LOW );  
  digitalWrite(ledM,LOW );
  digitalWrite(ledS,LOW );

  servoNesnesi.attach(servo);
  servoNesnesi.write(0);
}

void f()
{
    delay(sure);
    tone(Buzzer, 4000);
    delay(sure);
    noTone(Buzzer);
    delay(sure);
}

void LedIslem(int port,int durum)
{
     if(durum %2==1)
     {
        digitalWrite(port,HIGH );
     }
     else{
        digitalWrite(port,LOW );
     }
     f(); 
}
void loop() 
{
  if(Serial.available()>0)
  {  
     deger=Serial.read();
    if(deger=='1' || deger=='2')
     {
         LedIslem(ledS,deger);
     }
    else if(deger=='3' || deger=='4')
      {
          LedIslem(ledK,deger);
      }
    else if(deger=='5' || deger=='6')
     {
        LedIslem(ledM,deger);
     }
    else if(deger =='7' || deger =='8')
     {
        LedIslem(role,deger);
     }
     else if(deger =='9')
     {
        servoNesnesi.write(360);  /* Motorun mili 100. dereceye donuyor */
         delay(100);
     }
     else if(deger =='0')
     {
        servoNesnesi.write(0);  /* Motorun mili 100. dereceye donuyor */
         delay(100);
     }
  }
}
