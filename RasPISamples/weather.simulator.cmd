@setlocal
@echo off
set CP=.\classes
set CP=%CP%;D:\Cloud\pi4j.2013\libs\pi4j-core.jar
set CP=%CP%;D:\Cloud\pi4j.2013\libs\pi4j-device.jar
set CP=%CP%;D:\Cloud\pi4j.2013\libs\pi4j-example.jar
set CP=%CP%;D:\Cloud\pi4j.2013\libs\pi4j-gpio-extension.jar
set CP=%CP%;D:\Cloud\pi4j.2013\libs\pi4j-service.jar
set CP=%CP%;D:\OlivSoft.git\raspberry-pi4j-samples\AdafruitI2C\classes
set CP=%CP%;D:\OlivSoft.git\raspberry-pi4j-samples\ADC\classes
set CP=%CP%;D:\OlivSoft.git\raspberry-pi4j-samples\SevenSegDisplay\classes
set CP=%CP%;D:\OlivSoft.git\raspberry-pi4j-samples\RasPISamples\lib\java_websocket.jar
set CP=%CP%;D:\OlivSoft.git\raspberry-pi4j-samples\RasPISamples\lib\json.jar
set CP=%CP%;D:\OlivSoft.git\raspberry-pi4j-samples\PhoneKeyboard3x4\classes
set CP=%CP%;D:\OlivSoft.git\raspberry-pi4j-samples\Arduino.RaspberryPI\classes
set CP=%CP%;D:\OlivSoft.git\raspberry-pi4j-samples\Camera\classes
set CP=%CP%;D:\OlivSoft.git\raspberry-pi4j-samples\RasPISamples\lib\jansi-1.9.jar
set CP=%CP%;D:\OlivSoft.git\raspberry-pi4j-samples\WeatherStation\classes
::
:: set JAVA_OPTS=-Ddata.logger=weatherstation.logger.DummyLogger
set JAVA_OPTS=-Ddata.logger=weatherstation.logger.MySQLLoggerImpl
::
java -cp %CP% %JAVA_OPTS% weatherstation.ws.HomeWeatherStationSimulator
@endlocal
