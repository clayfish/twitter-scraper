package in.clayfish.utils;

import in.clayfish.annotations.Converters;
import in.clayfish.annotations.Property;
import lombok.Getter;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Properties;

/**
 * @author shuklaalok7
 * @since 16/01/16
 */
public class ApplicationProperties {

    private static String propertiesFile = "config/application.properties";

    @Getter
    @Property("target.username")
    private String targetUsername;

    @Getter
    @Property("output-folder")
    @Converters({"IN_OUTPUT_FOLDER"})
    private String outputFolder;

    @Getter
    @Property("state-file")
    @Converters({"IN_OUTPUT_FOLDER"})
    private String stateFile;

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

    /**
     * Internal structure
     */
    private Properties props;

    /**
     * This is default converter to be used on all the properties
     */
    Converter<String, String> basicConverter = new Converter<String, String>() {
        @Override
        public String convert(String src) {
            if (!src.contains("${")) {
                return src;
            }

            int low = src.indexOf("${");
            int high = src.substring(low).indexOf("}") + low;

            String property = ApplicationProperties.this.props.getProperty(src.substring(low + 2, high));
            String convertedString = src.substring(0, low) + property + src.substring(high + 1, src.length());
            return this.convert(convertedString);
        }
    };

    /**
     * Loads the config from properties file
     * @throws IOException If something goes south
     */
    public ApplicationProperties() throws IOException {
        props = new Properties();
        props.load(this.getClass().getClassLoader().getResourceAsStream(propertiesFile));

        for (Field field : this.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(Property.class)) {
                Object value = basicConverter.convert(props.getProperty(field.getAnnotation(Property.class).value()));

                if(field.isAnnotationPresent(Converters.class)) {
                    for(String converterString : field.getAnnotation(Converters.class).value()) {
                        Converter converter = Converter.forString(converterString);

                        if(converter != null) {
                            value = converter.convert(value);
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
