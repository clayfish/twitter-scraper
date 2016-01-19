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

    public ConversationExtractor(final ApplicationProperties props) throws IOException {
        super(props);
        counterFile = new File(String.format("%s/%s", props.getOutputFolder(), "counter-second-level.csv"));
        urlTemplate = String.format("https://twitter.com/%s/status/%%s", props.getTargetUsername());

        if (!counterFile.exists()) {
            boolean created = counterFile.createNewFile();
            if (!created) {
                throw new IllegalStateException("second-level-counter.csv does not exist and could not be created.");
            }

            List<String> contents = new ArrayList<>();
            contents.add("1,0");
            AppUtils.writeToCsv(counterFile, contents, false);
        }

        jsoupWrapper = new JsoupWrapper(props, true);
    }

    @Override
    public void run() {
        int currentLineIndex, currentFileIndex;
        File currentInputFile, currentOutputFile;

        try {
            CSVRecord stateRecord = AppUtils.readFirstRecord(counterFile);
            currentFileIndex = Converter.TO_INT.convert(stateRecord.get(0));
            currentLineIndex = Converter.TO_INT.convert(stateRecord.get(1));
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Could not find the line number to start fetching the conversations");
        }

        currentInputFile = new File(String.format("%s/first-level-%d.csv", props.getOutputFolder().getPath(), currentFileIndex));
        currentOutputFile = AppUtils.getCurrentOutputFile(2, props);

        while (currentInputFile.exists()) {
            if (Thread.interrupted()) {
                try {
                    AppUtils.writeToCsv(counterFile, String.format("%d,%d", currentFileIndex, currentLineIndex), false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println(this.getClass().getName() + " is interrupted");
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

            StringBuilder conversation = new StringBuilder();
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
                System.out.println("Reached 24 MB limit. Creating new output file");
                try {
                    currentOutputFile = AppUtils.createNewOutputFile(2, props);
                } catch (IOException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }
        }

        try {
            AppUtils.writeToCsv(counterFile, String.format("%d,%d", currentFileIndex, currentLineIndex), false);
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
