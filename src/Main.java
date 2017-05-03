import javax.sound.sampled.AudioInputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Main implements SourceSelectionPanel.SourceSelectionListener {

    private JFrame window = new JFrame("audio_research");
    private SourceSelectionPanel ssPanel = new SourceSelectionPanel(this);
    private JPanel controlPanel = new JPanel(new BorderLayout());


    public Main() {
        assert EventQueue.isDispatchThread();

        window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("Exiting...");
                gracefulExit();
            }
        });

        window.add(ssPanel, BorderLayout.NORTH);
        window.add(controlPanel, BorderLayout.CENTER);
        window.pack();
        window.setResizable(true);
        window.setSize(512, 600);
        window.setVisible(true);

    }

    public void gracefulExit() {
        System.exit(0);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Main();
            }
        });
    }

    @Override
    public void newSourceSelected(AudioInputStream in) {
        new Streamer(in).execute();

    }
}
