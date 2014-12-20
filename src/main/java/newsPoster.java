import org.fnlp.app.keyword.AbstractExtractor;
import org.fnlp.app.keyword.WordExtract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class newsPoster {

    private static final Logger LOG = LoggerFactory.getLogger(newsPoster.class);

    private static wpAccount account = new wpAccount();
    private static final AbstractExtractor extractor;

    static {
        AbstractExtractor tmp = null;
        try {
            LOG.info("Loading FNLP model...");
            tmp = new WordExtract("model/seg.m", "model/stopwords");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        extractor = tmp;
    }

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

        final List<String> feedLists = new ArrayList<String>();


        Timer timer = new Timer ();
        TimerTask hourlyTask = new TimerTask () {
            @Override
            public void run () {
                feedLists.clear();
                try {
                    LOG.info("Loading news sources!");
                    FileInputStream fis = new FileInputStream("newslist");
                    //Construct BufferedReader from InputStreamReader
                    BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        feedLists.add(line);
                    }
                    br.close();
                    fis.close();

                } catch (IOException ex) {
                    ex.printStackTrace();
                    LOG.error("Can not found any news sources!");
                }
                LOG.info("Load {} sources", feedLists.size());

                LOG.info("Update starts at {}", new Date().toString());

                newsPoster np = new newsPoster();
                wpPostList wpPosts = null;
                try
                {
                    FileInputStream fileIn = new FileInputStream("history.bin");
                    ObjectInputStream in = new ObjectInputStream(fileIn);
                    wpPosts = (wpPostList) in.readObject();
                    in.close();
                    fileIn.close();
                } catch (FileNotFoundException ex) {
                    LOG.info("No previous posts.");
                    wpPosts = new wpPostList();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    LOG.info("New data structure is used, clear previous posts.");
                    wpPosts = new wpPostList();
                }
                // keyword to search on google, extractor, category in WP[MUST EXISTS in WP]
//                wpPosts.addAll(new rssFeed("http://www.finanzen.net/rss/news", extractor, "实时快报", true).fetchSingleRSS());
//                wpPosts.addAll(new rssFeed("英国经济", extractor, "欧元区经济").fetchSingleRSS());
                for (int jj = 0; jj < feedLists.size(); jj++) {
                    String[] info = feedLists.get(jj).split(",");
                    if (info == null) {
                        continue;
                    }
                    LOG.info(feedLists.get(jj));
                    wpPosts.addAll(new rssFeed(info[0].trim(), extractor, info[1].trim()).fetchSingleRSS());
                }
                wpPosts.publish(account);
                try
                {
                    LOG.info("Saving posts...");
                    FileOutputStream fileOut =
                            new FileOutputStream("history.bin");
                    ObjectOutputStream out = new ObjectOutputStream(fileOut);
                    out.writeObject(wpPosts);
                    out.close();
                    fileOut.close();
                }catch(Exception ex)
                {
                    ex.printStackTrace();
                }
                LOG.info("Update finished at {}, \n now " +
                        "waiting for the update in next hour!", new Date().toString());
            }
        };

        // schedule the task to run starting now and then every hour...
        timer.schedule (hourlyTask, 0l, 1000*60*60);
    }


}


