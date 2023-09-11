package com.marginallyclever.makelangelo;

import com.marginallyclever.util.PreferencesHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class TranslationsMissingTest {

    private static final Logger logger = LoggerFactory.getLogger(TranslationsMissingTest.class);

    private final Pattern patternComment = Pattern.compile("^\\s*//.*");
    private final Pattern patternTranslator = Pattern.compile("Translator\\s*\\.\\s*get\\s*\\(\\s*\"(?<key>[^)]*)\"\\s*[,\\)]");

    public static class TranslationFileSearcher {
        public final String key;
        public final File file;
        public final int lineNumber;
        TranslationFileSearcher(String k,File f,int line) {
            key=k;
            file=f;
            lineNumber=line;
        }
    }

    @BeforeAll
    public static void init() {
        PreferencesHelper.start();
        Translator.start();
    }

    @Test
    public void findMissingTranslations() throws IOException {
        List<String> results = new ArrayList<>();

        searchAllSourceFiles((e)->{
            String trans = Translator.get(e.key);
            if (trans.startsWith(Translator.MISSING)) {
                try {
                    results.add(String.format("file://%s:%s: %s", e.file.getCanonicalPath(), e.lineNumber, e.key));
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        if (results.size() != 0) {
            logger.info("translations missing:");
            for (String result: results) {
                logger.info("  {}", result);
            }
        }
        assertEquals(0, results.size(), "Some translations are missing, see previous logs for details");
    }

    public void searchAllSourceFiles(Consumer<TranslationFileSearcher> consumer) throws IOException {
        File srcDir = new File("src" + File.separator + "main" + File.separator + "java");
        List<File> files = listFiles(srcDir.toPath(), ".java");
        files.forEach(file -> {
            try {
                searchInAFile(file, consumer);
            } catch (IOException e) {
                logger.warn("Can read file {}", file, e);
            }
        });
    }

    @Test
    public void verifyThatMatcherWorks_results() throws IOException {
        List<String> results = new ArrayList<>();

        // matches.txt contains few entries with translation and one without. The list should not be empty
        searchInAFile(new File("src/test/resources/translator/matches.txt"), e->{
            String trans = Translator.get(e.key);
            if (trans.startsWith(Translator.MISSING)) {
                try {
                    results.add(String.format("file://%s:%s: %s", e.file.getCanonicalPath(), e.lineNumber, e.key));
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        assertNotNull(results);
        assertEquals(2, results.size());
        assertTrue(results.get(0).contains("matches.txt:4: unknownKey"));
        assertTrue(results.get(1).contains("matches.txt:8: valueToBeFilledIn"));
    }

    @Test
    public void verifyThatMatcherWorks_noResult() throws IOException {
        List<String> results = new ArrayList<>();

        searchInAFile(new File("src/test/resources/translator/no-match.txt"), e->results.add(e.key));

        // no-match does not contains any code matching the translator. The list must be empty
        assertEquals(0, results.size());
    }

    public void searchInAFile(File file, Consumer<TranslationFileSearcher> consumer) throws IOException {
        int lineNb = 1;
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            String line;
            while((line = bufferedReader.readLine()) != null) {
                Matcher m = patternComment.matcher(line);
                if (!m.matches()) {
                    m = patternTranslator.matcher(line);
                    while (m.find()) {
                        String key = m.group("key");
                        consumer.accept(new TranslationFileSearcher(key, file, lineNb));
                    }
                }
                lineNb++;
            }
        }
    }


    /**
     * List all files and sub files in this path. Using
     * <code>Files.walk(path)</code> (so this take care of recursive path
     * exploration ) And applying filter ( RegularFile and ReadableFile ) and
     * filtering FileName ...
     *
     * @param path where to look.
     * @param fileNameEndsWithSuffix use ".java" to get only ... ( this is not a
     * regexp so no '.' despecialization required ) can be set to
     * <code>""</code> to get all files.
     * @return a list of files (may be empty if nothing is found) or null if
     * something is wrong.
     * @throws IOException
     */
    public static List<File> listFiles(Path path, String fileNameEndsWithSuffix) throws IOException {
        List<File> result;
        try ( Stream<Path> walk = Files.walk(path)) {
            result = walk
                    .filter(Files::isRegularFile)
                    .filter(Files::isReadable)
                    .map(Path::toFile)
                    .filter(f -> f.getName().endsWith(fileNameEndsWithSuffix))
                    .collect(Collectors.toList());
        }
        return result;
    }
}
