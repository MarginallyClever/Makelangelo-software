package com.marginallyClever.convenience;

import org.junit.jupiter.api.Test;

import java.io.BufferedInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class FileAccessTest {

    @Test
    void open_fileInClasspath() throws IOException {
        // given
        BufferedInputStream bis = FileAccess.open("/4laws.txt");

        // then
        assertNotNull(bis);
        assertEquals("0. A robot", readFirst(bis));
    }

    @Test
    void open_directFile() throws IOException {
        // given

        BufferedInputStream bis = FileAccess.open("pom.xml");

        // then
        assertNotNull(bis);
        assertEquals("<?xml vers", readFirst(bis));
    }

    @Test
    void open_fileInJar() throws IOException {
        // given
        BufferedInputStream bis = FileAccess.open("/test_jar.jar:folder1/test_in_jar.txt");

        // then
        assertNotNull(bis);
        assertEquals("inside jar", readFirst(bis));

    }

    /**
     * Read the first 4 bytes
     * @param bis
     * @return
     * @throws IOException
     */
    private String readFirst(BufferedInputStream bis) throws IOException {
        byte[] barr = new byte[10];
        bis.read(barr, 0, 10);
        
        return new String(barr);
    }
}