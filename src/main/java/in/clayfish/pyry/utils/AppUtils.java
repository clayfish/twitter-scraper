package in.clayfish.pyry.utils;

import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

/**
 * Static utility-class for helper functions
 *
 * @author shuklaalok7
 * @since 16/01/16
 */
public abstract class AppUtils {

    private static final CSVFormat CUSTOM = CSVFormat.DEFAULT.withQuote(null);

    /**
     * @param file
     * @param start
     * @param end
     * @return
     * @throws IOException
     */
    public static synchronized List<CSVRecord> readCsvFile(final File file, final long start, final long end) throws IOException {
        if (!file.exists() || start < 0 || end < 1) {
            throw new IllegalArgumentException(String.format(
                    "%s should exist, start(%d) should be greater than -1 and end(%d) should be greater than 0", file.getName(), start, end));
        }

        CSVParser csvParser = new CSVParser(new FileReader(file), CUSTOM);

        return StreamSupport.stream(csvParser.spliterator(), false).skip(start).limit(end - start).collect(Collectors.toList());
    }

    /**
     * @param file
     * @return
     * @throws IOException
     */
    public static synchronized CSVRecord readFirstRecord(final File file) throws IOException {
        return readNthRecord(file, 0);
    }

    /**
     * @param file
     * @param n
     * @return
     * @throws IOException
     */
    public static synchronized CSVRecord readNthRecord(final File file, long n) throws IOException {
        List<CSVRecord> result = readCsvFile(file, n, n + 1);
        if (result != null && !result.isEmpty()) {
            return result.get(0);
        }

        return null;
    }

    /**
     * @param file
     * @return
     * @throws IOException
     */
    public static synchronized CSVRecord readLastRecord(final File file) throws IOException {
        if (file.exists()) {
            CSVParser csvParser = new CSVParser(new FileReader(file), CUSTOM);

            long count = StreamSupport.stream(csvParser.spliterator(), false).count();

            if (count > 0) {
                return readNthRecord(file, count - 1);
            }
        }
        return null;
    }

    /**
     * @param file
     * @param objects
     * @param <T>
     * @throws IOException
     */
    public static synchronized <T> void appendToCsv(final File file, final List<T> objects) throws IOException {
        writeToCsv(file, objects, true);
    }

    public static synchronized <T> void writeToCsv(final File file, final T object, final boolean append) throws IOException {
        if (!file.exists()) {
            boolean created = file.createNewFile();

            if (!created) {
                throw new FileNotFoundException(file.getName() + " does not exist, nor could be created");
            }
        }
        CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(file, append), CUSTOM);
        if (object instanceof String) {
            System.out.println("Printing " + object);
            csvPrinter.print(object);
            csvPrinter.println();
        } else {
            csvPrinter.printRecord(Collections.singleton(object));
        }

        csvPrinter.flush();
    }

    /**
     * @param file
     * @param objects
     * @param <T>
     * @return
     */
    public static synchronized <T> void writeToCsv(final File file, final List<T> objects, final boolean append) throws IOException {
        if (!file.exists()) {
            boolean created = file.createNewFile();

            if (!created) {
                throw new FileNotFoundException(file.getName() + " does not exist, nor could be created");
            }
        }
        CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(file, append), CUSTOM);

        for (Object object : objects) {
            csvPrinter.printRecord(Collections.singleton(object));
        }

        csvPrinter.flush();
    }

    /**
     * @param step
     * @param props
     * @param threadNumber
     * @return
     * @throws IOException
     */
    public static synchronized File createNewOutputFile(final int step, final ApplicationProperties props, final int threadNumber) throws IOException {
        Objects.requireNonNull(props);

        if (threadNumber > props.getNumberOfConcurrentThreads()) {
            throw new IllegalStateException(String.format("Thread %d: Threads should be less than maximum number of threads(%d)", threadNumber,
                    props.getNumberOfConcurrentThreads()));
        }

        String prefix = getOutputFilePrefix(step);
        int currentIndex = getCurrentOutputFileIndex(prefix, props);
        File newOutputFile = new File(String.format("%s/%s%d-%d.csv", props.getOutputFolder().getPath(), prefix, threadNumber, currentIndex + 1));
        boolean created = newOutputFile.createNewFile();

        if (!created) {
            throw new IllegalStateException(String.format("Thread %d: Cannot create new output file for %s", threadNumber, getOutputFilePrefix(step)));
        }
        return newOutputFile;
    }

    /**
     * Thread-safe
     *
     * @param step
     * @param props
     * @return
     */
    public static synchronized File createNewOutputFile(final int step, final ApplicationProperties props) throws IOException {
        Objects.requireNonNull(props);

        String prefix = getOutputFilePrefix(step);
        int currentIndex = getCurrentOutputFileIndex(prefix, props);
        File newOutputFile = new File(String.format("%s/%s%d.csv", props.getOutputFolder().getPath(), prefix, currentIndex + 1));
        boolean created = newOutputFile.createNewFile();

        if (!created) {
            throw new IllegalStateException("Cannot create new output file for " + getOutputFilePrefix(step));
        }
        return newOutputFile;
    }

    /**
     * @param step
     * @param props
     * @param threadNumber
     * @return
     */
    public static synchronized File getCurrentOutputFile(final int step, final ApplicationProperties props, final int threadNumber) {
        Objects.requireNonNull(props);

        if (threadNumber > props.getNumberOfConcurrentThreads()) {
            throw new IllegalStateException(String.format("Thread %d: Threads should be less than maximum number of threads(%d)", threadNumber,
                    props.getNumberOfConcurrentThreads()));
        }

        String prefix = getOutputFilePrefix(step);
        int currentIndex = getCurrentOutputFileIndex(prefix, props);

        if (currentIndex == 0) {
            currentIndex++;
        }

        return new File(String.format("%s/%s%d-%d.csv", props.getOutputFolder().getPath(), prefix, threadNumber, currentIndex));
    }

    /**
     * @param step
     * @param props
     * @return
     */
    public static synchronized File getCurrentOutputFile(final int step, final ApplicationProperties props) {
        Objects.requireNonNull(props);

        String prefix = getOutputFilePrefix(step);
        int currentIndex = getCurrentOutputFileIndex(prefix, props);

        if (currentIndex == 0) {
            currentIndex++;
        }

        return new File(String.format("%s/%s%d.csv", props.getOutputFolder().getPath(), prefix, currentIndex));
    }

    public static synchronized int getCurrentOutputFileIndex(final int step, final ApplicationProperties props) {
        return getCurrentOutputFileIndex(getOutputFilePrefix(step), props);
    }

    /**
     * @param prefix
     * @param props
     * @return
     */
    public static synchronized int getCurrentOutputFileIndex(final String prefix, final ApplicationProperties props) {
        int maxIndex = 0;
        for (File firstLevelOutputFile : props.getOutputFolder().listFiles((dir, name) -> name.startsWith(prefix) && name.endsWith(".csv"))) {
            String[] nameParts = firstLevelOutputFile.getName().split(IConstants.MINUS);
            int index = Converter.TO_INT.apply(nameParts[nameParts.length - 1].replace(".csv", IConstants.BLANK));

            if (index > maxIndex) {
                maxIndex = index;
            }
        }

        return maxIndex;
    }

    /**
     * @param step
     * @return
     */
    private synchronized static String getOutputFilePrefix(final int step) {
        switch (step) {
            case 1:
                return "first-level-";

            case 2:
                return "second-level-";

            default:
                throw new IllegalArgumentException("Wrong step value: " + step);
        }
    }

}
