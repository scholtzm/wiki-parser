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
        // Timer variable
        long t1;

        if (args.length < 1) {
            System.out.println(TITLE + " - " + AUTHOR + " - " + YEAR);
            System.out.println("Usage: java -jar wikiparser.jar <input>");
            System.out.println("    input : input file in valid XML format");

            return;
        }

        String inputFile = args[0];
        String outputFile = inputFile + "-output.xml";

        System.out.println("Starting " + TITLE + "...");
        System.out.println("Working Directory: " + System.getProperty("user.dir"));
        System.out.println("Input file: " + inputFile);
        System.out.println("Output file: " + outputFile);

        // Setup XmlParser
        XmlParser xmlParser = new XmlParser();

        // Parse inputFile
        t1 = System.currentTimeMillis();
        System.out.println("Parsing data ...");

        ArrayList<Page> wikiPages = xmlParser.parseWiki(inputFile);

        System.out.println(String.format("Parsing took: %.3f seconds", ((double)(System.currentTimeMillis() - t1) / 1000)));

        // Setup our Wiki processor
        Processor processor = new Processor(wikiPages);

        // Process output of our XmlParser
        t1 = System.currentTimeMillis();
        System.out.println("Processing data ...");

        HashMap<String, Record> wikiRecords = processor.DoWork();

        System.out.println(String.format("Processing took: %.3f seconds", ((double)(System.currentTimeMillis() - t1) / 1000)));

        // Write output XML
        t1 = System.currentTimeMillis();
        System.out.println("Writing output file ...");

        xmlParser.write(outputFile, wikiRecords);

        System.out.println(String.format("Writing took: %.3f seconds", ((double)(System.currentTimeMillis() - t1) / 1000)));

        // Stats
        System.out.println("Found " + wikiPages.size() + " pages.");
        System.out.println("Found " + wikiRecords.size() + " unique pages.");
    }

}
