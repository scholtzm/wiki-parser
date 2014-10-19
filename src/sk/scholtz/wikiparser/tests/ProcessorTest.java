package sk.scholtz.wikiparser.tests;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.BeforeClass;
import org.junit.Test;

import sk.scholtz.wikiparser.*;

/**
 * Tests for Processor class
 * 
 * @author Mike
 * 
 */
public class ProcessorTest {
    private static final String INPUTFILE = "res/sample_alts_skwiki-latest-pages-articles.xml";

    private static XmlParser xmlParser;
    private static Processor processor;
    private static ArrayList<Page> xmlParserResult;

    @BeforeClass
    public static void runBeforeAllTests() {
        xmlParser = new XmlParser();
        xmlParserResult = xmlParser.parseWiki(new File(INPUTFILE).getAbsolutePath());

        processor = new Processor(xmlParserResult);
    }

    @Test
    public void processedStructureIsNotNull() {
        HashMap<String, Record> output = processor.DoWork();
        assertNotNull("Result of `DoWork` is not null.", output);
    }

    @Test
    public void hasTwoEntries() {
        HashMap<String, Record> output = processor.DoWork();
        assertTrue("Result has exactly 2 entries.", output.size() == 2);
    }

    @Test
    public void entryAutoDoesNotExist() {
        HashMap<String, Record> output = processor.DoWork();
        assertFalse("Entry named `Auto` does not exist.", output.keySet()
                .contains("Auto"));
    }

    @Test
    public void entryHlavnaStrankaDoesExist() {
        HashMap<String, Record> output = processor.DoWork();
        assertTrue("Entry named `Hlavn� str�nka` does exist.", output.keySet()
                .contains("Hlavn� str�nka"));
    }

    @Test
    public void entryHlavnaStrankaHasOneAlternative() {
        HashMap<String, Record> output = processor.DoWork();
        assertTrue("Entry named `Hlavn� str�nka` has one alternative.", output
                .get("Hlavn� str�nka").getAlternatives().size() == 1);
    }

    @Test
    public void entryHlavnaStrankasFirstAlternativeIsMainPage() {
        HashMap<String, Record> output = processor.DoWork();
        assertTrue(
                "Entry named `Hlavn� str�nka` has first alternative `Main Page`.",
                output.get("Hlavn� str�nka").getAlternatives().get(0)
                        .equals("Main Page"));
    }
}
