import redstone.xmlrpc.XmlRpcClient;
import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcFault;

import java.io.Serializable;
import java.net.MalformedURLException;

/**
 * Created by hxiao on 12/17/14.
 */
public class wpAccount implements Serializable {
    private transient XmlRpcClient client;
    private final String xmlRpcUrl = "http://54.93.32.189/xmlrpc.php";
    private final String username = "hanxiao";
    private final String password = "xh870531";

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

    public boolean publish(wpPost wp) {
        try {
            client.invoke("metaWeblog.newPost",
                    new Object[]{1, username, password,
                            wp.buildPub(),
                            true});
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
