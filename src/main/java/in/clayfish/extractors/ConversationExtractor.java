package in.clayfish.extractors;

import in.clayfish.utils.AppUtils;
import in.clayfish.utils.ApplicationProperties;
import in.clayfish.utils.Converter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author shuklaalok7
 * @since 18/01/16
 */
public class ConversationExtractor extends Extractor {

    private final File counterFile;
    public ConversationExtractor(final ApplicationProperties props) throws IOException {
        super(props);
        counterFile = new File(String.format("%s/%s", props.getOutputFolder(), "second-level-counter.csv"));

        if(!counterFile.exists()) {
            boolean created = counterFile.createNewFile();
             if(!created) {
                 throw new IllegalStateException("second-level-counter.csv does not exist and could not be created.");
             }

            List<String> contents = new ArrayList<>();
            contents.add("0");
            AppUtils.writeToCsv(counterFile, contents, false);
        }
    }

    @Override
    public void run() {
        int currentLineNumber;
        try {
            currentLineNumber = Converter.TO_INT.convert(AppUtils.readNthRecord(counterFile, 2L).get(0));
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Could not find the line number to start fetching the conversations");
        }


        while(true) {
            if(Thread.interrupted()) {
                try {
                    AppUtils.writeToCsv(counterFile, Collections.singletonList(currentLineNumber), false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println(this.getClass().getName()+" is interrupted");
                break;
            }

            /*
            * The steps here are as following
            * 1. Read the line at current index
             *
            * */

        }
    }
}
