package in.clayfish.pyry.models;

import in.clayfish.pyry.utils.IConstants;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;

/**
 * @author shuklaalok7
 * @since 16/01/16
 */
@Getter
@Setter
public class Conversation {
    private long id;
    private Set<Tweet> tweets;
    private Set<Long> tweetIds;

    /**
     *
     */
    private Conversation() {
        this.tweets = new HashSet<>();
        this.tweetIds = new HashSet<>();
    }

    /**
     *
     * @param id
     */
    public Conversation(long id) {
        this();
        this.id = id;
    }

    /**
     *
     * @param tweets
     */
    public void setTweets(Set<Tweet> tweets) {
        this.tweets = tweets;
        this.tweetIds = this.tweets.stream().map(Tweet::getId).collect(Collectors.toSet());
    }

    /**
     *
     * @param tweet
     */
    public void add(Tweet tweet) {
        tweet.setConversationId(id);
        if(this.tweetIds.add(tweet.getId())) {
            this.tweets.add(tweet);
        }
    }

    @Override
    public String toString() {
        return tweets.stream().map(Tweet::toString).reduce((s, s2) -> format("%s%n%s",s, s2)).orElse(IConstants.BLANK);
    }
}
