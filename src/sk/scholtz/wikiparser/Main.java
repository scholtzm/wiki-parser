package sk.scholtz.wikiparser;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Main class
 * 
 * @author Mike
 * 
 */
public class Main {
    private static final String TITLE = "wiki-parser";
    private static final String AUTHOR = "Michael Scholtz";
    private static final int YEAR = 2014;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println(TITLE + " - " + AUTHOR + " - " + YEAR);
            System.out.println("Usage: java -jar wikiparser.jar <input>");
            System.out.println("   input : input file in valid XML format");

            return;
        }

        String inputFile = args[0];
        String outputFile = inputFile + "-output.xml";

        System.out.println("Starting " + TITLE + "...");
        System.out.println("Working Directory: "
                + System.getProperty("user.dir"));
        System.out.println("Input file: " + inputFile);
        System.out.println("Output file: " + outputFile);

        // Setup XmlParser
        XmlParser xmlParser = new XmlParser(inputFile);

        // Parse inputFile
        ArrayList<Page> wikiPages = xmlParser.parse();

        // Setup our Wiki processor
        Processor processor = new Processor(wikiPages);

        // Process output of our XmlParser
        HashMap<String, Record> wikiRecords = processor.DoWork();

        // Write output XML
        xmlParser.write(outputFile, wikiRecords);

        System.out.println("Found " + wikiPages.size() + " pages.");
        System.out.println("Found " + wikiRecords.size() + " unique pages.");
    }

}
