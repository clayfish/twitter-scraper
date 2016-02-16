package in.clayfish.pyry.extractors;

import in.clayfish.pyry.models.Conversation;
import in.clayfish.pyry.models.Tweet;
import in.clayfish.pyry.utils.*;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author shuklaalok7
 * @since 18/01/16
 */
public class ConversationExtractor extends Extractor {

    private final static Logger logger = LogManager.getLogger(ConversationExtractor.class);

    private final File counterFile;
    private final String urlTemplate;
    private final int threadNumber;

    public ConversationExtractor(final ApplicationProperties props, final int threadNumber, final long recordsToProcess) throws IOException {
        super(props);
        this.threadNumber = threadNumber;
        counterFile = new File(String.format("%s/%s-%d.csv", props.getOutputFolder(), "counter-second-level", threadNumber));
        urlTemplate = String.format("https://twitter.com/%s/status/%%s", props.getTargetUsername());

        if (!counterFile.exists()) {
            boolean created = counterFile.createNewFile();
            if (!created) {
                throw new IllegalStateException("Thread " + threadNumber + ": second-level-counter.csv does not exist and could not be created.");
            }

            List<String> contents = new ArrayList<>();
            if (threadNumber < (props.getNumberOfConcurrentThreads() - 1)) {
                contents.add(String.format("1,%d,%d", threadNumber * recordsToProcess, (threadNumber + 1) * recordsToProcess));
            } else {
                contents.add(String.format("1,%d,%d", threadNumber * recordsToProcess, AppUtils.getLineCount(Converter.TO_FILE.apply(String.format("%s/first-level-1.csv", props.getOutputFolder())))));
            }
            AppUtils.writeToCsv(counterFile, contents, false);
        }

        jsoupWrapper = new JsoupWrapper(props, true);
    }

    @Override
    public void run() {
        int currentInputLineIndex, currentInputFileIndex;
        final int lastLineIndex;
        File currentInputFile, currentOutputFile;

        try {
            CSVRecord stateRecord = AppUtils.readFirstRecord(counterFile);
            currentInputFileIndex = Converter.TO_INT.apply(stateRecord.get(0));
            currentInputLineIndex = Converter.TO_INT.apply(stateRecord.get(1));
            lastLineIndex = Converter.TO_INT.apply(stateRecord.get(2));
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Could not find the line number to start fetching the conversations");
        }

        try {
            currentInputFile = new File(String.format("%s/first-level-%d.csv", props.getOutputFolder().getPath(), currentInputFileIndex));
            currentOutputFile = AppUtils.getCurrentOutputFile(2, threadNumber);

            while (currentInputFile.exists() && currentInputLineIndex < lastLineIndex) {
                if (Thread.interrupted()) {
                    try {
                        AppUtils.writeToCsv(counterFile, String.format("%d,%d,%d", currentInputFileIndex, currentInputLineIndex, lastLineIndex), false);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    logger.warn(MessageFormat.format("Thread {0}: {1} is interrupted", threadNumber, this.getClass().getSimpleName()));
                    break;
                }

                /*
                * The steps here are as following
                * 1. Read the current input file and line to read from counterFile
                * 2. Read the tweet ID at the specified line in the specified input file
                * 3. Fetch conversation involving that tweet
                * 4. Convert the html to tweets, give them same conversationId
                * 5. Save them in the outputFile
                *
                * */

                CSVRecord csvRecord = null;
                try {
                    csvRecord = AppUtils.readNthRecord(currentInputFile, currentInputLineIndex);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (csvRecord == null) {
                    currentInputFileIndex++;
                    currentInputFile = new File(String.format("%s/first-level-%d.csv", props.getOutputFolder().getPath(), currentInputFileIndex));
                    continue;
                }

                String tweetId = csvRecord.get(0);

                logger.debug(MessageFormat.format("Thread {0}: Line {1} - {2}", threadNumber, currentInputLineIndex, tweetId));
                Connection connection = jsoupWrapper.connect(String.format(urlTemplate, tweetId));
                final Document document;
                try {
                    document = jsoupWrapper.get(connection);
                } catch (IOException e) {
                    e.printStackTrace();
                    currentInputLineIndex++;
                    continue;
                }

                if (document == null) {
                    currentInputLineIndex++;
                    continue;
                }

                Conversation conversation1 = new Conversation(AppUtils.generateConversationId());
                for (Element streamItem : document.select("div.permalink-in-reply-tos li.stream-item")) {
                    conversation1.add(convertToTweet(streamItem));
                }

                // The tweet
                Element tweetBox = document.select("div.permalink-tweet-container .permalink-tweet").get(0);
                Element accountLink = tweetBox.select(".permalink-header a").get(0);

                Tweet tweet1 = new Tweet();
                tweet1.setId(Converter.TO_LONG.apply(tweetId));
                tweet1.setUser(accountLink.select(".fullname").text().replace("Verified account", IConstants.BLANK));
                tweet1.setUsername("@" + props.getTargetUsername());
                tweet1.setMessage(tweetBox.select("p.tweet-text").text());
                tweet1.setTimestamp(new Date(Converter.TO_LONG.apply(tweetBox.select(".time .js-relative-timestamp").attr("data-time-ms"))));
                tweet1.setLocation(tweetBox.select(".tweet-geo-text").text());
                conversation1.add(tweet1);

                for (Element streamItem : document.select("div.replies-to li.stream-item")) {
                    conversation1.add(convertToTweet(streamItem));
                }

                try {
                    AppUtils.writeToCsv(currentOutputFile, conversation1.toString(), true);
                    currentInputLineIndex++;
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (currentOutputFile.length() > IConstants.MB_12) {
                    logger.info("Thread " + threadNumber + ": Reached 24 MB limit. Creating new output file");
                    try {
                        currentOutputFile = AppUtils.createNewOutputFile(2, threadNumber);
//                        currentFileIndex = AppUtils.getCurrentOutputFileIndex(2, props);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Thread.currentThread().interrupt();
                    }
                }
            }

            try {
                AppUtils.writeToCsv(counterFile, String.format("%d,%d,%d", currentInputFileIndex, currentInputLineIndex, lastLineIndex), false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            try {
                AppUtils.writeToCsv(counterFile, String.format("%d,%d,%d", currentInputFileIndex, currentInputLineIndex, lastLineIndex), false);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    /**
     * Returns tweet having conversationId not set
     *
     * @param streamItem    Found DOM element containing tweet
     * @return Our persistent tweet object
     */
    private Tweet convertToTweet(Element streamItem) {
        Tweet tweet = new Tweet();

        StringBuilder message = new StringBuilder();
        Element aElement = streamItem.select(".content .stream-item-header a").get(0);

        Elements texts = streamItem.select(".content p.tweet-text");
        if (!texts.isEmpty()) {
            message.append(texts.get(0).text()).append(" ");
        }

        tweet.setId(Converter.TO_LONG.apply(streamItem.attr("data-item-id")));
        tweet.setUser(aElement.select(".fullname").text().replace("Verified account", IConstants.BLANK));
        tweet.setUsername(aElement.select(".username").text());
        tweet.setMessage(message.toString());
        tweet.setLocation(streamItem.select(".stream-item-footer .tweet-geo-text").text());
        tweet.setTimestamp(new Date(Converter.TO_LONG.apply(streamItem.select(".stream-item-header .time .js-short-timestamp").attr("data-time-ms"))));

        return tweet;
    }
}
