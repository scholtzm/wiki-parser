package sk.scholtz.wikiparser;

import java.io.FileOutputStream;
import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * XML Parser class which parses XML database using DOM parser
 *
 * @author Mike
 */
public class XmlParser {

    public ArrayList<Page> parseWiki(String inputFile) {
        try {
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();

            WikiHandler wikiHandler = new WikiHandler();
            parser.parse(inputFile, wikiHandler);
            return wikiHandler.getPageList();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }

        return null;
    }

    public HashMap<String, Record> parseDump(String inputFile) {
        try {
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();

            DumpHandler dumpHandler = new DumpHandler();
            parser.parse(inputFile, dumpHandler);
            return dumpHandler.getWikiRecords();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }

        return null;
    }

    /**
     * Write final output into XML file
     *
     * @param outputFile Chosen outputFile
     * @param wikiRecords Processed Wiki pages
     */
    public void write(String outputFile, HashMap<String, Record> wikiRecords) {
        try {


            FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
            XMLStreamWriter writer = XMLOutputFactory.newInstance()
                    .createXMLStreamWriter(fileOutputStream, "UTF-8");

            writer.writeStartDocument();
            writer.writeStartElement("output");

            for (Entry<String, Record> wikiRecord : wikiRecords.entrySet()) {
                ArrayList<String> alternatives = wikiRecord.getValue()
                        .getAlternatives();

                // Skip every record with 0 alternative titles
                if (alternatives.size() != 0) {
                    writer.writeStartElement("page");

                    writer.writeStartElement("title");
                    writer.writeCharacters(wikiRecord.getKey());
                    writer.writeEndElement();

                    writer.writeStartElement("alternative");
                    writer.writeAttribute("type", "array");

                    for (int i = 0; i < alternatives.size(); i++) {
                        writer.writeStartElement("alt");
                        writer.writeCharacters(alternatives.get(i));
                        writer.writeEndElement();
                    }

                    writer.writeEndElement();

                    writer.writeEndElement();
                }
            }

            writer.writeEndElement();
            writer.writeEndDocument();

            writer.close();
            fileOutputStream.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }

    }

    /**
     * Wiki Handler for the SAX parser
     *
     * @author Mike
     *
     */
    class WikiHandler extends DefaultHandler {
        private final String PAGE = "page";
        private final String TITLE = "title";
        private final String REDIRECT = "redirect";

        private boolean inPage = false;

        private StringBuilder value;
        private Page page;

        private ArrayList<Page> pageList;

        public ArrayList<Page> getPageList() {
            return this.pageList;
        }

        /*
         * Define necessary callbacks for the SAX parsing handler
         */
        @Override
        public void startDocument() throws SAXException {
            pageList = new ArrayList<Page>();
        }

        @Override
        public void endDocument() throws SAXException {
        }

        @Override
        public void startElement(String uri, String localName, String qName,
                Attributes attributes) throws SAXException {
            value = new StringBuilder();

            if (qName.equals(PAGE)) {
                inPage = true;
                page = new Page();

            } else if (qName.equals(REDIRECT)) {
                if (inPage) {
                    page.setRedirect(attributes.getValue(0));
                }
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            if (qName.equals(PAGE)) {
                inPage = false;
                pageList.add(page);

            } else if (qName.equals(TITLE)) {
                if (inPage) {
                    page.setTitle(value.toString());
                }
            }
        }

        @Override
        public void characters(char ch[], int start, int length)
                throws SAXException {
            value.append(ch, start, length);
        }
    }

    /**
     * Dump Handler for the SAX parser
     *
     * @author Mike
     *
     */
    class DumpHandler extends DefaultHandler {
        private final String PAGE = "page";
        private final String TITLE = "title";
        private final String ALT = "alt";

        private boolean inPage = false;

        private StringBuilder value;
        private Record record;

        private HashMap<String, Record> wikiRecords;

        public HashMap<String, Record> getWikiRecords() {
            return this.wikiRecords;
        }

        /*
         * Define necessary callbacks for the SAX parsing handler
         */
        @Override
        public void startDocument() throws SAXException {
            wikiRecords = new HashMap<String, Record>();
        }

        @Override
        public void endDocument() throws SAXException {
        }

        @Override
        public void startElement(String uri, String localName, String qName,
                Attributes attributes) throws SAXException {
            value = new StringBuilder();

            if (qName.equals(PAGE)) {
                inPage = true;
                record = new Record();
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            if (qName.equals(PAGE)) {
                inPage = false;
                wikiRecords.put(record.getTitle(), record);

            } else if (qName.equals(TITLE)) {
                if (inPage) {
                    record.setTitle(value.toString());
                }

            } else if (qName.equals(ALT)) {
                if (inPage) {
                    record.addAlternative(value.toString());
                }
            }
        }

        @Override
        public void characters(char ch[], int start, int length)
                throws SAXException {
            value.append(ch, start, length);
        }
    }
}
