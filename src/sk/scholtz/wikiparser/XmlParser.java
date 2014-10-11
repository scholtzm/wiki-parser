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
    private String inputFile;

    public XmlParser(String inputFile) {
        this.inputFile = inputFile;
    }

    /**
     * Parse selected XML (inputFile)
     * 
     * @return ArrayList of Page objects
     * @throws Exception
     */
    public ArrayList<Page> parse() {
        try {
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            SaxHandler saxHandler = new SaxHandler();

            System.out.println("Starting the parser ...");
            parser.parse(inputFile, saxHandler);
            System.out.println("Done parsing!");

            return saxHandler.getPageList();
        } catch (Exception e) {
            System.out.println(e.getStackTrace());
            System.exit(1);
        }

        return null;
    }

    /**
     * Write final output into XML file
     * 
     * @param outputFile
     *            Chosen outputFile
     * @param wikiRecords
     *            Processed Wiki pages
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
            System.out.println(e.getStackTrace());
            System.exit(1);
        }

    }

    /**
     * Handler for the SAX parser
     * 
     * @author Mike
     * 
     */
    class SaxHandler extends DefaultHandler {
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
}
