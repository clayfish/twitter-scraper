package in.clayfish;

import in.clayfish.extractors.ConversationExtractor;
import in.clayfish.utils.ApplicationProperties;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

        ExecutorService executorService = Executors.newFixedThreadPool(props.getNumberOfConcurrentThreads());
//        executorService.submit(new TweetIdExtractor(props));

        for(int i=0; i< props.getNumberOfConcurrentThreads(); i++) {
            executorService.submit(new ConversationExtractor(props, i, 1027));
        }

        // We may want to spawn more than one conversationExtractors one for each first-level output file
//        executorService.submit(new ConversationExtractor(props));

        // Adding a shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!executorService.isTerminated()) {
                System.out.println("Performing some shutdown cleanup...");
                executorService.shutdownNow();
                while (true) {
                    try {
                        System.out.println("Waiting for the service to terminate...");
                        if (executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                            break;
                        }
                    } catch (InterruptedException ignored) {
                    }
                }
                System.out.println("Done cleaning");
            }
            System.out.println(String.format("Process took %d seconds", (System.currentTimeMillis() - startTime) / 1000));
        }));

        // Shutting down the executorService. This shuts down when it has finished running all the submitted jobs and do not accept any more jobs.
        executorService.shutdown();
        System.out.println("Waiting for debugger");
    }

}
