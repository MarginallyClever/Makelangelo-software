package com.marginallyclever.makelangelo.makeart.io;

import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Factory for creating {@link Turtle} objects from audio files - a waveform that is the average of all channels.
 */
public class LoadAudio implements TurtleLoader {
    private static final Logger logger = LoggerFactory.getLogger(LoadAudio.class);
    private final FileNameExtensionFilter filter;

    public LoadAudio() {
        var audioFileTypes = AudioSystem.getAudioFileTypes();
        String [] list = new String[audioFileTypes.length];
        int i=0;
        for( var t : audioFileTypes ) {
            list[i++] = t.getExtension();
        }
        Arrays.sort(list);
        String exts = String.join(", ", list);
        //logger.info("audio formats: {}", exts);
        filter = new FileNameExtensionFilter("Audio Files ("+exts+")", list);
    }

    @Override
    public FileNameExtensionFilter getFileNameFilter() {
        return filter;
    }

    @Override
    public boolean canLoad(String filename) {
        // get the filename extension
        String ext = filename.substring(filename.lastIndexOf('.')+1);
        return Arrays.stream(filter.getExtensions()).anyMatch(ext::equalsIgnoreCase);
    }

    @Override
    public Turtle load(InputStream inputStream) throws Exception {
        if (inputStream == null) throw new NullPointerException("Input stream is null");
        logger.debug("Loading...");

        var turtle = new Turtle();
        turtle.penDown();

        try(AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new BufferedInputStream(inputStream))) {
            // use ais to get the pcm data
            AudioFormat format = audioInputStream.getFormat();
            AudioInputStream pcmStream =
                    (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED)
                    ? getConvertedAudioStream(audioInputStream)
                    : audioInputStream;

            // get all the data at once.
            byte[] bytes = pcmStream.readAllBytes();

            AudioFormat pcmFormat = pcmStream.getFormat();
            int sampleSizeInBytes = pcmFormat.getSampleSizeInBits() / 8; // Convert bits to bytes
            int numChannels = pcmFormat.getChannels();
            if (pcmFormat.getFrameSize() == AudioSystem.NOT_SPECIFIED) {
                // Estimate frame size: bytes per sample * number of channels
                pcmFormat = new AudioFormat(
                        pcmFormat.getEncoding(),
                        pcmFormat.getSampleRate(),
                        pcmFormat.getSampleSizeInBits(),
                        pcmFormat.getChannels(),
                        sampleSizeInBytes * numChannels,
                        pcmFormat.getSampleRate(),
                        pcmFormat.isBigEndian()
                );
            }

            // use pcm data to create a turtle
            int bytesPerFrame = pcmFormat.getFrameSize();
            boolean bigEndian = pcmFormat.isBigEndian();

            // Read samples
            float j=0;
            for (int i = 0; i < bytes.length; i += bytesPerFrame) {
                float sum=0;
                for (int channel = 0; channel < numChannels; channel++) {
                    int sample = 0;
                    if (sampleSizeInBytes == 2) {  // 16-bit samples
                        if (bigEndian) {
                            sample = (bytes[i] << 8) | (bytes[i + 1] & 0xFF);
                        } else {
                            sample = (bytes[i + 1] << 8) | (bytes[i] & 0xFF);
                        }
                    } else if (sampleSizeInBytes == 1) {  // 8-bit samples
                        sample = bytes[i];
                    }
                    float normalized = sample / 32768f;  // Normalize to -1..1
                    // sum the channels
                    sum += normalized;
                }
                // average the channels and move the turtle
                turtle.moveTo(j, sum * 100.0f / numChannels);
                j+=0.01f;
            }
        } catch (Exception e) {
            logger.error("Error loading audio", e);
            throw e;
        }

        return turtle;
    }

    private static AudioInputStream getConvertedAudioStream(AudioInputStream sourceStream) {
        AudioFormat sourceFormat = sourceStream.getFormat();
        AudioFormat targetFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,   // Target encoding
                sourceFormat.getSampleRate(),     // Same sample rate
                16,                               // Use 16-bit samples
                sourceFormat.getChannels(),       // Same number of channels
                sourceFormat.getChannels() * 2,   // Frame size (bytes per frame)
                sourceFormat.getSampleRate(),     // Same frame rate
                false                             // Little-endian
        );
        return AudioSystem.getAudioInputStream(targetFormat, sourceStream);
    }

}
