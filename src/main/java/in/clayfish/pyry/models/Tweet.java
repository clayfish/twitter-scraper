package in.clayfish.pyry.models;

import java.util.Date;
import java.util.Objects;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.lang3.StringUtils;

import in.clayfish.pyry.utils.Converter;
import in.clayfish.pyry.utils.IConstants;

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
        Objects.requireNonNull(record);
        if (StringUtils.isBlank(record)) {
            throw new IllegalArgumentException("record cannot be blank");
        }

        String[] tweetMetadata = record.split(IConstants.COMMA);

        if (tweetMetadata.length < 4) {
            throw new IllegalArgumentException("record is malformed and cannot be converted to tweet object");
        }

        this.id = Converter.TO_LONG.apply(tweetMetadata[0]);
        this.conversationId = Converter.TO_LONG.apply(tweetMetadata[1]);
        this.timestamp = Converter.TO_DATE.apply(tweetMetadata[2]);
        this.user = tweetMetadata[3];

        if (tweetMetadata.length > 4) {
            this.message = tweetMetadata[4];
        }

        return this;
    }

    @Override
    public String toString() {
        return String.format("%d,%d,%s,%s,%s", id, conversationId, Converter.DATE_TO_STRING.apply(timestamp), user,
                message.replaceAll(IConstants.COMMA, IConstants.BLANK));
    }
}
