package in.clayfish.pyry.models;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.Objects;

import static in.clayfish.pyry.utils.Converter.*;
import static in.clayfish.pyry.utils.IConstants.BLANK;
import static in.clayfish.pyry.utils.IConstants.COMMA;

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
    private String location;
    private String user;
    private String username;
    private String message;

    @Override
    public Tweet fromRecord(String record) {
        Objects.requireNonNull(record);
        if (StringUtils.isBlank(record)) {
            throw new IllegalArgumentException("record cannot be blank");
        }

        String[] tweetMetadata = record.split(COMMA);

        if (tweetMetadata.length < 5 || tweetMetadata.length > 7) {
            throw new IllegalArgumentException("record is malformed and cannot be converted to tweet object");
        }

        this.id = TO_LONG.apply(tweetMetadata[0]);
        this.conversationId = TO_LONG.apply(tweetMetadata[1]);
        this.timestamp = LONG_TO_DATE.apply(TO_LONG.apply(tweetMetadata[2]));
        this.username = tweetMetadata[3];
        this.user = tweetMetadata[4];

        if (tweetMetadata.length > 5) {
            this.location = tweetMetadata[5];
        }

        if (tweetMetadata.length > 6) {
            this.message = tweetMetadata[6];
        }

        return this;
    }

    @Override
    public String toString() {
        try {
            this.sanitize();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException("Cannot serialize tweet: " + id);
        }
        return String.format("%d,%d,%d,%s,%s,%s,%s", id, conversationId, DATE_TO_LONG.apply(timestamp), username, user, location,
                message.replaceAll(COMMA, BLANK));
    }

}
