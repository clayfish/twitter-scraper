package in.clayfish.pyry;

import in.clayfish.pyry.utils.AppUtils;
import in.clayfish.pyry.utils.ApplicationProperties;

import java.io.IOException;

/**
 * @author shuklaalok7
 * @since 16/01/16
 */
public class Application {
    private static String propertiesFile = "config/application.properties";

    /**
     * Starting point
     *
     * @param args command-line arguments
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        ApplicationProperties props = new ApplicationProperties(propertiesFile);

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
        System.out.println(String.format("First: %d\tLast: %d", AppUtils.getLatestTweetIdFetched(1), AppUtils.getOldestTweetIdFetched(1)));

        new TwitterScraper(props).scrape();
    }

}
