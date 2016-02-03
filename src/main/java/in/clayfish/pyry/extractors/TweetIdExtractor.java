package in.clayfish.pyry.extractors;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVRecord;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import in.clayfish.pyry.utils.*;


/**
 * First-level extractor
 *
 * @author shuklaalok7
 * @since 18/01/16
 */
public class TweetIdExtractor extends Extractor {

    private final JsoupWrapper jsoupWrapper;
    private final String urlTemplate;
    private long startingTweetId;

    public TweetIdExtractor(final ApplicationProperties props) {
        super(props);
        this.urlTemplate = String.format("https://twitter.com/i/search/timeline?f=tweets&vertical=default&q=from%%%%3A%s&src=typd&include_available_features=1&include_entities=1&last_note_ts=300&max_position=TWEET-%%d-%d-BD1UO2FFu9QAAAAAAAAETAAAAAcAAAASAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA&reset_error_state=false",
                props.getTargetUsername(), props.getStartingTweetId());
        this.startingTweetId = props.getStartingTweetId();
        this.startingTweetId = this.getLastFetchedTweetId();

        try {
            this.jsoupWrapper = new JsoupWrapper(props, false);

            // Following call is to set the mood of the wrapper
            Connection connection = this.jsoupWrapper.connect(String.format("https://twitter.com/search?f=tweets&vertical=default&q=from%%3A%s&src=typd",
                    props.getTargetUsername()));
            this.jsoupWrapper.get(connection);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void run() {
        /* The steps are -
         1. Fetch the url created by urlTemplate and startingTweetId
         2. run decodeURI for the received response
         3. Get all the li.stream-item and retrieve data-tweet-id
         4. Save that tweet-id to "first-level-<thread-name>-1.csv" file in output folder
         5. Repeat 1-4 with the tweet-id found at the last of the last first-level output file
         6. When that CSV file has got approximately 24 MB (25165824 bytes), start saving in "first-level-<thread-name>-2.csv" and so on
         7. At the end of it you'll have a lot of tweet IDs with replies to look into
        */


        final String label = String.format("Thread %d:", 0);
        File currentOutputFile = AppUtils.getCurrentOutputFile(1, props);
        long currentTweetId = startingTweetId;

        System.out.println(String.format("%s Started thread: %1$s", label));
        System.out.println(label+ " startingTweetId: " + startingTweetId);

        int reattempt=0;
        // Keep fetching and writing the tweet IDs until the last id, configured in application.properties is fetched
        for (boolean lastTweetIdFetched = false; !lastTweetIdFetched || reattempt>5;) {
            // Only way out is when we get interrupted from outside the thread
            if (Thread.interrupted()) {
                System.out.println("TweetIdExtractor is interrupted.");
                break;
            }

            if (currentOutputFile.length() > IConstants.MB_24) {
                System.out.println(currentOutputFile.getName() + " is overflowing, writing to new file now.");
                try {
                    currentOutputFile = AppUtils.createNewOutputFile(1, props);
                } catch (IOException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }
            Document document = null;

            for(int reattempts = 0; reattempts < 3;) {
                Connection connection = jsoupWrapper.connect(String.format(urlTemplate, currentTweetId));
                Connection.Response response = jsoupWrapper.execute(connection);
                try {
                    document = Jsoup.parse((String) ((JSONObject) new JSONParser().parse(response.body())).get("items_html"));
                    break;
                } catch (ParseException e) {
                    e.printStackTrace();
                    reattempts++;
                }
            }

            if(document == null) {
                Thread.currentThread().interrupt();
                continue;
            }

            List<String> tweetIds = document.select("li.stream-item").stream().map(element -> element.attr("data-item-id")).collect(Collectors.toList());

            if (props.getLastTweetId() != null && !props.getLastTweetId().isEmpty() && tweetIds.contains(props.getLastTweetId())) {
                tweetIds = tweetIds.subList(0, tweetIds.indexOf(props.getLastTweetId()));
                lastTweetIdFetched = true;
            }

            System.out.println(String.format("%s Found %d new tweets with replies.", label, tweetIds.size()));

            if(tweetIds.size() == 0) {
                reattempt++;
                continue;
            }

            try {
                AppUtils.appendToCsv(currentOutputFile, tweetIds);
            } catch (IOException e) {
                e.printStackTrace();
            }

            currentTweetId = Converter.TO_LONG.apply(tweetIds.get(tweetIds.size() - 1));
        }

    }


    /**
     * @return last fetched ID found in the output folder
     */
    private synchronized long getLastFetchedTweetId() {
        long lastTweetId = startingTweetId;

        File currentOutputFile = AppUtils.getCurrentOutputFile(1, props);

        if (currentOutputFile != null) {
            try {
                CSVRecord lastRecord = AppUtils.readLastRecord(currentOutputFile);
                if (lastRecord != null) {
                    lastTweetId = Converter.TO_LONG.apply(lastRecord.get(0));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return lastTweetId;
    }
}
