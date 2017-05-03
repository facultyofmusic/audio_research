import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * Created by dgli on 5/3/17.
 */
public class SourceSelectionPanel extends JPanel implements ActionListener {
    String[] sourceNames = {"None", "Microphone", "Choose File..."};
    int currentSourceIndex = 0;
    JComboBox sourceSelector;

    AudioInputStream currentInputStream;
    SourceSelectionListener listener;



    public SourceSelectionPanel(SourceSelectionListener listener) {
        this.listener = listener;

        this.setLayout(new FlowLayout());

        sourceSelector = new JComboBox(sourceNames);
        sourceSelector.setSelectedIndex(0);
        sourceSelector.addActionListener(this);

        this.add(new JLabel("Select source:"));
        this.add(sourceSelector);
    }

    private void loadNewFile() {
        System.out.println("Loading new file...");
    }

    private void setSource(boolean isMicrophone) {
        // should always close streams when no longer used.
        if (currentInputStream != null) {
            try {
                currentInputStream.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        if (isMicrophone) {
            System.out.println("Switching to microphone");

            try {
                AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100,
                        16, 1, 2, 44100, true);
//                AudioFormat format = new AudioFormat(8000.0f, 16, 1,
//                        true, true);
                TargetDataLine microphoneLine = AudioSystem.getTargetDataLine(format);
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

                System.out.println("Opening microphone: " + info);

                microphoneLine.open();
                currentInputStream = new AudioInputStream(microphoneLine);
                microphoneLine.start();
                listener.newSourceSelected(currentInputStream);

            } catch (LineUnavailableException ex) {
                ex.printStackTrace();
                // TODO should proably restore to none.
            }

            currentSourceIndex = 1;

        } else {
            System.out.println("Switching to file");

            currentSourceIndex = 0;

        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == sourceSelector) {
            int sourceIndex = sourceSelector.getSelectedIndex();

            if (sourceIndex == 2) {
                // TODO select a new file.
                loadNewFile();
                setSource(false);

            } else if (sourceIndex == 1 && currentSourceIndex != 1) {
                // TODO select microphone.
                setSource(true);

            } else if (sourceIndex == 0 && currentSourceIndex != 0){
                // TODO set to prev file.
                setSource(false);
            }
        }
    }

    public interface SourceSelectionListener {
        void newSourceSelected(AudioInputStream in);
    }
}
