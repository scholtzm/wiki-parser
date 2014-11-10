package sk.scholtz.wikiparser;

/**
 * POJO class for a single Wiki page
 *
 * @author Michael Scholtz
 *
 */
public class Page {
    private String title;

    // Redirect is null for main pages
    private String redirect;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRedirect() {
        return redirect;
    }

    public void setRedirect(String redirect) {
        this.redirect = redirect;
    }
}
