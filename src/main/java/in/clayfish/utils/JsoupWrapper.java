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
    private final int timeoutInMilliseconds;
    private final String userAgent;

    @Getter
    private Map<String, String> cookies;

    public JsoupWrapper(ApplicationProperties properties) {
        this.cookies = new HashMap<>();
        this.userAgent = properties.getUserAgent();
        this.timeoutInMilliseconds = properties.getConnectionTimeout();
    }

    public Connection connect(String url) {
        return Jsoup.connect(url).userAgent(userAgent)
                .timeout(timeoutInMilliseconds).cookies(this.cookies);
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
