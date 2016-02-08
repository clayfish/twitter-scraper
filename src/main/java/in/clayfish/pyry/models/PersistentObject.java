package in.clayfish.pyry.models;

import java.io.Serializable;
import java.lang.reflect.Field;

import static in.clayfish.pyry.utils.IConstants.*;

/**
 * @author shuklaalok7
 * @since 16/01/16
 */
public abstract class PersistentObject<T extends PersistentObject> implements Serializable {
    /**
     *
     * @param record    The serialized form of tweet
     * @return deserialized PersistentObject
     */
    public abstract T fromRecord(String record);

    /**
     * It sanitizes the fields of the object so that it does not cause any trouble while being saved in a comma-separated file
     */
    protected void sanitize() throws IllegalAccessException {
        for(Field field : this.getClass().getDeclaredFields()) {
            if(field.getType().isAssignableFrom(String.class)) {
                field.setAccessible(true);
                String value = (String) field.get(this);

                if(value != null && !value.isEmpty()) {
                    value = value.replaceAll(COMMA, BLANK).replaceAll(DOUBLE_QUOTES, BLANK);
                    field.set(this, value);
                }
            }
        }
    }

}
