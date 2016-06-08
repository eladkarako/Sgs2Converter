package dk.znz.sgs2converter;
public class StopWatch {
  private long mStartTime   = 0;
  private long mStopTime    = 0;
  private long mElapsedTime = 0;

  public void start() {
    this.mStartTime = System.currentTimeMillis();
  }

  public void stop() {
    this.mStopTime = System.currentTimeMillis();
    mElapsedTime = mStopTime - mStartTime;
  }

  public void restart() {
    stop();
    start();
  }

  public long getElapsedTime() {
    return mElapsedTime;
  }

  public static String millisToString(long timeMillis) {
    long time = timeMillis / 1000;
    int millis = (int) (timeMillis % 1000);
    int seconds = (int) (time % 60);
    int minuttes = (int) ((time % 3600) / 60);
    int hours = (int) (time / 3600);
    return String.format("%02d:%02d:%02d.%03d", hours, minuttes, seconds, millis);
  }

  @Override public String toString() {
    return millisToString(getElapsedTime());
  }
}