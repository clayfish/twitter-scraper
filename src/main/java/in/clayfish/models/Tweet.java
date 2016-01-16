package in.clayfish.models;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * The tweet
 *
 * @author shuklaalok7
 * @since 16/01/16
 */
@Getter
@Setter
public class Tweet extends PersistentObject<Tweet> {
    private long id;
    private long conversationId;
    private Date timestamp;
    private String user;
    private String message;

    @Override
    public Tweet fromRecord(String record) {
        return null;
    }
}
