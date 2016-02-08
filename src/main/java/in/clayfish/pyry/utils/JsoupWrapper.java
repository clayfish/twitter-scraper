package in.clayfish.pyry.utils;

import in.clayfish.pyry.enums.Mode;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class must be thread-safe
 *
 * @author shuklaalok7
 * @since 8/4/15 00:55
 */
public class JsoupWrapper {
    private final Logger logger = LogManager.getLogger(JsoupWrapper.class);
    private final int timeoutInMilliseconds;
    private final String userAgent;
    private final ApplicationProperties props;

    @Getter
    private Map<String, String> cookies;

    /**
     * @param props The properties set from the properties file
     */
    public JsoupWrapper(final ApplicationProperties props, final boolean initialize) throws IOException {
        this.props = props;
        this.cookies = new HashMap<>();
        this.userAgent = props.getUserAgent();
        this.timeoutInMilliseconds = props.getConnectionTimeout();

        if (initialize) {
            this.init();
        }
    }

    /**
     * @param url The URL to connect
     * @return The Jsoup connection object
     */
    public Connection connect(String url) {
        return Jsoup.connect(url).userAgent(userAgent)
                .header("Accept", "application/json")
                .ignoreContentType(true)
                .ignoreHttpErrors(true)
                .timeout(timeoutInMilliseconds).cookies(this.cookies);
    }

    /**
     * @param connection Jsoup connection object
     * @param method     HTTP method
     * @return Jsoup Connection.Response object
     */
    public Connection.Response execute(Connection connection, Connection.Method method) {
        Connection.Response response;

        if (method != null) {
            connection.method(method);
        }

        try {
            logger.debug("Calling " + connection.request().url());
            if (props.getMode() == Mode.TEST) {
                return null;
            }
            response = connection.execute();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        this.cookies.putAll(response.cookies());
        return response;
    }

    /**
     * @param connection
     * @return
     */
    public Connection.Response execute(Connection connection) {
        return this.execute(connection, Connection.Method.GET);
    }

    /**
     * @param connection
     * @return
     * @throws IOException
     */
    public Document get(Connection connection) throws IOException {
        return this.execute(connection).parse();
    }

    /**
     * @param connection
     * @return
     * @throws IOException
     */
    public Document post(Connection connection) throws IOException {
        return this.execute(connection, Connection.Method.POST).parse();
    }

    /**
     * Makes initial call to setup the cookies
     */
    public void init() throws IOException {
        Connection connection = this.connect(props.getBaseUrl());

        // This initial get requests sets our jsoupWrapper with appropriate cookies
        this.get(connection);
    }

}
