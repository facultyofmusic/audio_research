import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;


/**
 * This panel implements the audio source selection mechanism.  It is responsible for
 * notifying listeners for source selection changes.
 */
public class SourceSelectionPanel extends JPanel implements ActionListener {
    private static final Logger log = Logger.getLogger(SourceSelectionPanel.class.getName());
    private static final String[] DEFAULT_SOURCE_NAMES = {"None", "Microphone", "Choose File..."};

    // UI control
    private DefaultComboBoxModel<String> selectorModel;
    private JComboBox<String> sourceSelector;
    private JFileChooser fileChooser;
    private File currentFile = null;
    private int currentSourceIndex = 0;

    // Audio
    private AudioInputStream currentInputStream;

    /**
     * There is only one listener because handling and managing an audio input stream
     * between more than one consumers is outside the scope of this class.
     */
    private SourceSelectionListener listener;

    public SourceSelectionPanel(SourceSelectionListener listener) {
        this.listener = listener;

        this.setLayout(new FlowLayout());

        selectorModel = new DefaultComboBoxModel<>(DEFAULT_SOURCE_NAMES);

        sourceSelector = new JComboBox<>(selectorModel);
        sourceSelector.setSelectedIndex(0);
        sourceSelector.addActionListener(this);

        fileChooser = new JFileChooser();

        this.add(new JLabel("Select source:"));
        this.add(sourceSelector);
    }

    /**
     * Attempt to choose a new file.
     * @return true of file was selected.
     */
    private boolean loadNewFile() {
        log.info("Loading new file...");

        int retVal = fileChooser.showOpenDialog(this);
        if (retVal == JFileChooser.APPROVE_OPTION) {
            currentFile = fileChooser.getSelectedFile();
            return true;
        } else {
            log.info("File selection canceled.  Aborting.");
            return false;
        }
    }

    /**
     * Closes the current audio input stream of there is one open.
     */
    private void closeCurrentInputStreamIfOpen() {
        if (currentInputStream != null) {
            try {
                currentInputStream.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Switch, init, and open audio lines of one of 2 sources: microphone or the currently
     * selected file.
     * @param isMicrophone true to switch to microphone.
     */
    private void setSource(boolean isMicrophone) {
        closeCurrentInputStreamIfOpen();

        if (isMicrophone) {
            log.info("Switching to microphone");

            try {
                //TODO should move this somewhere else and make it configurable.
                AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100,
                        16, 1, 2, 44100, true);
                TargetDataLine microphoneLine = AudioSystem.getTargetDataLine(format);
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

                log.info("Opening microphone: " + info);

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
            log.info("Switching to file " + currentFile);

            try {
                AudioSystem.getAudioFileFormat(currentFile); // this fails if file is unsupported.
                selectorModel.removeElementAt(0);
                selectorModel.insertElementAt(currentFile.getName(), 0);
                currentInputStream = AudioSystem.getAudioInputStream(currentFile);
                listener.newSourceSelected(currentInputStream);

            } catch (UnsupportedAudioFileException ex) {
                log.warning("Format in " + currentFile + " unsupported.");
                currentFile = null;
            } catch (IOException ex) {
                ex.printStackTrace();
                currentFile = null;
            }

            currentSourceIndex = 0;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == sourceSelector) {
            int sourceIndex = sourceSelector.getSelectedIndex();

            if (sourceIndex == 2) {
                // Only set the source of file is loaded successfully.
                if (loadNewFile()) {
                    setSource(false);
                }

            } else if (sourceIndex == 1 && currentSourceIndex != 1) {
                setSource(true);

            } else if (sourceIndex == 0 && currentSourceIndex != 0){
                setSource(false);
            }

            // No matter what set the current selected source to the "new current source".
            sourceSelector.setSelectedIndex(currentSourceIndex);
        }
    }

    /**
     * A listener interface for classes that want to listen to source selection events.
     */
    public interface SourceSelectionListener {
        void newSourceSelected(AudioInputStream in);
    }
}
