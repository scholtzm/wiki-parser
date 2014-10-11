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
 * @author Mike
 * 
 */
public class XmlParserTest {
    private static final String INPUTFILE = "res/sample_alts_skwiki-latest-pages-articles.xml";
    private static final String OUTPUTFILE = "output.xml";

    private static XmlParser xmlParser;

    @BeforeClass
    public static void runBeforeAllTests() {
        xmlParser = new XmlParser(new File(INPUTFILE).getAbsolutePath());
    }

    @Test
    public void parsedStructureIsNotNull() {
        ArrayList<Page> output = xmlParser.parse();
        assertNotNull("Parsed input file is not null.", output);
    }

    @Test
    public void outputFileShouldExists() {
        ArrayList<Page> wikiPages = xmlParser.parse();
        Processor processor = new Processor(wikiPages);
        HashMap<String, Record> wikiRecords = processor.DoWork();
        xmlParser.write(OUTPUTFILE, wikiRecords);

        if (!new File(OUTPUTFILE).exists()) {
            fail("Output file: \'" + OUTPUTFILE + "\' was not created.");
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
