package in.clayfish;

import in.clayfish.utils.ApplicationProperties;
import in.clayfish.utils.JsoupWrapper;

/**
 * @author shuklaalok7
 * @since 16/01/16
 */
public class TwitterScraper {

    private JsoupWrapper jsoupWrapper;
    private ApplicationProperties properties;


    public TwitterScraper(ApplicationProperties properties) {
        this.properties = properties;
        this.jsoupWrapper = new JsoupWrapper(properties);
    }

    public void scrape() {

    }

}
