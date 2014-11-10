package sk.scholtz.wikiparser.tests;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import sk.scholtz.wikiparser.*;

/**
 * Tests for our XmlParser
 *
 * @author Michael Scholtz
 *
 */
public class XmlParserTest {
    private static final String INPUTFILE = "res/sample_alts_skwiki-latest-pages-articles.xml";
    private static final String VALIDOUTPUTFILE = "res/output_sample_alts_skwiki-latest-pages-articles.xml";
    private static final String OUTPUTFILE = "output.xml";

    private static XmlParser xmlParser;

    @BeforeClass
    public static void runBeforeAllTests() {
        xmlParser = new XmlParser();
    }

    @Test
    public void parsedStructureIsNotNull() {
        ArrayList<Page> output = xmlParser.parseWiki(new File(INPUTFILE).getAbsolutePath());
        assertNotNull("Parsed input file is not null.", output);
    }

    @Test
    public void outputFileShouldExist() {
        ArrayList<Page> wikiPages = xmlParser.parseWiki(new File(INPUTFILE).getAbsolutePath());
        Processor processor = new Processor(wikiPages);
        HashMap<String, Record> wikiRecords = processor.DoWork();
        xmlParser.write(OUTPUTFILE, wikiRecords);

        if (!new File(OUTPUTFILE).exists()) {
            fail("Output file: \'" + OUTPUTFILE + "\' was not created.");
        }
    }

    @Test
    public void outputFileMatchesValidFile() {
        outputFileShouldExist();

        HashMap<String, Record> valid = xmlParser.parseDump(new File(VALIDOUTPUTFILE).getAbsolutePath());
        HashMap<String, Record> generated = xmlParser.parseDump(new File(OUTPUTFILE).getAbsolutePath());

        try {
            assertEquals("Sample output file matches generated output file.", valid, generated);
        } catch (Exception e) {
            fail("Caught exception: " + e.getMessage());
        }
    }

    @AfterClass
    public static void cleanUp() {
        File file = new File(OUTPUTFILE);

        if (file.exists()) {
            file.delete();
        }
    }

}
