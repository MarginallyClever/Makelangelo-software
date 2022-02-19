package com.marginallyclever.convenience;

import org.junit.jupiter.api.Test;

import java.io.BufferedInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class FileAccessTest {

    @Test
    void open_fileInClasspath() throws IOException {
        // given
        BufferedInputStream bis = FileAccess.open("/line.svg");

        // then
        assertNotNull(bis);
        assertEquals("<svg", readFirst(bis));
    }

    @Test
    void open_directFile() throws IOException {
        // given

        BufferedInputStream bis = FileAccess.open("pom.xml");

        // then
        assertNotNull(bis);
        assertEquals("<?xm", readFirst(bis));
    }

    @Test
    void open_fileInJar() throws IOException {
        // given
        BufferedInputStream bis = FileAccess.open("/test_jar.jar:folder1/test_in_jar.txt");

        // then
        assertNotNull(bis);
        assertEquals("insi", readFirst(bis));

    }

    /**
     * Read the first 4 bytes
     * @param bis
     * @return
     * @throws IOException
     */
    private String readFirst(BufferedInputStream bis) throws IOException {
        byte[] barr = new byte[4];
        bis.read(barr, 0, 4);
        
        return new String(barr);
    }
}