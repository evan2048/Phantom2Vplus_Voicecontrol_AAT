#include<Servo.h>
#include <LiquidCrystal.h>

Servo yawServo;
Servo pitchServo;
LiquidCrystal lcd(13,12,11,8,7,3);  //use pin
String incomeString="";
int yawAngle=0;
int pitchAngle=0;
String lcdRow1String="  by  evan2048  ";
String lcdRow2String="Yaw:000Pitch:000";
String lcdYawString="";
String lcdPitchString="";
float gpsResults[3];  //hold gps distance and bearing
float homeLocationLatitude,homeLocationLongitude,phantomLocationLatitude,phantomLocationLongitude,phantomAltitude,phantomDistance;

void setup()
{
  Serial.begin(115200);  //initial the Serial
  yawServo.attach(9);  //only can attach 9 and 10
  pitchServo.attach(10);
  lcd.begin(16,2);
  lcd.clear();
  delay(1000);
  lcd.setCursor(0, 0);
  lcd.print(lcdRow1String);
  lcd.setCursor(0, 1);
  lcd.print(lcdRow2String);
}

void loop()
{
  if(Serial.available())
  {
    char buffer=Serial.read();
    //incomeString start
    if(buffer=='<')
    {
      incomeString="";
    }
    //incomeString end
    else if(buffer=='>')
    {
      //incomeString define like this:<a0b0c0d0e0>
      //a:homeLocationLatitude     b:homeLocationLongitude
      //c:phantomLocationLatitude  d:phantomLocationLongitude
      //e:phantomAltitude
      //Serial.println(incomeString);
      String homeLocationLatitudeString=incomeString.substring(incomeString.indexOf('a')+1,incomeString.indexOf('b'));
      String homeLocationLongitudeString=incomeString.substring(incomeString.indexOf('b')+1,incomeString.indexOf('c'));
      String phantomLocationLatitudeString=incomeString.substring(incomeString.indexOf('c')+1,incomeString.indexOf('d'));
      String phantomLocationLongitudeString=incomeString.substring(incomeString.indexOf('d')+1,incomeString.indexOf('e'));
      String phantomAltitudeString=incomeString.substring(incomeString.indexOf('e')+1);
      homeLocationLatitude=homeLocationLatitudeString.toFloat();
      homeLocationLongitude=homeLocationLongitudeString.toFloat();
      phantomLocationLatitude=phantomLocationLatitudeString.toFloat();
      phantomLocationLongitude=phantomLocationLongitudeString.toFloat();
      phantomAltitude=phantomAltitudeString.toFloat();
      
      calculateDistanceAndBearing(homeLocationLatitude,homeLocationLongitude,phantomLocationLatitude,phantomLocationLongitude);
      phantomDistance=gpsResults[0];
      yawAngle=(int)gpsResults[1];
      if(yawAngle<0)
      {
        yawAngle+=360;
      }
      pitchAngle=(int)(atan2(phantomAltitude, phantomDistance)/PI*180);
      
      //show angle in lcd
      //calculate yaw value number of char
      if(yawAngle<10)
      {
        lcdYawString="00"+String(yawAngle);
      }
      else if(yawAngle<100)
      {
        lcdYawString="0"+String(yawAngle);
      }
      else
      {
        lcdYawString=String(yawAngle);
      }
      //calculate pitch value number of char
      if(pitchAngle<10)
      {
        lcdPitchString="00"+String(pitchAngle);
      }
      else if(pitchAngle<100)
      {
        lcdPitchString="0"+String(pitchAngle);
      }
      else
      {
        lcdPitchString=String(pitchAngle);
      }
      lcd.setCursor(0, 0);
      lcd.print(lcdRow1String);
      lcd.setCursor(0, 1);
      lcd.print("Yaw:"+lcdYawString+"Pitch:"+lcdPitchString);
      Serial.println("Yaw:"+lcdYawString+"Pitch:"+lcdPitchString);
      
      //income yawAngle:0~360,income pitchAngle:0~90
      if(yawAngle<=180)
      {
        yawAngle=180-yawAngle;
        pitchAngle=180-pitchAngle;
      }
      else if(yawAngle>180)
      {
        yawAngle=360-yawAngle;
      }
      //pitch 180 will hit the bottom
      if(pitchAngle>173)
      {
        pitchAngle=173;
      }
      yawServo.write(yawAngle);
      pitchServo.write(pitchAngle);
    }
    else
    {
      incomeString+=buffer;
    }
  }
}
