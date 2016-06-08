package dk.znz.sgs2converter;
public class Utils {
  public static String humanReadableByteCount(long bytes, boolean si) {
    int unit = si ? 1000 : 1024;
    if (bytes < unit)
      return String.format("%d B", bytes);
    int exp = (int) (Math.log(bytes) / Math.log(unit));
    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
  }
  
  public static String getNameNoExtension(String path) {
    int index = path.lastIndexOf('.');
    if(index <= 0)
      return path;
    return path.substring(0, index);
  }
}
