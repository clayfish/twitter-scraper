package in.clayfish;

import in.clayfish.utils.ApplicationProperties;

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

        System.out.println(props.getConnectionTimeout());
        System.out.println(props.getBaseUrl());
        System.out.println(props.getTargetUsername());
        System.out.println(props.getOutputFolder());
        System.out.println(props.getStateFile());

        new TwitterScraper(props).scrape();
    }

}
