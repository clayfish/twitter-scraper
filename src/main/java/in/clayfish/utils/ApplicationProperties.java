package in.clayfish.utils;

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

    /**
     * Internal structure
     */
    private Properties props;

    public ApplicationProperties() throws IOException {
        props = new Properties();
        props.load(this.getClass().getClassLoader().getResourceAsStream(propertiesFile));

        for (Field field : this.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(Property.class)) {
                try {
                    field.set(this, props.getProperty(field.getAnnotation(Property.class).value()));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    throw new IOException(e);
                }
            }
        }
    }

    /**
     * [NOT YET IMPLEMENTED]
     * Saves the current property values
     *
     * @return true if saved successfully
     */
    public boolean save() {
//        try {
//            props.store(new FileOutputStream(this.getClass().getClassLoader().getResource(propertiesFile)), "Application properties");
//        } catch (IOException e) {
//            e.printStackTrace();
//            return false;
//        }
//        return true;

        return false;
    }

}
