import org.fnlp.app.keyword.AbstractExtractor;
import org.fnlp.app.keyword.WordExtract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class newsPoster {
    private static final wpPostComparator wpc = new wpPostComparator();
    private static final Logger LOG = LoggerFactory.getLogger(newsPoster.class);

    private static wpAccount account = new wpAccount();

    public static void main(String[] args){
        // write account info
//        try
//        {
//            LOG.info("Saving account info...");
//            FileOutputStream fileOut =
//                    new FileOutputStream("account.bin");
//            ObjectOutputStream out = new ObjectOutputStream(fileOut);
//            out.writeObject(account);
//            out.close();
//            fileOut.close();
//        } catch(Exception ex)
//        {
//            ex.printStackTrace();
//            return;
//        }
        final List<String> feedLists = new ArrayList<String>();
        try {
            FileInputStream fis = new FileInputStream("newslist");
            //Construct BufferedReader from InputStreamReader
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String line = null;
            while ((line = br.readLine()) != null) {
                feedLists.add(line);
            }
            br.close();

        } catch (IOException ex) {
            ex.printStackTrace();
            LOG.error("Can not found any news sources!");
        }
        LOG.info("Load {} sources", feedLists.size());


        // read account info
        try {
            FileInputStream fileIn = new FileInputStream("account.bin");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            account = (wpAccount) in.readObject();
            account.touch();
            in.close();
            fileIn.close();
        } catch (Exception ex) {
            LOG.info("No account info: where is account.bin???");
            ex.printStackTrace();
            return;
        }


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
//                wpPosts.addAll(new rssFeed("http://www.finanzen.net/rss/news", extractor, "实时快报", true).fetchSingleRSS());
//                wpPosts.addAll(new rssFeed("英国经济", extractor, "欧元区经济").fetchSingleRSS());
                for (int jj = 0; jj < feedLists.size(); jj++) {
                    String[] info = feedLists.get(jj).split(",");
                    wpPosts.addAll(new rssFeed(info[0].trim(), extractor, info[1].trim()).fetchSingleRSS());
                }
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
            if (!wp.has_posted) {
                LOG.info("Posting: {}", wp.trans_title);
                success += (account.publish(wp)) ? 1 : 0;
            }
        }
        LOG.info("Published: {} posts", success);
    }

}


