package weatherstation;

import adafruiti2c.adc.AdafruitADS1x15;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.wiringpi.Gpio;

import java.text.DecimalFormat;
import java.text.Format;

import org.omg.SendingContext.RunTime;

import weatherstation.utils.Utilities;

public class SDLWeather80422
{
  final GpioController gpio = GpioFactory.getInstance();
  
  public enum AdcMode
  {
    SDL_MODE_INTERNAL_AD,
    SDL_MODE_I2C_ADS1015
  }

  // sample mode means return immediately.  
  // The wind speed is averaged at sampleTime or when you ask, whichever is longer.
  // Delay mode means to wait for sampleTime and the average after that time.
  public enum SdlMode
  {
    SAMPLE,
    DELAY
  }

  private final static double WIND_FACTOR = 2.400;

  private int currentWindCount = 0;
  private int currentRainCount = 0;
  private long shortestWindTime = 0;
  
  private long lastWindPing = 0;

  private GpioPinDigitalInput pinAnem;
  private GpioPinDigitalInput pinRain;
  private AdcMode ADMode = AdcMode.SDL_MODE_I2C_ADS1015;

  private double currentWindSpeed     = 0.0;
  private double currentWindDirection = 0.0;

  private long lastWindTime = 0;
                   
  private int sampleTime = 5;
  private SdlMode selectedMode = SdlMode.SAMPLE;
  private long startSampleTime = 0L;

  private int currentRainMin = 0;
  private int lastRainTime = 0;

  private AdafruitADS1x15 ads1015 = null;
  private final static AdafruitADS1x15.ICType ADC_TYPE = AdafruitADS1x15.ICType.IC_ADS1015;
  private int gain = 6144;
  private int sps  =  250;

  public SDLWeather80422()
  {
    super();
  }
  
  public void init(Pin anemo, Pin rain, AdcMode ADMode)
  {
    this.pinAnem = gpio.provisionDigitalInputPin(anemo, "Anemometer");
    this.pinRain = gpio.provisionDigitalInputPin(rain,  "Rainmeter");
    
//  Gpio.add_event_detect(pinAnem, GPIO.RISING, callback=self.serviceInterruptAnem, bouncetime=300)  
//  GPIO.add_event_detect(pinRain, GPIO.RISING, callback=self.serviceInterruptRain, bouncetime=300)  
       
    this.pinAnem.addListener(new GpioPinListenerDigital() 
      {
        @Override
        public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) 
        {
          if (event.getState().isHigh() && (System.currentTimeMillis() - lastWindPing) > 300) // bouncetime
          {
            long currentTime = Utilities.currentTimeMicros() - lastWindTime;
            lastWindTime = Utilities.currentTimeMicros();
            if (currentTime > 1000) // debounce
            {
              currentWindCount += 1;
              if (currentTime < shortestWindTime)
                shortestWindTime = currentTime;
            }
            lastWindPing = System.currentTimeMillis();
    //      System.out.println(" --> GPIO pin state changed: " + System.currentTimeMillis() + ", " + event.getPin() + " = " + event.getState());
          }
        }
      });
    
    this.pinRain.addListener(new GpioPinListenerDigital() 
      {
        @Override
        public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) 
        {
          System.out.println(" --> GPIO pin state changed: " + System.currentTimeMillis() + ", " + event.getPin() + " = " + event.getState());
        }
      });

    this.ads1015 = new AdafruitADS1x15(ADC_TYPE);
    this.ADMode = ADMode;
  }
  
  // Wind Direction Routines
  public float getCurrentWindDirection()
  {
    double direction = Utilities.voltageToDegrees(getCurrentWindDirectionVoltage(), this.currentWindDirection);
    return (float)direction;
  }

  public double getCurrentWindDirectionVoltage()
  {
    double voltage = 0f;
    if (this.ADMode == AdcMode.SDL_MODE_I2C_ADS1015)
    {
      float value = ads1015.readADCSingleEnded(AdafruitADS1x15.Channels.CHANNEL_1, 
                                               this.gain, 
                                               this.sps); // AIN1 wired to wind vane on WeatherPiArduino
//    System.out.println("Voltage Value:" + value);
      voltage = value / 1000f;
    }
    else
    {
      // user internal A/D converter
      voltage = 0.0f;
    }
    return voltage;
  }

  public void setWindMode(SdlMode selectedMode, int sampleTime) // time in seconds 
  {
    this.sampleTime = sampleTime;
    this.selectedMode = selectedMode;
    if (this.selectedMode == SdlMode.SAMPLE)
      this.startWindSample(this.sampleTime);
  }
  
  public void startWindSample(int sampleTime)
  {
    this.startSampleTime = Utilities.currentTimeMicros();
    this.sampleTime = sampleTime;
  }
  
  public double getCurrentWindSpeedWhenSampling()
  {
    double compareValue = this.sampleTime * 1000000;
    if ((Utilities.currentTimeMicros() - this.startSampleTime) >= compareValue)
    {                
      // sample time exceeded, calculate currentWindSpeed
      long timeSpan = (Utilities.currentTimeMicros() - this.startSampleTime);
      this.currentWindSpeed = (float)((this.currentWindCount)/(float)timeSpan) * WIND_FACTOR * 1000000.0;
      /*
      System.out.printf("SDL_CWS = %f, shortestWindTime = %d, CWCount=%d TPS=%f\n", 
                        this.currentWindSpeed,
                        this.shortestWindTime, 
                        this.currentWindCount, 
                        (float)this.currentWindCount/(float)this.sampleTime );
      */
      this.currentWindCount = 0;
      this.startSampleTime = Utilities.currentTimeMicros();
    }
    return this.currentWindSpeed;
  }

  public double currentWindSpeed()
  {
    if (this.selectedMode == SdlMode.SAMPLE)
      this.currentWindSpeed = this.getCurrentWindSpeedWhenSampling();
    else
    {
      // km/h * 1000 msec
      this.currentWindCount = 0;
      try { Thread.sleep(Math.round(this.sampleTime * 1000L)); } catch (Exception ex) {}
      this.currentWindSpeed = ((float)(this.currentWindCount)/(float)(this.sampleTime)) * WIND_FACTOR;
    }
    return this.currentWindSpeed;
  }

  public void resetWindGust()
  {
    this.shortestWindTime = Long.MAX_VALUE;
  }
  
  public double getWindGust()
  {
    long latestTime = this.shortestWindTime;
    this.shortestWindTime = Long.MAX_VALUE;
    double time = latestTime / 1000000d;  // in microseconds
//  System.out.println("WindGust: Latest:" + latestTime + ", time:" + time);
    if (time == 0d)
      return 0;
    else
      return (1.0 / time) * WIND_FACTOR;
  }

  public void shutdown()
  {
    gpio.shutdown();
  }
  
  public static double toMPH(double val)
  {
    return val / 1.609;
  }
  
  public static double toKnots(double val)
  {
    return val / 1.852;
  }
  
  private final static Format SPEED_FMT = new DecimalFormat("##0.00");
  private final static Format VOLTS_FMT = new DecimalFormat("##0.000");
  private final static Format DIR_FMT   = new DecimalFormat("##0.0");
  
  // Sample main, for tests
  private static boolean go = true;
  public static void main(String[] args)
  {
    Runtime.getRuntime().addShutdownHook(new Thread()
     {
       public void run()
       {
         System.out.println("\nUser interru[pted.");
         go = false;
         try { Thread.sleep(1100L); } catch (Exception ex) { ex.printStackTrace(); }
       }
     });
      
    SDLWeather80422 weatherStation = new SDLWeather80422();
    final Pin ANEMOMETER_PIN = RaspiPin.GPIO_16; // <- WiringPi number. GPIO 15, #10
    final Pin RAIN_PIN       = RaspiPin.GPIO_01; // <- WiringPi number. GPIO 18, #12
    weatherStation.init(ANEMOMETER_PIN, RAIN_PIN, AdcMode.SDL_MODE_I2C_ADS1015);
    weatherStation.setWindMode(SdlMode.SAMPLE, 5);
    
    while (go)
    {
      double ws = weatherStation.currentWindSpeed();
      double wg = weatherStation.getWindGust();
      float wd = weatherStation.getCurrentWindDirection();
      double volts = weatherStation.getCurrentWindDirectionVoltage();
      
      System.out.println("Wind : Dir=" + DIR_FMT.format(wd) + "\272, (" + VOLTS_FMT.format(volts) + " V) Speed:" + 
                                         SPEED_FMT.format(toKnots(ws)) + " kts, Gust:" + 
                                         SPEED_FMT.format(toKnots(wg)) + " kts");
      try { Thread.sleep(1000L); } catch (Exception ex) {}
    }
    weatherStation.shutdown();
  }
}
