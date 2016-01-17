package in.clayfish.extractors;

import in.clayfish.utils.ApplicationProperties;

/**
 * @author shuklaalok7
 * @since 18/01/16
 */
public abstract class Extractor implements Runnable {

    protected final ApplicationProperties props;

    public Extractor(ApplicationProperties props) {
        this.props = props;
    }
}
