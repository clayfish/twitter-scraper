package in.clayfish.extractors;

import in.clayfish.utils.*;
import org.apache.commons.csv.CSVRecord;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;


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
        this.urlTemplate = String.format("https://twitter.com/i/profiles/show/%s/timeline/with_replies?include_available_features=1&include_entities=1&last_note_ts=123&max_position=%%d&reset_error_state=false",
                props.getTargetUsername());
        this.startingTweetId = props.getStartingTweetId();
        this.startingTweetId = this.getLastTweetId();

        try {
            this.jsoupWrapper = new JsoupWrapper(props, true);
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


        final String label = Thread.currentThread().getName();
        File currentOutputFile = AppUtils.getCurrentOutputFile(1, props);
        long currentTweetId = startingTweetId;

        System.out.println("Started thread: " + label);
        System.out.println("startingTweetId: " + startingTweetId);

        // Keep fetching and writing the tweet IDs
        while (true) {
            // Only way out is when we get interrupted from outside the thread
            if (Thread.interrupted()) {
                System.out.println("TweetIdExtractor is interrupted.");
                break;
            }

            if (currentOutputFile.length() > IConstants.MB_24) {
                System.out.println(currentOutputFile.getName() + " is overflowing, writing to new file now.");
                currentOutputFile = AppUtils.createNewOutputFile(1, props);
            }

            Connection connection = jsoupWrapper.connect(String.format(urlTemplate, currentTweetId));
            Connection.Response response = jsoupWrapper.execute(connection);
            Document document;
            try {
                document = Jsoup.parse((String) ((JSONObject) new JSONParser().parse(response.body())).get("items_html"));
            } catch (ParseException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
                continue;
            }
            List<String> tweetIds = document.select("li.stream-item").stream().map(element -> element.attr("data-item-id")).collect(Collectors.toList());
            System.out.println("Found "+ tweetIds.size() + " new tweets with replies.");

            try {
                AppUtils.appendToCsv(currentOutputFile, tweetIds);
            } catch (IOException e) {
                e.printStackTrace();
            }

            currentTweetId = Converter.TO_LONG.convert(tweetIds.get(tweetIds.size() - 1));
        }

    }


    /**
     * @return
     */
    private long getLastTweetId() {
        long lastTweetId = startingTweetId;

        File currentOutputFile = AppUtils.getCurrentOutputFile(1, props);

        if (currentOutputFile != null) {
            try {
                CSVRecord lastRecord = AppUtils.readLastRecord(currentOutputFile);
                if (lastRecord != null) {
                    lastTweetId = Converter.TO_LONG.convert(lastRecord.get(0));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return lastTweetId;
    }
}
