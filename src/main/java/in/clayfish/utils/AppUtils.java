package in.clayfish.utils;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
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
     *
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
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static CSVRecord readLastRecord(final File file) throws IOException {
        CSVParser csvParser = new CSVParser(new FileReader(file), CSVFormat.DEFAULT);

        long count = StreamSupport.stream(csvParser.spliterator(), false).count();

        if(count >0 ) {
            return readNthRecord(file, count - 1);
        }
        return null;
    }

    /**
     *
     * @param file
     * @param objects
     * @param <T>
     * @return
     */
    public static <T> void appendToCsv(final File file, final List<T> objects) throws IOException {
        if(!file.exists()) {
            boolean ignored = file.createNewFile();
        }
        CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(file), CSVFormat.DEFAULT);

        for(Object object : objects) {
            csvPrinter.printRecord(Collections.singleton(object));
        }

        csvPrinter.flush();
    }

}
