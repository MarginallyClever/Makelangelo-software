package com.marginallyclever.makelangelo;

import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

public class TranslationsUnusedTest {
    private InputStream getInputStream(String filename) throws FileNotFoundException {
        String nameInsideJar = Translator.WORKING_DIRECTORY+"/"+ FilenameUtils.getName(filename);
        InputStream stream = Translator.class.getClassLoader().getResourceAsStream(nameInsideJar);
        String actualFilename = "Jar:"+nameInsideJar;
        File externalFile = new File(filename);
        if(externalFile.exists()) {
            stream = new FileInputStream(filename);
            actualFilename = filename;
        }
        if( stream == null ) throw new FileNotFoundException(actualFilename);
        return stream;
    }

    @Test
    public void findUnusedTranslations() throws Exception {
        TranslatorLanguage english = new TranslatorLanguage();
        english.loadFromInputStream(getInputStream("target/classes/languages/english.xml"));
        Set<String> keys = english.getKeys();
        Set<String> found = new HashSet<>();

        TranslationsMissingTest search = new TranslationsMissingTest();
        search.searchAllSourceFiles((e)->{
            found.add(e.key);
        });
        keys.removeAll(found);
        System.out.println("Unused translations:");
        for(String key : keys) {
            System.out.println(key);
        }
        Assertions.assertEquals(0,keys.size());
    }
}
