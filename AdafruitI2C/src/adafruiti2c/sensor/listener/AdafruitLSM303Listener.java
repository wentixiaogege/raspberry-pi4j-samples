package adafruiti2c.sensor.listener;

import java.util.EventListener;

public abstract class AdafruitLSM303Listener implements EventListener
{
  public void dataDetected(int accX, int accY, int accZ, int magX, int magY, int magZ, float heading) {}
  public void close() {}
}
