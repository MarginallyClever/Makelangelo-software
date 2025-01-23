package com.marginallyclever.makelangelo.makeart.io;

import org.junit.jupiter.api.Test;

import javax.sound.sampled.AudioSystem;

public class LoadAudioTest {
    @Test
    public void getAudioFileFormats() {
        for( var t : AudioSystem.getAudioFileTypes() ) {
            System.out.println(t.getExtension()+": "+ t.toString());
        }
    }

    @Test
    public void testLoadAudio() {
        String testPath = "com/marginallyclever/makelangelo/makeart/io/file_example_MP3_1MG.mp3";

        LoadAudio loadAudio = new LoadAudio();
        assert(null!=loadAudio.getFileNameFilter());
        assert(loadAudio.canLoad(testPath));
        try {
            var turtle = loadAudio.load(getClass().getClassLoader().getResourceAsStream(testPath));
            assert(turtle.history.size()>1e6);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
