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
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

        if (!results.isEmpty()) {
            logger.info("translations missing:");
            for (String result: results) {
                logger.info("  {}", result);
            }
        }
        assertEquals(0, results.size(), "Some translations are missing, see previous logs for details");
    }

    public void searchAllSourceFiles(Consumer<TranslationFileSearcher> consumer) throws IOException {
        Path srcDir = Paths.get("src", "main", "java");
        try (Stream<Path> paths = Files.walk(srcDir)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    //.filter(Files::isRegularFile)
                    //.filter(Files::isReadable)
                    .forEach(path -> {
                        try {
                            searchInAFile(path.toFile(), consumer);
                        } catch (IOException e) {
                            logger.warn("Can read file {}", path, e);
                        }
                    });
        }
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
     * Compare all translations to the english version.
     * Display a list of all missing translations.
     * Fail if any translations are missing.
     */
    @Test
    public void findMissingAndExtraTranslationsInAllLanguages() {
        // load resource bundle for english
        ResourceBundle english = ResourceBundle.getBundle("messages",Locale.forLanguageTag("en"));
        // find all other languages in the same bundle.
        String[] availableLocales = Translator.getLanguageList();

        // compare all other languages to english
        boolean perfect = true;
        for(String locale : availableLocales) {
            if(locale.equals("en")) continue;
            ResourceBundle other = ResourceBundle.getBundle("messages",Locale.forLanguageTag(locale));
            Set<String> missingKeys = new HashSet<>(english.keySet());
            missingKeys.removeAll(other.keySet());
            if (!missingKeys.isEmpty()) {
                logger.info("translations missing in {}: {}", locale, missingKeys);
            }
            Set<String> extraKeys = new HashSet<>(other.keySet());
            extraKeys.removeAll(english.keySet());
            if (!extraKeys.isEmpty()) {
                logger.info("translations not in english in {}: {}", locale, extraKeys);
            }
            perfect &= missingKeys.isEmpty();
        }

        assertTrue(perfect, "Some translations are missing, see previous logs for details");
    }
}
