package in.clayfish.extractors;

import in.clayfish.utils.*;
import org.apache.commons.csv.CSVRecord;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author shuklaalok7
 * @since 18/01/16
 */
public class ConversationExtractor extends Extractor {

    private final JsoupWrapper jsoupWrapper;
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
                throw new IllegalStateException("Thread "+ threadNumber +": second-level-counter.csv does not exist and could not be created.");
            }

            List<String> contents = new ArrayList<>();
            if (threadNumber<(props.getNumberOfConcurrentThreads()-1)) {
                contents.add(String.format("1,%d,%d", threadNumber*recordsToProcess, (threadNumber+1)*recordsToProcess));
            } else {
                contents.add(String.format("1,%d,%d", threadNumber*recordsToProcess, 132437));
            }
            AppUtils.writeToCsv(counterFile, contents, false);
        }

        jsoupWrapper = new JsoupWrapper(props, true);
    }

    @Override
    public void run() {
        int currentLineIndex, currentFileIndex;
        final int lastLineIndex;
        File currentInputFile, currentOutputFile;

        try {
            CSVRecord stateRecord = AppUtils.readFirstRecord(counterFile);
            currentFileIndex = Converter.TO_INT.apply(stateRecord.get(0));
            currentLineIndex = Converter.TO_INT.apply(stateRecord.get(1));
            lastLineIndex = Converter.TO_INT.apply(stateRecord.get(2));
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Could not find the line number to start fetching the conversations");
        }

        currentInputFile = new File(String.format("%s/first-level-%d.csv", props.getOutputFolder().getPath(), currentFileIndex));
        currentOutputFile = AppUtils.getCurrentOutputFile(2, props, threadNumber);

        while (currentInputFile.exists() && currentLineIndex<lastLineIndex) {
            if (Thread.interrupted()) {
                try {
                    AppUtils.writeToCsv(counterFile, String.format("%d,%d,%d", currentFileIndex, currentLineIndex,lastLineIndex), false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println(String.format("Thread %d: %s is interrupted", threadNumber, this.getClass().getSimpleName()));
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
                csvRecord = AppUtils.readNthRecord(currentInputFile, currentLineIndex);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (csvRecord == null) {
                currentFileIndex++;
                currentInputFile = new File(String.format("%s/first-level-%d.csv", props.getOutputFolder().getPath(), currentFileIndex));
                continue;
            }

            String tweetId = csvRecord.get(0);

            System.out.println(String.format("Thread %d: Line %d - %s", threadNumber, currentLineIndex, tweetId));
            Connection connection = jsoupWrapper.connect(String.format(urlTemplate, tweetId));
            final Document document;
            try {
                document = jsoupWrapper.get(connection);
            } catch (IOException e) {
                e.printStackTrace();
                currentLineIndex++;
                continue;
            }

            if (document == null) {
                currentLineIndex++;
                continue;
            }

            StringBuilder conversation = new StringBuilder(tweetId).append(" ");
            for (Element streamItem : document.select("div.permalink-in-reply-tos li.stream-item")) {
                conversation.append(convertToString(streamItem));
            }

            // The tweet
            Element tweetBox = document.select("div.permalink-tweet-container .permalink-tweet").get(0);
            Element accountLink = tweetBox.select(".permalink-header a").get(0);
            conversation.append("<").append(accountLink.select(".username").text()).append(" ")
                    .append(props.getTargetUsername()).append("> ").append(tweetBox.select("p.tweet-text").text()).append(" ");

            for (Element streamItem : document.select("div.replies-to li.stream-item")) {
                conversation.append(convertToString(streamItem));
            }

            try {
                AppUtils.writeToCsv(currentOutputFile, conversation.toString(), true);
                currentLineIndex++;
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (currentOutputFile.length() > IConstants.MB_24) {
                System.out.println("Thread "+ threadNumber +": Reached 24 MB limit. Creating new output file");
                try {
                    currentOutputFile = AppUtils.createNewOutputFile(2, props, threadNumber);
                    currentFileIndex = AppUtils.getCurrentOutputFileIndex(2, props);
                } catch (IOException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }
        }

        try {
            AppUtils.writeToCsv(counterFile, String.format("%d,%d,%d", currentFileIndex, currentLineIndex, lastLineIndex), false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String convertToString(Element streamItem) {
        StringBuilder partialConversation = new StringBuilder("<");
        Element aElement = streamItem.select(".content .stream-item-header a").get(0);
        partialConversation.append(aElement.select(".fullname").text()).append(IConstants.SPACE)
                .append(aElement.select(".username").text()).append("> ");

        Elements texts = streamItem.select(".content p.tweet-text");
        if (!texts.isEmpty()) {
            partialConversation.append(texts.get(0).text()).append(" ");
        }

        return partialConversation.toString();
    }
}
