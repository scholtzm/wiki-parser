package sk.scholtz.wikiparser;

import java.io.File;
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

        if (args.length < 2 || args.length > 50) {
            printHelp();
            return;
        }

        OperationMode opMode;

        if(args[0].equals("-W")) {
            opMode = OperationMode.WIKIPEDIA;
        } else if(args[0].equals("-L")) {
            opMode = OperationMode.WIKIPARSER;
        } else {
            printHelp();
            return;
        }

        String inputFile = args[1];
        String outputFile = inputFile + "-output.xml";
        String query = args.length >= 3 ? args[2] : "";

        System.out.println("Starting " + TITLE + "...");
        System.out.println("Working Directory: " + System.getProperty("user.dir"));
        System.out.println("Mode: " + opMode.toString());

        if(opMode == OperationMode.WIKIPARSER)
            System.out.println("Query: " + query);

        System.out.println("Input file: " + inputFile);

        if(opMode == OperationMode.WIKIPEDIA)
            System.out.println("Output file: " + outputFile);

        // Setup XmlParser
        XmlParser xmlParser = new XmlParser();

     // Parsing and processing Wikipedia dumps
        if(opMode == OperationMode.WIKIPEDIA) {
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

        // Parsing, indexing and searching Wikiparser dumps
        } else if(opMode == OperationMode.WIKIPARSER) {
            // Process XML and create index
            t1 = System.currentTimeMillis();
            System.out.println("Processing XML dump and creating indexes ...");

            HashMap<String, Record> wikiRecords = xmlParser.parseDump(new File(inputFile).getAbsolutePath());

            LuceneSearch lucene = new LuceneSearch();
            try {
                lucene.createIndex(wikiRecords);
            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println(String.format("Processing took: %.3f seconds", ((double)(System.currentTimeMillis() - t1) / 1000)));

            // Commence search
            t1 = System.currentTimeMillis();
            System.out.println("Commencing search ...");

            try {
                lucene.search(query);
            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println(String.format("Searching took: %.3f seconds", ((double)(System.currentTimeMillis() - t1) / 1000)));
        }
    }

    public static void printHelp() {
        System.out.println(TITLE + " - " + AUTHOR + " - " + YEAR);
        System.out.println("Usage: java -jar wikiparser.jar [-W input|-L input query]");
        System.out.println("    -W : parse and process valid Wikipedia dump");
        System.out.println("    -L : index and search valid Wikiparser dump");
        System.out.println("    input : input file in valid XML format");
        System.out.println("    query : Lucene query");
    }

    public enum OperationMode {
        WIKIPEDIA,
        WIKIPARSER
    }
}
