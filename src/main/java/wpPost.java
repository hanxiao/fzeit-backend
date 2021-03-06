import com.github.stuxuhai.jpinyin.PinyinHelper;
import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;
import org.apache.commons.lang.StringUtils;
import org.fnlp.nlp.cn.ChineseTrans;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.SimpleDateFormat;
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
    public String curLink = "";
    public String author;
    public String category;
    public String subCategory;
    public String imgUrl;
    public List<String> keywords;
    public boolean has_posted = false;
    public int numLinks = 0;
    transient Elements links;

    static final ChineseTrans chineseTrans = new ChineseTrans();
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat dateFormatLong = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public HashMap buildPub() {
        HashMap hmContent = new HashMap();
        hmContent.put("title", trans_title);
        hmContent.put("description", trans_content);
        hmContent.put("post_status", "publish");
        hmContent.put("post_date", dateFormatLong.format(pub_date));
        hmContent.put("filename", String.format("%s-%s.md",
                dateFormat.format(pub_date),
                PinyinHelper.getShortPinyin(trans_title)
                        .replaceAll("[^a-zA-Z ]", "")
                        .replaceAll("\\s+", "")
                        .toLowerCase()));
        //Basically, we can put anything here as long as it match's wordpress's fields.;
//        if (numLinks > 4) {
//            hmContent.put("categories", new String[]{"头条", category});
//        } else {
//            hmContent.put("categories", new String[]{category});
//        }
        hmContent.put("category", category);
        hmContent.put("subcategory", subCategory);
        hmContent.put("mt_keywords", StringUtils.join(keywords.listIterator(), "\n- "));
        hmContent.put("dateCreated", pub_date);
        hmContent.put("date_modified", pub_date);
        hmContent.put("mt_text_more", linkUrl);
        hmContent.put("thumbnail", String.format("![%s](%s)", category, imgUrl));
        hmContent.put("imgurl", String.format("%s", imgUrl));
        hmContent.put("link", String.format("%s", curLink));
//        HashMap thumbContent = new HashMap();
//        thumbContent.put("grid", "double");
//        hmContent.put("custom_fields", thumbContent);
//        hmContent.put("wp_post_thumbnail","http://www.tum.de/fileadmin/tu/layout/images/tumlogo.png");
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
        org_title = chineseTrans.toSimp(org_title);
        author = org_title.substring(org_title.lastIndexOf('-') + 1, org_title.length()).trim();
        result = org_title
                .replaceAll("-\\s.*?$", "")
                .replaceAll("^.*[：:]", "")
                .replaceAll(".*[\\(【《].*?[】\\)》]", "")
                .replaceAll("\"", "").trim();
        //result = org_title;
        result = chineseTrans.normalizeCAP(result, true);
        return result;
    }

    private String cleanContent(String org_content) {
        String result;
        Document doc = Jsoup.parse(org_content);
        links = doc.select("a[href]");
        result = chineseTrans.toSimp(doc.text())
                .replaceAll("\\.\\.\\..*$", "")
                .replaceAll("\"", "").trim();


        for (Element link : links) {
            if (link.attr("abs:href").matches(".*url=.*")) {
                String this_link = link.attr("abs:href").replaceAll(".*?url=", "");
                if (this_link.length() > 0) {
                    linkUrl += String.format("<p><a href=\"%s\" target=\"_blank\">[%s]</a></p>", this_link, link.text().trim());
                    curLink = this_link;
                    numLinks ++;
                }
            }
        }
        result = chineseTrans.normalizeCAP(result, true);
        return result;
    }

    public wpPost(String org_title, String org_content, Date pub_date,
                  rssFeed feed, HashMap<String, String> allowedKeys) {

        this.org_title = cleanTitle(org_title);
        this.org_content = cleanContent(org_content);
        this.org_content = this.org_content.replace(this.org_title, "")
                .replace(this.author, "").trim();
        this.pub_date = pub_date;
        this.is_translate = feed.needTranslate;
        // fetch date is current time
        this.fetch_date = new Date();
        this.category = feed.getCategory();
        this.subCategory = feed.getSubCategory();
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

        if (trans_content.length() > 1) {
            List<String> tags = new ArrayList<String>();
            for (Map.Entry<String, String> entry : allowedKeys.entrySet()) {
                if (trans_content.contains(entry.getKey())) {
                    tags.add(entry.getKey());
                }
            }
            this.keywords = tags;
        }
    }
}
