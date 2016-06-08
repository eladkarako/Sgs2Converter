package dk.znz.sgs2converter;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import java.awt.Font;

public class DropFrame extends JFrame implements DropTargetListener, WindowListener {

  private static final long                     serialVersionUID = 1L;

  private final JLabel                          mLblNewLabel;
  private final JTextArea                       mTxtpnDropAnSamsung;
  private final JProgressBar                    mprogressBar;
  private JPanel                                mcontentPane;
  private ArrayBlockingQueue<File>              mFiles           = new ArrayBlockingQueue<File>(128);
  private final SwingWorker<Void, ProgressData> mWorker          = new SwingWorker<Void, ProgressData>() {
                                                                   @Override protected Void doInBackground() throws Exception {
                                                                     while (!isCancelled()) {
                                                                       File file = mFiles.take();
                                                                       final long fileLength = file.length();

                                                                       final ProgressData progressData = new ProgressData();
                                                                       progressData.file = file;
                                                                       final CountInputStream in = new CountInputStream(new BufferedInputStream(new FileInputStream(file)));
                                                                       BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(Utils.getNameNoExtension(file.getAbsolutePath()) + ".ext4.img"));
                                                                       Thread thread = new Thread(new Runnable() {
                                                                         private long lastPosition;

                                                                         @Override public void run() {
                                                                           while (true) {
                                                                             try {
                                                                               Thread.sleep(1000);
                                                                             } catch (InterruptedException e) {
                                                                               return;
                                                                             }
                                                                             long position = in.getPosition();
                                                                             long timespan = position - lastPosition;
                                                                             String status = String.format("%9s %9s/s",
                                                                                 Utils.humanReadableByteCount(position, false),
                                                                                 Utils.humanReadableByteCount(timespan, false));
                                                                             lastPosition = position;
                                                                             progressData.progress = (double) position / fileLength;
                                                                             progressData.status = status;
                                                                             publish(progressData);
                                                                             if (lastPosition == fileLength)
                                                                               break;
                                                                           }
                                                                         }
                                                                       });
                                                                       thread.start();

                                                                       Sgs2Converter sgs2Converter = new Sgs2Converter(in, out);
                                                                       sgs2Converter.convert();
                                                                       thread.interrupt();
                                                                       progressData.progress = 0.0;
                                                                       progressData.status = "done!";
                                                                       publish(progressData);
                                                                     }
                                                                     return null;
                                                                   }

                                                                   @Override protected void process(List<ProgressData> chunks) {
                                                                     for (ProgressData c : chunks) {
                                                                       mTxtpnDropAnSamsung.append(c.file.getName() + " " + c.status + "\n");
                                                                       mTxtpnDropAnSamsung.setCaretPosition(mTxtpnDropAnSamsung.getDocument().getLength());
                                                                       mprogressBar.setValue((int) (c.progress * 1000));
                                                                     }
                                                                     updateLabel();
                                                                   }
                                                                 };
  private JScrollPane                           mscrollPane;

  /**
   * Launch the application.
   * 
   * @throws IOException
   * @throws FileNotFoundException
   */
  public static void main(String[] args) throws FileNotFoundException, IOException {
    if (args.length != 0) {
      Program.main(args);
    } else {
      EventQueue.invokeLater(new Runnable() {
        public void run() {
          try {
            DropFrame frame = new DropFrame();
            frame.setVisible(true);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      });
    }
  }

  /**
   * Create the frame.
   */
  public DropFrame() {
    setTitle("Drop .img here");
    setAlwaysOnTop(true);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setBounds(100, 100, 578, 369);
    mcontentPane = new JPanel();
    mcontentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
    mcontentPane.setLayout(new BorderLayout(0, 0));
    setContentPane(mcontentPane);

    mTxtpnDropAnSamsung = new JTextArea();
    mTxtpnDropAnSamsung.setFont(new Font("Courier New", Font.PLAIN, 11));
    mTxtpnDropAnSamsung.setLineWrap(true);
    new DropTarget(mTxtpnDropAnSamsung, this);
    mTxtpnDropAnSamsung.setEditable(false);
    mTxtpnDropAnSamsung.setText("drop a samsung \".img\" file here\r\noutput will be <inputname>.ext4.img\r\n");
    mscrollPane = new JScrollPane(mTxtpnDropAnSamsung);
    mcontentPane.add(mscrollPane, BorderLayout.CENTER);

    JPanel panel = new JPanel();
    mcontentPane.add(panel, BorderLayout.SOUTH);
    panel.setLayout(new BorderLayout(0, 0));

    mprogressBar = new JProgressBar();
    mprogressBar.setMaximum(1000);
    panel.add(mprogressBar, BorderLayout.CENTER);

    mLblNewLabel = new JLabel("Awaiting drop..");
    panel.add(mLblNewLabel, BorderLayout.WEST);

    this.addWindowListener(this);

    mWorker.execute();
  }

  @Override public void dragEnter(DropTargetDragEvent dtde) {
    // TODO Auto-generated method stub

  }

  @Override public void dragExit(DropTargetEvent dte) {
    // TODO Auto-generated method stub

  }

  @Override public void dragOver(DropTargetDragEvent dtde) {
    // TODO Auto-generated method stub

  }

  @Override public void drop(DropTargetDropEvent dtde) {
    Transferable tr = dtde.getTransferable();
    if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
      dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
      try {
        List<?> list = (List<?>) tr.getTransferData(DataFlavor.javaFileListFlavor);
        for (Object item : list) {
          File file = (File) item;
          mFiles.add(file);
          mTxtpnDropAnSamsung.append("Added: " + file.getPath() + "\n");
          mTxtpnDropAnSamsung.setCaretPosition(mTxtpnDropAnSamsung.getDocument().getLength());
        }
        updateLabel();
        dtde.dropComplete(true);
      } catch (UnsupportedFlavorException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  void updateLabel() {
    int remaining = mFiles.size();
    mLblNewLabel.setText(remaining != 0 ? "Queued " + remaining : "Awaiting drop..");
  }

  @Override public void dropActionChanged(DropTargetDragEvent dtde) {
    // TODO Auto-generated method stub

  }

  @Override public void windowActivated(WindowEvent arg0) {
    // TODO Auto-generated method stub

  }

  @Override public void windowClosed(WindowEvent arg0) {
    mWorker.cancel(true);
  }

  @Override public void windowClosing(WindowEvent arg0) {
    // TODO Auto-generated method stub

  }

  @Override public void windowDeactivated(WindowEvent arg0) {
    // TODO Auto-generated method stub

  }

  @Override public void windowDeiconified(WindowEvent arg0) {
    // TODO Auto-generated method stub

  }

  @Override public void windowIconified(WindowEvent arg0) {
    // TODO Auto-generated method stub

  }

  @Override public void windowOpened(WindowEvent arg0) {
    // TODO Auto-generated method stub

  }

  class ProgressData {
    public File   file;
    public double progress;
    public String status;
  }
}
