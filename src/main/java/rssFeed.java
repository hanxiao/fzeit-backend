/**
 * Created by hxiao on 12/11/14.
 */

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import org.fnlp.app.keyword.AbstractExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;


//Primary Account Key	FcsobK38692Ar95cYWoln4HvzZmBfDeQOVqqxW27ZUE
//Customer ID	c944f3ea-fd7c-42fc-a0eb-bb2134f49a79

public class rssFeed {
    Random r = new Random();
    private static final Logger LOG = LoggerFactory.getLogger(rssFeed.class);
    public boolean needTranslate;
    private URL feedUrl;
    private AbstractExtractor extractor;

    public String getImg() {
        if (imgSets.size() > 0) {
            return imgSets.get(r.nextInt(imgSets.size()));
        }
        return null;
    }

    public String getCategory() {
        return category;
    }

    private String category;
    private List<String> imgSets = new ArrayList<String>();

    public rssFeed(String url, String category, boolean needTranslate,
                   AbstractExtractor extractor) {
        this.needTranslate = needTranslate;
        try {
            this.feedUrl = new URL(url);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        this.category = category;
        this.extractor = extractor;
    }


    public rssFeed(String keyword, AbstractExtractor extractor, String category) {
        this.needTranslate = false;
        try {
            this.feedUrl = new URL("https://news.google.com/news/section?cf=all&ned=us&hl=en&q=" +
                    URLEncoder.encode(keyword, "UTF-8")+ "&output=rss");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        this.category = category;
        this.extractor = extractor;
    }

    public void addImg(String url) {
        imgSets.add(url);
    }

    public Stack<wpPost> fetchSingleRSS(){
        LOG.info("Fetching RSS news ...");
        Stack<wpPost> result = new Stack<wpPost>();
        try {
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(feedUrl));
            List<SyndEntry> allFeeds = feed.getEntries();
            for (SyndEntry sf : allFeeds) {
                wpPost newPost = new wpPost(sf.getTitle(),
                        sf.getDescription().getValue(), sf.getPublishedDate(), this, extractor);
                result.push(newPost);
                LOG.info("added a post!");
            }

        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        return result;
    }
}
