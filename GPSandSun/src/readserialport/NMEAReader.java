package readserialport;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataListener;
import com.pi4j.io.serial.SerialFactory;

public class NMEAReader
{
  public static void main(String args[])
    throws InterruptedException, NumberFormatException
  {
    System.setProperty("deltaT", "67.2810"); // 2014-Jan-01

    int br = Integer.parseInt(System.getProperty("baud.rate", "4800"));
    if (args.length > 0)
    {
      try
      {
        br = Integer.parseInt(args[0]);
      }
      catch (Exception ex)
      {
        System.err.println(ex.getMessage());
      }
    }
    
    System.out.println("Serial Communication.");
    System.out.println(" ... connect using settings: " + Integer.toString(br) +  ", N, 8, 1.");
    System.out.println(" ... data received on serial port should be displayed below.");

    // create an instance of the serial communications class
    final Serial serial = SerialFactory.createInstance();

    // create and register the serial data listener
    serial.addListener(new SerialDataListener()
    {
      @Override
      public void dataReceived(SerialDataEvent event)
      {
        // print out the data received to the console
        System.out.print("Read:\n" + event.getData()); // << and not println
      }
    });
    
    Thread t = Thread.currentThread();
    try
    {
      System.out.println("Opening port [" + Serial.DEFAULT_COM_PORT + "]");
      serial.open(Serial.DEFAULT_COM_PORT, br);

      synchronized (t) { t.wait(); }
    }
    catch (InterruptedException ie)
    {
      ie.printStackTrace();
    }
  }
}