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

    public void sort() {
        allPosts.sort(wpc);
    }

    public void addAll(List<wpPost> wpPosts) {
        for (wpPost wp_new : wpPosts) {
            boolean isDuplicate = false;
            for (wpPost wp_old : allPosts) {
                if (ed.sim(wp_old.trans_title, wp_new.trans_title) > 0.9 ||
                        ed.sim(wp_old.trans_content, wp_new.trans_content) > 0.9) {
                    // probably report the same news
                    LOG.info("{} is similar to {}, merged!", wp_old.trans_title, wp_new.trans_title);
                    wp_old.numLinks += wp_new.numLinks;
                    wp_old.linkUrl += "<p>" + wp_new.trans_content + "<p>" + wp_new.linkUrl;
                    isDuplicate = true;
                    break;
                }
            }
            if (!isDuplicate) {
                allPosts.add(wp_new);
            }
        }
    }

    public void publish(wpAccount account) {
        int success = 0;
        for (wpPost wp : allPosts) {
            if (!wp.has_posted) {
                LOG.info("Posting: {}", wp.trans_title);
                success += (account.publish(wp)) ? 1 : 0;
            }
        }
        LOG.info("Published: {} posts", success);
    }
}
