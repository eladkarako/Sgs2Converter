package dk.znz.sgs2converter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class Sgs2Converter {
  
  private static final long UINT32_MASK = 0xffffffffl;

  private final int[] mBuffer;

  private DataInputStream mIn;
  private DataOutputStream mOut;

  public Sgs2Converter(InputStream in, OutputStream out) {
    mIn = new DataInputStream(in);
    mOut = new DataOutputStream(out);
    mBuffer = new int[3];
  }
  
  public void convert() throws IOException {
    try { 
      for (int i = 0; i < 7; i++) {
        mIn.readInt();
      }
  
      while (true) {
        try {
          mBuffer[0] = mIn.readInt();
        } catch (EOFException e) {
          return;
        }
        switch (mBuffer[0]) {
        case 0xC1CA0000:
          mBuffer[1] = mIn.readInt();
          mBuffer[2] = mIn.readInt();
          copyField();
          break;
        case 0xC3CA0000:
          mBuffer[1] = mIn.readInt();
          mBuffer[2] = mIn.readInt();
          writeZeros();
          break;
        default:
          throw new IOException("Invalid header");
        }
      }
    } finally {
      mOut.close();
    }
  }
  
  private void copyField() throws IOException {
    int b = Integer.reverseBytes(mBuffer[2]);
    long k = b & UINT32_MASK;
    k = ((k - 12) >> 2);
    for (int i = 0; i < k; i++) {
      mBuffer[0] = mIn.readInt();
      mOut.writeInt(mBuffer[0]);
    }
  }
  
  private void writeZeros() throws IOException {
    if (mBuffer[1] != 0) {
      long k = (Integer.reverseBytes(mBuffer[1]) & UINT32_MASK) * 1024;
      mBuffer[2] = 0;
      for (int i = 0; i < k; i++) {
        mOut.writeInt(0);
      }
    }
  }
}