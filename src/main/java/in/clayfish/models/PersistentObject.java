package in.clayfish.models;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author shuklaalok7
 * @since 16/01/16
 */
@Getter
@Setter
public abstract class PersistentObject<T extends PersistentObject> implements Serializable {
    /**
     *
     * @param record
     * @return
     */
    public abstract T fromRecord(String record);

}
