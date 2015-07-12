/**
 * Created by hxiao on 12/11/14.
 */

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;


//Primary Account Key	FcsobK38692Ar95cYWoln4HvzZmBfDeQOVqqxW27ZUE
//Customer ID	c944f3ea-fd7c-42fc-a0eb-bb2134f49a79

public class rssFeed {
    private static final Logger LOG = LoggerFactory.getLogger(rssFeed.class);
    public boolean needTranslate;
    Random r = new Random();
    private URL feedUrl;
    private String category;
    private String subCategory;
    private List<String> imgSets = new ArrayList<String>();
    private HashMap<String, String> allowedKeys = new HashMap<String, String>();

    public rssFeed(String url, String category, boolean needTranslate) {
        this.needTranslate = needTranslate;
        try {
            this.feedUrl = new URL(url);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        this.category = category;
    }

    public rssFeed(String keyword, String subCategory, String category, String imgUrl) {
        this.needTranslate = false;
        try {
            this.feedUrl = new URL("https://news.google.com/news/section?cf=all&ned=us&hl=en&q=" +
                    URLEncoder.encode(keyword, "UTF-8") + "&output=rss");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        this.category = category;
        this.subCategory = subCategory;
        addImg(imgUrl);
        try {
            FileInputStream fis = new FileInputStream("keywords.txt");
            //Construct BufferedReader from InputStreamReader
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String line = null;
            while ((line = br.readLine()) != null) {
                String[] info = line.trim().split(",");
                allowedKeys.put(info[0], info[1]);
            }
            br.close();
            fis.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    public String getImg() {
        if (imgSets.size() > 0) {
            return imgSets.get(r.nextInt(imgSets.size()));
        }
        return null;
    }

    public String getCategory() {
        return category;
    }

    public String getSubCategory() {
        return subCategory;
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
                        sf.getDescription().getValue(), sf.getPublishedDate(), this, allowedKeys);
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
