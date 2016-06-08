package dk.znz.sgs2converter;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

class CountInputStream extends FilterInputStream {
  private volatile long mPosition;

  public CountInputStream(InputStream in) {
    super(in);
  }

  @Override public int read() throws IOException {
    mPosition++;
    return super.read();
  }

  public long getPosition() {
    return mPosition;
  }
}