package adafruiti2c.adc.samples;

import adafruiti2c.adc.AdafruitADS1x15;

public class ComparatorSample
{
  private static boolean go = true;
  
  private final static void setGo(boolean b)
  {
    go = b;
  }
  
  public static void main(String[] args)
  {
    final AdafruitADS1x15 adc = new AdafruitADS1x15(AdafruitADS1x15.ICType.IC_ADS1115);
    Runtime.getRuntime().addShutdownHook(new Thread()
     {
       public void run()
       {
         System.out.println("Stop reading.");
         adc.stopContinuousConversion();
         try { Thread.sleep(250L); } catch (Exception ex) {}
         setGo(false);
       }
     });
    adc.startSingleEndedComparator(AdafruitADS1x15.Channels.CHANNEL_2, 200, 100, 1024, 250);
    while (go)
    {
      System.out.println("Channel 2:" + adc.getLastConversionResults() / 1000.0);
      try { Thread.sleep(250L); } catch (Exception ex) {}
    }
  }
}
