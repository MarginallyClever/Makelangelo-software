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
        // check every file in target/classes/languages.
        File folder = new File("target/classes/languages");
        File[] files = folder.listFiles();
        for(File file : files) {
            if(file.isFile()) {
                findUnusedTranslations(file.getAbsolutePath());
            }
        }
    }

    private void findUnusedTranslations(String filename) throws Exception {
        System.out.println("testing "+filename);
        TranslatorLanguage set = new TranslatorLanguage();
        set.loadFromInputStream(getInputStream(filename));
        Set<String> keys = set.getKeys();
        Set<String> found = new HashSet<>();

        TranslationsMissingTest search = new TranslationsMissingTest();
        search.searchAllSourceFiles((e)->found.add(e.key));
        keys.removeAll(found);
        Assertions.assertTrue(keys.isEmpty(),"Unused translations: "+keys);
    }
}
