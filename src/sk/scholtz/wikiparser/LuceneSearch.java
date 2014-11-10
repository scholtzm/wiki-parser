package sk.scholtz.wikiparser;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

/**
 * Lucene search handling
 *
 * @author Michael Scholtz
 *
 */
public class LuceneSearch {
    private StandardAnalyzer analyzer;
    private Directory index;

    private final Version VERSION = Version.LUCENE_4_10_1;

    public LuceneSearch() {
        this.analyzer = new StandardAnalyzer();
    }

    /**
     * This will create complete index of our XML dump
     * @param wikiRecords Result of XmlParser.parseDump method
     * @throws IOException
     * @throws ParseException
     */
    public void createIndex(HashMap<String, Record> wikiRecords) throws IOException, ParseException {
        // RAMDirectory seems to be ideal for small indexes
        index = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(VERSION, analyzer);
        IndexWriter indexWriter = new IndexWriter(index, config);

        for(Entry<String, Record> record : wikiRecords.entrySet()) {
            Document document = new Document();

            document.add(new TextField("title", record.getValue().getTitle(), Field.Store.YES));
            for(String alt : record.getValue().getAlternatives())
                document.add(new TextField("alt", alt, Field.Store.YES));

            indexWriter.addDocument(document);
        }

        indexWriter.close();
    }

    /**
     * Search our index
     * @param query Search query
     * @throws IOException
     * @throws ParseException
     */
    public void search(String query, int hits) throws IOException, ParseException {
        // Create multi-field query
        String[] fields = new String[] { "title", "alt" };
        Query multiFieldQuery = new MultiFieldQueryParser(fields, analyzer).parse(query);

        // Start search
        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
        TopScoreDocCollector collector = TopScoreDocCollector.create(hits, true);
        searcher.search(multiFieldQuery, collector);
        ScoreDoc[] results = collector.topDocs().scoreDocs;

        // Dump results
        System.out.println("Found " + results.length + " search results.");
        for (int i = 0; i < results.length; i++) {
            int resultId = results[i].doc;
            Document d = searcher.doc(resultId);
            System.out.println((i + 1) + ". " + d.get("title"));

            for(IndexableField inf : d.getFields("alt")) {
                System.out.println("\t" + inf.stringValue());
            }
        }

        // Do not forget to close the reader
        reader.close();
    }
}
