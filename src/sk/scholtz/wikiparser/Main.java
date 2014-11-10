package sk.scholtz.wikiparser;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Main class
 *
 * @author Michael Scholtz
 *
 */
public class Main {
    private static Options options = new Options();

    private static final String TITLE = "wiki-parser";
    private static final String AUTHOR = "Michael Scholtz";
    private static final int YEAR = 2014;

    public static void main(String[] args) {
        // Timer variable
        long t1;
        // Operation mode W or P
        OperationMode opMode = OperationMode.WIKIPEDIA;
        // Other variables coming from command line arguments
        String inputFile = "";
        String outputFile = "";
        String query = "";
        int hits = 100;

        // Read stdin
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        // Process command line options
        createOptions();

        CommandLineParser parser = new BasicParser();
        try {
            CommandLine line = parser.parse(options, args);

            // help
            if(line.hasOption("h")) {
                printHelp();
                return;
            }

            // mode and query
            if(line.hasOption("W")) {
                opMode = OperationMode.WIKIPEDIA;
            } else if(line.hasOption("P")) {
                opMode = OperationMode.WIKIPARSER;

                if(line.hasOption("q")) {
                    query = line.getOptionValue("q");
                } else {
                    System.out.print("WIKIPARSER mode enabled. Enter your query: ");
                    try {
                        query = in.readLine();

                        if(query.equals("/exit")) {
                            System.exit(0);
                        }
                    } catch (IOException e) {
                        System.out.println("Exception: " + e.getMessage());
                        printHelp();
                        return;
                    }
                }
            } else {
                printHelp();
                return;
            }

            // input file
            if(line.hasOption("i")) {
                inputFile = line.getOptionValue("i");
            } else {
                printHelp();
                return;
            }

            // output file
            if(line.hasOption("o")) {
                outputFile = line.getOptionValue("o");
            } else {
                outputFile = inputFile + "-output.xml";
            }

            // hits
            if(line.hasOption("H")) {
                try {
                    hits = Integer.parseInt(line.getOptionValue("H"));
                } catch(Exception e) {
                    System.out.println("Exception: " + e.getMessage());
                    printHelp();
                    return;
                }
            }
        }
        catch(ParseException e) {
            System.out.println("Command line exception: " + e.getMessage());
            System.exit(1);
        }

        System.out.println("Starting " + TITLE + "...");
        System.out.println("Working Directory: " + System.getProperty("user.dir"));
        System.out.println("Mode: " + opMode.toString());

        if(opMode == OperationMode.WIKIPARSER) {
            System.out.println("Query: " + query);
            System.out.println("Hits: " + hits);
        }

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

            while(!query.equals("/exit")) {
                // Commence search
                t1 = System.currentTimeMillis();
                System.out.println("Commencing search ...");

                try {
                    lucene.search(query, hits);
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                    return;
                }

                System.out.println(String.format("Searching took: %.3f seconds", ((double)(System.currentTimeMillis() - t1) / 1000)));

                System.out.println("Enter new query or type \"/exit\" to exit the application.");
                System.out.print("New query: ");
                try {
                    query = in.readLine();
                } catch (IOException e) {
                    System.out.println("Exception: " + e.getMessage());
                    return;
                }
            }
        }

        System.out.println("Finished.");
    }

    public static void printHelp() {
        System.out.println(TITLE + " - " + AUTHOR + " - " + YEAR);

        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java -jar wikiparser.jar -W|-P -i <inputFile> [-q <query>] [-h <hits>]", options);
    }

    public static void createOptions() {
        options.addOption("h", "help", false, "show this help");
        options.addOption("W", "wikipedia", false, "use wikipedia mode");
        options.addOption("P", "parser", false, "use wikiparser mode");
        options.addOption("i", "inputFile", true, "input file");
        options.addOption("o", "outputFile", true, "output file");
        options.addOption("q", "query", true, "Lucene query");
        options.addOption("H", "hits", true, "max number of Lucene search hits (default: 100)");
    }

    public enum OperationMode {
        WIKIPEDIA,
        WIKIPARSER
    }
}
