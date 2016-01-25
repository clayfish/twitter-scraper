package in.clayfish.utils;

import in.clayfish.annotations.Converters;
import in.clayfish.annotations.Property;
import in.clayfish.enums.Mode;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Properties;

/**
 * @author shuklaalok7
 * @since 16/01/16
 */
public class ApplicationProperties {

    @Getter
    @Property("target.username")
    private String targetUsername;

    @Getter
    @Property("target.starting-tweet")
    @Converters({"TO_LONG"})
    private long startingTweetId;

    @Getter
    @Property("target.last-tweet")
//    @Converters({"TO_LONG"})
    private String lastTweetId;

    @Getter
    @Property("output-folder")
    @Converters({"IN_OUTPUT_FOLDER", "TO_FILE"})
    private File outputFolder;

    @Getter
    @Property("state-file")
    @Converters({"IN_OUTPUT_FOLDER", "TO_FILE"})
    private File stateFile;

    @Getter
    @Property("base-url")
    private String baseUrl;

    @Getter
    @Property("user-agent")
    private String userAgent;

    @Getter
    @Property("connection.timeout")
    @Converters({"TO_INT"})
    private int connectionTimeout;

    @Getter
    @Property("concurrent-threads")
    @Converters({"TO_INT"})
    private int numberOfConcurrentThreads;

    @Getter
    @Property("target.continue")
    @Converters({"TO_BOOLEAN"})
    private boolean toContinue;

    @Getter
    @Property("mode")
    @Converters({"TO_MODE"})
    private Mode mode;

    @Getter
    @Property("target.step")
    @Converters({"TO_INT"})
    private int step;

    /**
     * Internal structure
     */
    private Properties props;

    /**
     * This is default converter to be used on all the properties
     */
    Converter<String, String> basicConverter = new Converter<String, String>() {
        @Override
        public String apply(String src) {
            if (!src.contains("${")) {
                return src;
            }

            int low = src.indexOf("${");
            int high = src.substring(low).indexOf("}") + low;

            String property = ApplicationProperties.this.props.getProperty(src.substring(low + 2, high));
            String convertedString = src.substring(0, low) + property + src.substring(high + 1, src.length());
            return this.apply(convertedString);
        }
    };

    /**
     * Loads the config from properties file
     *
     * @throws IOException If something goes south
     */
    @SuppressWarnings(IConstants.UNCHECKED)
    public ApplicationProperties(final String propertiesFile) throws IOException {
        props = new Properties();
        props.load(this.getClass().getClassLoader().getResourceAsStream(propertiesFile));

        for (Field field : this.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(Property.class)) {
                Object value = basicConverter.apply(props.getProperty(field.getAnnotation(Property.class).value()));

                if (field.isAnnotationPresent(Converters.class)) {
                    for (String converterString : field.getAnnotation(Converters.class).value()) {
                        Converter converter = Converter.forString(converterString);
                        if (converter != null) {
                            value = converter.apply(value);
                        }
                    }
                }

                try {
                    field.set(this, value);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    throw new IOException(e);
                }
            }
        }
    }

}
