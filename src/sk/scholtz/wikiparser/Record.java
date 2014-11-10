package sk.scholtz.wikiparser;

import java.util.ArrayList;

/**
 * POJO class for a single record
 *
 * @author Michael Scholtz
 *
 */
public class Record {
    private String title;
    private ArrayList<String> alternatives;

    public Record() {
        this.alternatives = new ArrayList<String>();
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ArrayList<String> getAlternatives() {
        return this.alternatives;
    }

    public void addAlternative(String alternative) {
        this.alternatives.add(alternative);
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Record)) {
            return false;
        }

        Record compared = (Record) object;

        return this.title.equals(compared.getTitle()) && this.alternatives.equals(compared.getAlternatives());
    }
}
