package adafruiti2c.sensor.listener;

import java.util.EventListener;

public abstract class AdafruitL3GD20Listener implements EventListener
{
  public void motionDetected(double x, double y, double z) {}
  public void close() {}
}
