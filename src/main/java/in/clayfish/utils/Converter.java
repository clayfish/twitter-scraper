package in.clayfish.utils;

import java.lang.reflect.Field;
import java.util.stream.Stream;

/**
 * @author shuklaalok7
 * @since 16/01/16
 */
@FunctionalInterface
public interface Converter<T, U> {
    Converter<String, Long> TO_LONG = Long::parseLong;
    Converter<String, Integer> TO_INT = Integer::parseInt;
    Converter<String, String> IN_OUTPUT_FOLDER = (src) -> String.format("%s/%s", System.getProperty("user.dir"), src);

    static Converter forString(final String name) {
        Field field = Stream.of(Converter.class.getDeclaredFields()).map(field1 -> {
            field1.setAccessible(true);
            return field1;
        }).filter(field1 -> field1.getName().equalsIgnoreCase(name)).findAny().orElse(null);

        if(field != null) {
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
