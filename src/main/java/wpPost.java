import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;
import org.fnlp.app.keyword.AbstractExtractor;
import org.fnlp.nlp.cn.ChineseTrans;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;

public class wpPost implements java.io.Serializable{
    public String postid;
    public String org_title;
    public String org_content;
    public String trans_title;
    public String trans_content;
    public Date pub_date;
    public Date fetch_date;
    public boolean is_translate;
    public String linkUrl = "";
    public String author;
    public String category;
    public String imgUrl;
    public String[] keywords;
    public boolean has_posted = false;
    public int numLinks = 0;
    transient Elements links;

    static final ChineseTrans chineseTrans = new ChineseTrans();

    public HashMap buildPub() {
        HashMap hmContent = new HashMap();
        hmContent.put("title", trans_title);
        hmContent.put("description", trans_content);
        hmContent.put("post_status", "publish");
        hmContent.put("post_date", pub_date);
        //Basically, we can put anything here as long as it match's wordpress's fields.;
        if (numLinks > 6) {
            hmContent.put("categories", new String[]{"头条"});
        } else {
            hmContent.put("categories", new String[]{category});
        }
        hmContent.put("mt_keywords", keywords);
        hmContent.put("dateCreated", pub_date);
        hmContent.put("date_modified", pub_date);
        hmContent.put("mt_text_more", linkUrl);
        HashMap thumbContent = new HashMap();
        thumbContent.put("grid", "double");
        hmContent.put("custom_fields", thumbContent);

        this.has_posted = true;
        //hmContent.put("wp_post_thumbnail","http://www.tum.de/fileadmin/tu/layout/images/tumlogo.png");
        return  hmContent;
    }

    public int hashCode() {
        return this.org_title.hashCode();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof wpPost)) {
            return false;
        }
        wpPost other = (wpPost) obj;
        return this.org_title.equals(other.org_title);
    }

    private String cleanTitle(String org_title) {
        // do something cleaning work for the title
        String result;
        author = org_title.substring(org_title.lastIndexOf('-') + 1, org_title.length()).trim();
        result = chineseTrans.toSimp(org_title
                .replaceAll("-\\s.*?$", "")
                .replaceAll("^.*[：:]", "")).trim();
        result = chineseTrans.normalizeCAP(result, true);
        return result;
    }

    private String cleanContent(String org_content) {
        String result;
        Document doc = Jsoup.parse(org_content);
        links = doc.select("a[href]");
        result = chineseTrans.toSimp(doc.text())
                .replaceAll("\\.\\.\\..*$", "").trim();


        for (Element link : links) {
            if (link.attr("abs:href").matches(".*url=.*")) {
                String this_link = link.attr("abs:href").replaceAll(".*?url=", "");
                if (this_link.length() > 0) {
                    linkUrl += String.format("<p><a href=\"%s\">[%s]</a></p>", this_link, link.text().trim());
                    numLinks ++;
                }
            }
        }
        result = chineseTrans.normalizeCAP(result, true);
        return result;
    }

    public wpPost(String org_title, String org_content, Date pub_date,
                  rssFeed feed, AbstractExtractor extractor) {

        this.org_title = cleanTitle(org_title);
        this.org_content = cleanContent(org_content);
        this.pub_date = pub_date;
        this.is_translate = feed.needTranslate;
        // fetch date is current time
        this.fetch_date = new Date();
        this.category = feed.getCategory();
        this.imgUrl = feed.getImg();


        if (is_translate) {
            Translate.setClientId("fzeit-backend");
            Translate.setClientSecret("EMPqFQHxHvRtuSGWTWtDEDlXVeOQkwvp38guui4rGJ4=");
            try {
                if (org_title.trim().length() > 0) {
                    this.trans_title = Translate.execute(org_title, Language.AUTO_DETECT, Language.CHINESE_SIMPLIFIED);
                }
                if (org_content.trim().length() > 0) {
                    this.trans_content = Translate.execute(org_content, Language.AUTO_DETECT, Language.CHINESE_SIMPLIFIED);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            if (org_title.trim().length() == 0 && org_title.trim().length() == 0) {
                this.is_translate = false;
            }
        } else {
            this.trans_content = this.org_content;
            this.trans_title = this.org_title;
        }

        if (this.pub_date == null) {
            this.pub_date = this.fetch_date;
        }

        List<String> tags = new ArrayList<String>();
        try {
            Map<String,Integer> result = extractor.extract(this.trans_content, 10);
            for (Map.Entry<String, Integer> entry : result.entrySet()) {
                String key = entry.getKey();
                if (!key.matches(".*\\d+.*") &&
                        key.length() > 1 && entry.getValue() > 60) {
                    tags.add(key);
                }
            }
            this.keywords = new String[tags.size()];
            this.keywords = tags.toArray(this.keywords);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
