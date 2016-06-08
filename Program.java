package dk.znz.sgs2converter;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Program {

  /**
   * @param args
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {
    if(args.length != 2) {
      System.out.println(
          "Usage java -jar sgs2converter.jar \"inputfile\" \"outputfile\"\n" +
          "fx: java -jar sgs2converter.jar \"factoryfs.img\" \"factoryfs.ext4.img\"");
      return;
    }
    File inputFilename = new File(args[0]);
    File outputFilename = new File(args[1]);

    final long fileLength = inputFilename.length();
    
    final CountInputStream in = new CountInputStream(new BufferedInputStream(new FileInputStream(inputFilename)));
    BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outputFilename));
    
    final StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    
    Thread thread = new Thread(new Runnable() {
      private long lastPosition;
      @Override public void run() {
        while(true) {
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            return;
          }
          long position = in.getPosition();
          long timespan = position - lastPosition;
          System.out.format("%9s %9s/s%n",
              Utils.humanReadableByteCount(position, false),
              Utils.humanReadableByteCount(timespan, false)); 
          lastPosition = position;
          if(lastPosition == fileLength)
            break;
        }
        stopWatch.stop();
        System.out.println("Finished in " + stopWatch);
      }
    });
    thread.start();

    Sgs2Converter sgs2Converter = new Sgs2Converter(in, out);
    sgs2Converter.convert();
    thread.interrupt();
  }
}
