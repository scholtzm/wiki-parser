package sk.scholtz.wikiparser;

import java.util.HashMap;
import java.util.List;

/**
 * Class to further process our parsed Wiki dump
 *
 * @author Michael Scholtz
 *
 */
public class Processor {
    private List<Page> wikiPages;
    private HashMap<String, Record> wikiRecords;

    public Processor(List<Page> wikiPages) {
        this.wikiPages = wikiPages;
    }

    public HashMap<String, Record> DoWork() {
        wikiRecords = new HashMap<String, Record>();

        for (Page page : wikiPages) {

            // This is full Wiki page, add this to our map.
            if (page.getRedirect() == null) {

                // Add this only if it does not exist yet! It might have been
                // created by another redirect.
                if (!wikiRecords.containsKey(page.getTitle())) {
                    Record record = new Record();
                    record.setTitle(page.getTitle());

                    wikiRecords.put(record.getTitle(), record);
                }

            // This is just a redirect.
            } else {

                // We already have this page in our HashMap, add alternative
                // title.
                if (wikiRecords.containsKey(page.getRedirect())) {
                    wikiRecords.get(page.getRedirect()).addAlternative(
                            page.getTitle());

                // Fresh record.
                } else {
                    Record record = new Record();
                    record.setTitle(page.getRedirect());
                    record.addAlternative(page.getTitle());

                    wikiRecords.put(record.getTitle(), record);
                }
            }
        }

        return wikiRecords;
    }
}
