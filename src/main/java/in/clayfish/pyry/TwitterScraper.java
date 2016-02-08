package in.clayfish.pyry;

import in.clayfish.pyry.extractors.ConversationExtractor;
import in.clayfish.pyry.extractors.TweetIdExtractor;
import in.clayfish.pyry.utils.AppUtils;
import in.clayfish.pyry.utils.ApplicationProperties;
import in.clayfish.pyry.utils.Converter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author shuklaalok7
 * @since 16/01/16
 */
public class TwitterScraper {
    private static final Logger logger = LogManager.getLogger(TwitterScraper.class);

    private ApplicationProperties props;

    private long startTime;

    public TwitterScraper(ApplicationProperties props) {
        this.props = props;
    }

    public void scrape() throws IOException {
        startTime = System.currentTimeMillis();

        ExecutorService executorService = Executors.newFixedThreadPool(props.getNumberOfConcurrentThreads());
        if (props.getStep() == 1) {
            executorService.submit(new TweetIdExtractor(props));
        } else {
            final long totalRecords = AppUtils.getLineCount(Converter.TO_FILE.apply(String.format("%s/first-level-1.csv", props.getOutputFolder())));
            final long recordsToProcess = totalRecords/props.getNumberOfConcurrentThreads();
            for (int i = 0; i < props.getNumberOfConcurrentThreads(); i++) {
                executorService.submit(new ConversationExtractor(props, i, recordsToProcess));
            }
        }

        // We may want to spawn more than one conversationExtractors one for each first-level output file
//        executorService.submit(new ConversationExtractor(props));

        // Adding a shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!executorService.isTerminated()) {
                logger.debug("Performing some shutdown cleanup...");
                executorService.shutdownNow();
                while (true) {
                    try {
                        logger.debug("Waiting for the service to terminate...");
                        if (executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                            break;
                        }
                    } catch (InterruptedException ignored) {
                    }
                }
                logger.debug("Done cleaning");
            }
            logger.info(String.format("Process took %d seconds", (System.currentTimeMillis() - startTime) / 1000));
        }));

        // Shutting down the executorService. This shuts down when it has finished running all the submitted jobs and do not accept any more jobs.
        executorService.shutdown();
        logger.trace("Waiting for debugger");
    }

}
