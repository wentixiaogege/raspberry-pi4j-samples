package raspisamples.log.net;

import adafruiti2c.sensor.AdafruitBMP180;

import adafruiti2c.sensor.AdafruitHTU21DF;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import java.text.SimpleDateFormat;

import java.util.Date;

import org.json.JSONObject;

import raspisamples.util.HTTPClient;

/**
 * Log weather data with php/MySQL over the net
 */
public class WeatherDataLogging
{
  private final static String LOGGER_URL = "http://donpedro.lediouris.net/php/raspi/insert.php"; // ?board=OlivRPi1&sensor=BMP180&type=TEMPERATURE&data=24
  private final static String BMP_SENSOR_ID = "BMP180";
  private final static String HUM_SENSOR_ID = "HTU21D-F";
  private final static String TEMPERATURE = "TEMPERATURE";
  private final static String PRESSURE    = "PRESSURE";
  private final static String HUMIDITY    = "HUMIDITY";

  private static String boardID = "OlivRPi1";
  private static long waitTime  = 10000L;
  private static String sessionID = "XX";
  static 
  {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    sessionID = sdf.format(new Date());
  }

  private final static String BOARD_PRM = "-board";
  private final static String WAIT_PRM  = "-wait";
  private final static String SESS_PRM  = "-sess";
  private final static String HELP_PRM  = "-help";
  
  protected static void waitfor(long howMuch)
  {
    try { Thread.sleep(howMuch); } catch (InterruptedException ie) { ie.printStackTrace(); }
  }
  
  private static void processPrm(String[] args)
  {
    for (int i=0; i<args.length; i++)
    {
      if (BOARD_PRM.equals(args[i]))
        boardID = args[i + 1];
      else if (WAIT_PRM.equals(args[i]))
      {
        try
        {
          waitTime = 1000L * Integer.parseInt(args[i + 1]);
        }
        catch (Exception ex) 
        {
          ex.printStackTrace();
        }
      }
      else if (SESS_PRM.equals(args[i]))
        sessionID = args[i + 1];
      else if (HELP_PRM.equals(args[i]))
      {
        System.out.println("Usage is:");
        System.out.println("  java raspisamples.log.net.WeatherDataLogging -board <BoardID> -sess <Session ID> -wait <time-in-sec> -help ");
        System.out.println("  <BoardID> is your board ID (default is OlivRPi1)");
        System.out.println("  <Session ID> identifies your logging session (default current date YYYY-MM-DD)");
        System.out.println("  <time-in-sec> is the amount of seconds between logs (default is 10)");
        System.out.println();
        System.out.println("Logging data for board [" + boardID + "]");
        System.out.println("Logging data every " + Long.toString(waitTime / 1000) + " s. Session ID:" + sessionID);
        System.exit(0);
      }
    }
  }
  
  public static void main(String[] args)
  {
    processPrm(args);

    System.out.println("Logging data for [" + boardID + "], every " + Long.toString(waitTime / 1000) + " s.");
    
    final NumberFormat NF = new DecimalFormat("##00.00");
    AdafruitBMP180 bmpSensor = new AdafruitBMP180();
    float press = 0;
    float temp  = 0;
    double alt  = 0;
    AdafruitHTU21DF humSensor = new AdafruitHTU21DF();
    float hum  = 0;
    
    try
    {
      if (!humSensor.begin())
      {
        System.out.println("Sensor not found!");        
        System.exit(1);
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      System.exit(1);
    }

    Runtime.getRuntime().addShutdownHook(new Thread()
                                         {
                                           public void run()
                                           {
                                             System.out.println("\nBye now.");
                                           }
                                         });

    while (true)
    {
      try { press = bmpSensor.readPressure(); } 
      catch (Exception ex) 
      { 
        System.err.println(ex.getMessage()); 
        ex.printStackTrace();
      }
      bmpSensor.setStandardSeaLevelPressure((int)press); // As we ARE at the sea level (in San Francisco).
      try { alt = bmpSensor.readAltitude(); } 
      catch (Exception ex) 
      { 
        System.err.println(ex.getMessage()); 
        ex.printStackTrace();
      }
      try { temp = bmpSensor.readTemperature(); } 
      catch (Exception ex) 
      { 
        System.err.println(ex.getMessage()); 
        ex.printStackTrace();
      }
      try { hum = humSensor.readHumidity(); } 
      catch (Exception ex) 
      { 
        System.err.println(ex.getMessage()); 
        ex.printStackTrace();
      }

      System.out.println("At " + new Date().toString());
      System.out.println("Temperature: " + NF.format(temp) + " C");
      System.out.println("Pressure   : " + NF.format(press / 100) + " hPa");
      System.out.println("Altitude   : " + NF.format(alt) + " m");
      System.out.println("Humidity   : " + NF.format(hum) + " %");
      
      // Log here
      try
      {
        String url = LOGGER_URL + "?board=" + boardID + "&session=" + sessionID + "&sensor=" + BMP_SENSOR_ID + "&type=" + TEMPERATURE + "&data=" + NF.format(temp);
        String response = HTTPClient.getContent(url);
        JSONObject json = new JSONObject(response);
        System.out.println("Returned\n" + json.toString(2));
        try { Thread.sleep(1000); } catch (Exception ex) { ex.printStackTrace(); } // To avoid duplicate PK
        url = LOGGER_URL + "?board=" + boardID + "&session=" + sessionID + "&sensor=" + BMP_SENSOR_ID + "&type=" + PRESSURE + "&data=" + NF.format(press / 100);
        response = HTTPClient.getContent(url);
        json = new JSONObject(response);
        System.out.println("Returned\n" + json.toString(2));
        try { Thread.sleep(1000); } catch (Exception ex) { ex.printStackTrace(); } // To avoid duplicate PK
        url = LOGGER_URL + "?board=" + boardID + "&session=" + sessionID + "&sensor=" + HUM_SENSOR_ID + "&type=" + HUMIDITY + "&data=" + NF.format(hum);
        response = HTTPClient.getContent(url);
        json = new JSONObject(response);
        System.out.println("Returned\n" + json.toString(2));
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
      waitfor(waitTime);
    }
  }
}