import org.fnlp.app.keyword.AbstractExtractor;
import org.fnlp.app.keyword.WordExtract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redstone.xmlrpc.XmlRpcClient;
import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcFault;

import java.io.*;
import java.net.MalformedURLException;
import java.util.*;

public class newsPoster {
    private static final wpPostComparator wpc = new wpPostComparator();
    private static final Logger LOG = LoggerFactory.getLogger(newsPoster.class);
    private final String xmlRpcUrl = "http://54.93.32.189/xmlrpc.php";
    private final String username = "hanxiao";
    private final String password = "xh870531";
    //First step, init an client
    private XmlRpcClient client;

    public newsPoster() {
        try {
            this.client = new XmlRpcClient(xmlRpcUrl, true);
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        Timer timer = new Timer ();
        final int rumTimes = 0;
        TimerTask hourlyTask = new TimerTask () {
            @Override
            public void run () {
                LOG.info("{} run at {}", rumTimes,  new Date().toString());
                LOG.info("loading FNLP model...");
                AbstractExtractor extractor = null;
                try {
                    extractor = new WordExtract("model/seg.m", "model/stopwords");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                newsPoster np = new newsPoster();
                List<wpPost> wpPosts = null;
                try
                {
                    FileInputStream fileIn = new FileInputStream("previous_posts");
                    ObjectInputStream in = new ObjectInputStream(fileIn);
                    wpPosts = (ArrayList<wpPost>) in.readObject();
                    in.close();
                    fileIn.close();
                } catch (FileNotFoundException ex) {
                    LOG.info("No previous posts.");
                    wpPosts = new ArrayList<wpPost>();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    LOG.info("New data structure is used, clear previous posts.");
                    wpPosts = new ArrayList<wpPost>();
                }
                // keyword to search on google, extractor, category in WP[MUST EXISTS in WP]
                wpPosts.addAll(new rssFeed("英国经济", extractor, "欧元区经济").fetchSingleRSS());
                wpPosts.addAll(new rssFeed("法国经济", extractor, "欧元区经济").fetchSingleRSS());
                wpPosts.addAll(new rssFeed("瑞士经济", extractor, "欧元区经济").fetchSingleRSS());
                wpPosts.addAll(new rssFeed("德国经济", extractor, "德国经济").fetchSingleRSS());
                wpPosts.addAll(new rssFeed("欧元", extractor, "欧元区经济").fetchSingleRSS());
                wpPosts.addAll(new rssFeed("欧洲央行", extractor, "欧洲央行").fetchSingleRSS());
                wpPosts.addAll(new rssFeed("DAX 股市", extractor, "德国股市").fetchSingleRSS());
                wpPosts.addAll(new rssFeed("伦敦交易所", extractor, "欧洲股市").fetchSingleRSS());
                wpPosts.sort(wpc);
                List<wpPost> uniPosts =
                        new ArrayList<wpPost>(new LinkedHashSet<wpPost>(wpPosts));

                np.postNews(uniPosts);
                try
                {
                    FileOutputStream fileOut =
                            new FileOutputStream("previous_posts");
                    ObjectOutputStream out = new ObjectOutputStream(fileOut);
                    out.writeObject(uniPosts);
                    out.close();
                    fileOut.close();
                    LOG.info("Saving posts...");
                }catch(Exception ex)
                {
                    ex.printStackTrace();
                }
                LOG.info("waiting for the update in next hour!");
            }
        };

        // schedule the task to run starting now and then every hour...
        timer.schedule (hourlyTask, 0l, 1000*60*60);
    }

    public void postNews(List<wpPost> wpPosts){
        int success = 0;
        for (wpPost wp : wpPosts) {
            try {
                if (!wp.has_posted) {
                    LOG.info("Posting {}", wp.trans_title);
                    client.invoke("metaWeblog.newPost",
                            new Object[] {new Integer(1), username,password,
                                    wp.buildPub(),
                                    true});
                    success ++;
                }
            } catch (XmlRpcException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (XmlRpcFault e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        LOG.info("Published: {} posts", success);
    }

}


