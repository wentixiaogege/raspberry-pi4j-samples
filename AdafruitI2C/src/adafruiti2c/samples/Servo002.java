package adafruiti2c.samples;

import adafruiti2c.servo.AdafruitPCA9685;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/*
 * Standard servo
 * TowerPro SG-5010
 * 
 * Enter the angle interactively, and see for yourself.
 */
public class Servo002
{
  private static final BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

  public static String userInput(String prompt)
  {
    String retString = "";
    System.err.print(prompt);
    try
    {
      retString = stdin.readLine();
    }
    catch(Exception e)
    {
      System.out.println(e);
      String s;
      try
      {
        s = userInput("<Oooch/>");
      }
      catch(Exception exception) 
      {
        exception.printStackTrace();
      }
    }
    return retString;
  }

  public static void main(String[] args)
  {
    AdafruitPCA9685 servoBoard = new AdafruitPCA9685();
    int freq = 60;
    servoBoard.setPWMFreq(freq); // Set frequency in Hz
    
    // For the TowerPro SG-5010
    int servoMin = 130;   // -90 deg
    int servoMax = 615;   // +90 deg

    final int STANDARD_SERVO_CHANNEL   = 15;
    
    int servo = STANDARD_SERVO_CHANNEL;
    
    boolean keepGoing = true;
    System.out.println("[" + servoMin + ", " + servoMax + "]");
    System.out.println("Enter 'quit' to exit.");
    while (keepGoing)
    {
      String s1 = userInput("Angle in degrees (0: middle, -90: full left, 90: full right) ? > ");
      if ("QUIT".equalsIgnoreCase(s1))
        keepGoing = false;
      else
      {
        try
        {
          int angle = Integer.parseInt(s1);
          if (angle < -90 || angle > 90)
            System.err.println("Between -90 and 90 only");
          else
          {
            int on = 0;
            int off = (int)(servoMin + (((double)(angle + 90) / 180d) * (servoMax - servoMin)));
            System.out.println("setPWM(" + servo + ", " + on + ", " + off + ");");
            servoBoard.setPWM(servo, on, off);
            System.out.println("-------------------");
          }
        }
        catch (Exception ex)
        {
          ex.printStackTrace();
        }
      }
    } 
    System.out.println("Done.");
  }
}
