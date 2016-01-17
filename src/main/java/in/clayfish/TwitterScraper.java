package in.clayfish;

import in.clayfish.utils.*;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author shuklaalok7
 * @since 16/01/16
 */
public class TwitterScraper {

    private ApplicationProperties props;

    private long startTime;

    public TwitterScraper(ApplicationProperties props) {
        this.props = props;
    }

    public void scrape() throws IOException {
        startTime = System.currentTimeMillis();

        // Initial document
        ExecutorService executorService = Executors.newFixedThreadPool(props.getNumberOfConcurrentThreads());
        executorService.execute(new TweetIdExtractor(props));

        System.out.println("Waiting for debugger");
    }

    /**
     * First-level extractor
     */
    class TweetIdExtractor implements Runnable {

        private final JsoupWrapper jsoupWrapper;
        private final String username;
        private final String urlTemplate;
        private long startingTweetId;

        public TweetIdExtractor(final ApplicationProperties props) {
            this.username = props.getTargetUsername();
            this.urlTemplate = String.format("https://twitter.com/i/profiles/show/%s/timeline/with_replies?include_available_features=1&include_entities=1&last_note_ts=123&max_position=%%d&reset_error_state=false",
                    username);

            if (props.isToContinue()) {
                File latestOutputFile = null;
                int largestIndex = 0;
                for (File firstLevelOutputFile : props.getOutputFolder().listFiles((dir, name) -> name.startsWith("first-level-") && name.endsWith(".csv"))) {
                    String[] nameParts = firstLevelOutputFile.getName().split(IConstants.MINUS);
                    int index = Converter.TO_INT.convert(nameParts[nameParts.length - 1].replace(".csv", IConstants.BLANK));

                    if (index > largestIndex) {
                        largestIndex = index;
                        latestOutputFile = firstLevelOutputFile;
                    }
                }

                if (latestOutputFile != null) {
                    try {
                        this.startingTweetId = Converter.TO_LONG.convert(AppUtils.readLastRecord(latestOutputFile).get(0));
                    } catch (IOException e) {
                        e.printStackTrace();
                        this.startingTweetId = props.getStartingTweetId();
                    }
                } else {
                    this.startingTweetId = props.getStartingTweetId();
                }
            } else {
                this.startingTweetId = props.getStartingTweetId();
            }

            try {
                this.jsoupWrapper = new JsoupWrapper(props, true);
            } catch (IOException e) {
                e.printStackTrace();
                throw new IllegalStateException(e);
            }
        }

        @Override
        public void run() {
            final String label = Thread.currentThread().getName();
            System.out.println("Thread name: " + label);

            System.out.println("startingTweetId: "+startingTweetId);
            System.out.println("urlTemplate: "+urlTemplate);


            /* The steps are -
             1. Fetch the url created by urlTemplate and startingTweetId
             2. run decodeURI for the received response
             3. Get all the li.stream-item and retrieve data-tweet-id
             4. Save that tweet-id to "first-level-<thread-name>-1.csv" file in output folder
             5. When that CSV file has got 24 Mb, start saving in "first-level-<thread-name>-2.csv" and so on
             6. At the end of it you'll have a lot of tweet IDs with replies to look into

            * */
        }
    }

}
