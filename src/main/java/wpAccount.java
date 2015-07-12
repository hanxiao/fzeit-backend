import redstone.xmlrpc.XmlRpcClient;
import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcFault;

import java.io.*;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

/**
 * Created by hxiao on 12/17/14.
 */
public class wpAccount implements Serializable {
    private transient XmlRpcClient client;
    private final String xmlRpcUrl = "http://ojins.com/xmlrpc.php";
    private final String username = "hanxiao";
    private final String password = "xh870531";
    private final String localPath = "/Users/hxiao/Documents/ojins-com/source/_posts/";

    public wpAccount() {
        touch();
    }

    public void touch() {
        try {
            this.client = new XmlRpcClient(xmlRpcUrl, true);
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void publishItem(wpPost wp, HtmlReplaceString htmlReplaceString) {
        HashMap wpInfo = wp.buildPub();
        htmlReplaceString.contentStr.append(String.format("\"%s\",\n",
                wpInfo.get("description")));
        htmlReplaceString.datetimeStr.append(String.format("\"%s\",\n",
                wpInfo.get("post_date")));
        htmlReplaceString.titleStr.append(String.format("\"%s\",\n",
                wpInfo.get("title")));
        htmlReplaceString.publisherStr.append(String.format("\"%s\",\n",
                wpInfo.get("subcategory")));
        htmlReplaceString.imageStr.append(String.format("\"%s\",\n",
                wpInfo.get("imgurl")));
        htmlReplaceString.linkStr.append(String.format("\"%s\",\n",
                wpInfo.get("link")));
    }

    static String readFile(String path, Charset encoding)
            throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    public void publishHTML(String inFile, String outFile, HtmlReplaceString htmlReplaceString) {
        try {
            String content = readFile(inFile, Charset.defaultCharset());
            content = content.replace("#1", htmlReplaceString.publisherStr);
            content = content.replace("#2", htmlReplaceString.datetimeStr);
            content = content.replace("#3", htmlReplaceString.titleStr);
            content = content.replace("#4", htmlReplaceString.contentStr);
            content = content.replace("#5", htmlReplaceString.imageStr);
            content = content.replace("#6", htmlReplaceString.linkStr);

            Writer writer = new FileWriter(new File(outFile));
            writer.write(content);
            writer.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }


    }


    public boolean publishLocal(wpPost wp) {
        try {
            HashMap wpInfo = wp.buildPub();
            Writer writer = new FileWriter(new File(String.format("%s%s", localPath, wpInfo.get("filename"))));
            writer.write("---\n");
            writer.write("layout: post\n");
            writer.write(String.format("title:  %s\n", wpInfo.get("title")));
            writer.write(String.format("date:  %s\n", wpInfo.get("post_date")));
            writer.write(String.format("categories:\n- %s\n", wpInfo.get("category")));
            writer.write(String.format("- %s\n", wpInfo.get("subcategory")));
            writer.write(String.format("tags:\n- %s\n", wpInfo.get("mt_keywords")));
            writer.write("---\n");
            writer.write(String.format("%s\n", wpInfo.get("thumbnail")));
            writer.write(String.format("%s\n", wpInfo.get("description")));
            writer.write(String.format("%s", wpInfo.get("mt_text_more")));
            writer.flush();
            writer.close();
            wp.has_posted = true;

        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean publish(wpPost wp) {
        try {
            String postid = (String) client.invoke("metaWeblog.newPost",
                    new Object[]{1, username, password,
                            wp.buildPub(),
                            true});
            wp.postid = postid;
            wp.has_posted = true;
        } catch (XmlRpcException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        } catch (XmlRpcFault e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
        return true;

    }

    public String getXmlRpcUrl() {
        return xmlRpcUrl;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }


}
