package in.clayfish.models;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author shuklaalok7
 * @since 16/01/16
 */
@Getter
@Setter
public class Conversation {
    private long id;
    private List<Tweet> tweets;
}
