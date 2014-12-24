import org.fnlp.nlp.similarity.EditDistance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hxiao on 12/20/14.
 */
public class wpPostList implements Serializable {
    private static final Logger LOG = LoggerFactory.getLogger(newsPoster.class);
    List<wpPost> allPosts = new ArrayList<wpPost>();
    transient EditDistance ed = new EditDistance();
    private static final wpPostComparator wpc = new wpPostComparator();

    private void sort() {
        allPosts.sort(wpc);
    }

    public void addAll(List<wpPost> wpPosts) {
        if (ed == null) {
            ed = new EditDistance();
        }
        //List<wpPost> newPosts = new ArrayList<wpPost>();
        for (wpPost wp_new : wpPosts) {
            boolean isDuplicate = false;
            for (wpPost wp_old : allPosts) {
                if (ed.sim(wp_old.trans_title, wp_new.trans_title) > 0.7 ||
                        ed.sim(wp_old.trans_content, wp_new.trans_content) > 0.7) {
                    // probably report the same news
                    isDuplicate = true;
                    if (!wp_old.has_posted) {
                        LOG.info("{} is similar to {}, merged!", wp_old.trans_title, wp_new.trans_title);
                        wp_old.numLinks += wp_new.numLinks;
                        wp_old.linkUrl += "<p>" + wp_new.trans_content + "<p>" + wp_new.linkUrl;
                    }
                    break;
                }
            }
            if (!isDuplicate && wp_new.trans_title.trim().length() > 0) {
                allPosts.add(wp_new);
            }
        }
        //allPosts.addAll(newPosts);
    }

    public void publish(wpAccount account) {
        sort();
        int success = 0;
        for (wpPost wp : allPosts) {
            if (!wp.has_posted && wp.trans_content.length() > 1) {
                LOG.info("Posting: {}", wp.trans_title);
                success += (account.publish(wp)) ? 1 : 0;
            }
        }
        LOG.info("Published: {} posts", success);
    }
}
