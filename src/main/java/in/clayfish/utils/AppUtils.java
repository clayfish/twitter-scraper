package in.clayfish.utils;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Static utility-class for helper functions
 *
 * @author shuklaalok7
 * @since 16/01/16
 */
public abstract class AppUtils {

    /**
     * @param file
     * @param start
     * @param end
     * @return
     * @throws IOException
     */
    public static List<CSVRecord> readCsvFile(final File file, final long start, final long end) throws IOException {
        if (!file.exists() || start < 0 || end < 1) {
            throw new IllegalArgumentException(String.format("%s should exist, start(%d) should be greater than -1 and end(%d) should be greater than 0", file.getName(), start, end));
        }

        CSVParser csvParser = new CSVParser(new FileReader(file), CSVFormat.DEFAULT);

        return StreamSupport.stream(csvParser.spliterator(), false).skip(start).limit(end - start).collect(Collectors.toList());
    }

    /**
     * @param file
     * @return
     * @throws IOException
     */
    public static CSVRecord readFirstRecord(final File file) throws IOException {
        return readNthRecord(file, 0);
    }

    /**
     * @param file
     * @param n
     * @return
     * @throws IOException
     */
    public static CSVRecord readNthRecord(final File file, long n) throws IOException {
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
    public static CSVRecord readLastRecord(final File file) throws IOException {
        CSVParser csvParser = new CSVParser(new FileReader(file), CSVFormat.DEFAULT);

        long count = StreamSupport.stream(csvParser.spliterator(), false).count();

        if (count > 0) {
            return readNthRecord(file, count - 1);
        }
        return null;
    }

    /**
     * @param file
     * @param objects
     * @param <T>
     * @throws IOException
     */
    public static <T> void appendToCsv(final File file, final List<T> objects) throws IOException {
        writeToCsv(file, objects, true);
    }

    /**
     * @param file
     * @param objects
     * @param <T>
     * @return
     */
    public static <T> void writeToCsv(final File file, final List<T> objects, final boolean append) throws IOException {
        if (!file.exists()) {
            boolean created = file.createNewFile();

            if (!created) {
                throw new FileNotFoundException(file.getName() + " does not exist, nor could be created");
            }
        }
        CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(file, append), CSVFormat.DEFAULT);

        for (Object object : objects) {
            csvPrinter.printRecord(Collections.singleton(object));
        }

        csvPrinter.flush();
    }

    /**
     * Thread-safe
     *
     * @param step
     * @param props
     * @return
     */
    public static synchronized File createNewOutputFile(final int step, final ApplicationProperties props) {
        Objects.requireNonNull(props);

        String prefix = getOutputFilePrefix(step);
        int currentIndex = getCurrentOutputFileIndex(prefix, props);
        return new File(String.format("%s/%s%d.csv", props.getOutputFolder().getPath(), prefix, currentIndex + 1));
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
        return new File(String.format("%s/%s%d.csv", props.getOutputFolder().getPath(), prefix, currentIndex));
    }

    /**
     * @param prefix
     * @param props
     * @return
     */
    private static synchronized int getCurrentOutputFileIndex(final String prefix, final ApplicationProperties props) {
        int maxIndex = 0;
        for (File firstLevelOutputFile : props.getOutputFolder().listFiles((dir, name) -> name.startsWith(prefix)
                && name.endsWith(".csv"))) {
            String[] nameParts = firstLevelOutputFile.getName().split(IConstants.MINUS);
            int index = Converter.TO_INT.convert(nameParts[nameParts.length - 1].replace(".csv", IConstants.BLANK));

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
            case 0:
                return "first-level-";

            case 1:
                return "second-level-";

            default:
                throw new IllegalArgumentException("Wrong step value: " + step);
        }
    }

}
