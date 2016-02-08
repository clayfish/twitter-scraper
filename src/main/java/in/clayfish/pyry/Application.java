package in.clayfish.pyry;

import in.clayfish.pyry.utils.AppUtils;
import in.clayfish.pyry.utils.ApplicationProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * @author shuklaalok7
 * @since 16/01/16
 */
public class Application {
    /**
     * Starting point
     *
     * @param args command-line arguments
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        System.setProperty("log4j.configurationFile", "config/log4j2.xml");
        System.setProperty("twitter-scraper.configurationFile", "config/application.properties");

        Logger logger = LogManager.getLogger(Application.class);

        ApplicationProperties props = new ApplicationProperties();

        if (props.getOutputFolder().exists()) {
            if (!props.getOutputFolder().isDirectory()) {
                throw new IllegalStateException("Please check output folder: " + props.getOutputFolder().getPath());
            }
        } else {
            boolean created = props.getOutputFolder().mkdirs();
            if (!created) {
                throw new IllegalStateException("Cannot create output folder: " + props.getOutputFolder().getPath());
            }
        }

        AppUtils.initialize(props);
        logger.debug(String.format("First: %d\tLast: %d", AppUtils.getLatestTweetIdFetched(1), AppUtils.getOldestTweetIdFetched(1)));

        new TwitterScraper(props).scrape();
    }

}
