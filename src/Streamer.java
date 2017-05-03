import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import javax.swing.*;
import java.util.Arrays;
import java.util.List;

/**
 * Created by dgli on 5/3/17.
 */
public class Streamer extends SwingWorker<Void, Streamer.Frame> {
    public static final int FRAME_SIZE = 512;

    private AudioInputStream inputStream;

    public Streamer(AudioInputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    protected Void doInBackground() throws Exception {
        System.out.println("Starting new streamer.");

        float samples[] = new float[FRAME_SIZE * inputStream.getFormat().getChannels()];
        long transfer[] = new long[samples.length];
        int bytesPerSample = inputStream.getFormat().getSampleSizeInBits() + 7 >> 3;
        byte[] bytes = new byte[samples.length * bytesPerSample];

        System.out.println("Source format: " + inputStream.getFormat());
        System.out.println("bytes per sample: " + bytesPerSample);

        SourceDataLine out = AudioSystem.getSourceDataLine(inputStream.getFormat());
        out.open(inputStream.getFormat(), bytes.length);
        out.start();

        System.out.println("Output line openned: " + out);

        // pad output with leading zeros
        for(int feed = 0; feed < 6; feed++) {
            out.write(bytes,0, bytes.length);
        }

        float frame[] = new float[FRAME_SIZE];
        int readAmount;

        while(true) {
            readAmount = inputStream.read(bytes);

            if (readAmount == -1) {
                System.out.println("EOF REACHED!!");
                break;
            }

//            for(int x = 0; x < bytes.length; x++) {
//                bytes[x] = (byte) (Math.sin(x) * 8);
//            }

            out.write(bytes, 0, readAmount);

            for(int x = 0; x < FRAME_SIZE; x++) {
                frame[x] = (float) Math.random();
            }

            publish(new Frame(frame));

        }

        System.out.println("Streamer ended.");

        return null;
    }

    @Override
    protected void process(List<Frame> frames) {
        // choose the first frame and notify listeners about it.

    }

    public interface StreamListener {
        void processFrame(float frame[]);
    }

    public static class Frame {
        float[] samples;

        public Frame(float[] samples) {
            this.samples = Arrays.copyOf(samples, samples.length);
        }
    }
}
