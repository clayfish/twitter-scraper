package in.clayfish.utils;

import in.clayfish.enums.Mode;
import in.clayfish.models.Tweet;

import java.io.File;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.Date;
import java.util.stream.Stream;

/**
 * @author shuklaalok7
 * @since 16/01/16
 */
@FunctionalInterface
public interface Converter<T, U> {
    Converter<String, Long> TO_LONG = (src) -> {
        if(src == null || src.isEmpty()) {
            return 0L;
        }
        return Long.parseLong(src);
    };

    Converter<String, Integer> TO_INT = (src) -> {
        if(src == null || src.isEmpty()) {
            return 0;
        }
        return Integer.parseInt(src);
    };

    Converter<String, Boolean> TO_BOOLEAN = (src) -> !(src == null || src.isEmpty()) && Boolean.parseBoolean(src);
    Converter<String, Mode> TO_MODE = Mode::find;

    Converter<String, File> TO_FILE = File::new;
    Converter<String, String> IN_OUTPUT_FOLDER = (src) -> String.format("%s/%s", System.getProperty("user.dir"), src);
    Converter<Date, String> DATE_TO_STRING = IConstants.SIMPLE_DATE_FORMATTER::format;

    Converter<String, Date> TO_DATE = (src) -> {
        try {
            return IConstants.SIMPLE_DATE_FORMATTER.parse(src);
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    };

    Converter<String, Tweet> TO_TWEET = (src) -> new Tweet().fromRecord(src);

    static Converter forString(final String name) {
        Field field = Stream.of(Converter.class.getDeclaredFields()).map(field1 -> {
            field1.setAccessible(true);
            return field1;
        }).filter(field1 -> field1.getName().equalsIgnoreCase(name)).findAny().orElse(null);

        if (field != null) {
            try {
                return (Converter) field.get(null);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    U convert(T t);

}
