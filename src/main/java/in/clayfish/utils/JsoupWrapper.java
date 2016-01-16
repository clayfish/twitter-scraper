package in.clayfish.utils;

import lombok.Getter;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author shuklaalok7
 * @since 8/4/15 00:55
 */
public class JsoupWrapper {
    private static final int TIMEOUT_IN_MILLISECONDS = 30000;
    private static final String USER_AGENT_STRING = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.132 Safari/537.36";

    @Getter
    private Map<String, String> cookies;
    @Getter
    private boolean useProxy;

    public JsoupWrapper() {
        this.cookies = new HashMap<>();
    }

    public JsoupWrapper(boolean useProxy) {
        this();
        this.useProxy = useProxy;
    }

    public Connection connect(String url) {
        return Jsoup.connect(url).userAgent(USER_AGENT_STRING)
                .timeout(TIMEOUT_IN_MILLISECONDS).cookies(this.cookies);
    }

    public Connection.Response execute(Connection connection, Connection.Method method) {
        Connection.Response response;
        //        setSystemProperties();

        if (method != null) {
            connection.method(method);
        }

        try {
            response = connection.execute();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        this.cookies = response.cookies();
        return response;
    }

    private void setSystemProperties() {
        if (useProxy) {
            System.setProperty("http.proxyHost", "67.212.175.123");
            System.setProperty("http.proxyPort", "80");
            System.setProperty("http.proxyUser", "freevpnaccess.com");
            System.setProperty("http.proxyPassword", "4168");
        } else {
            System.clearProperty("http.proxyHost");
            System.clearProperty("http.proxyPort");
            System.clearProperty("http.proxyUser");
            System.clearProperty("http.proxyPassword");
        }
    }

    public Connection.Response execute(Connection connection) {
        return this.execute(connection, Connection.Method.GET);
    }

    public Document get(Connection connection) throws IOException {
        return this.execute(connection).parse();
    }

    public Document post(Connection connection) throws IOException {
        return this.execute(connection, Connection.Method.POST).parse();
    }

}
