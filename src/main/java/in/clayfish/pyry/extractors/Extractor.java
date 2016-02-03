package in.clayfish.pyry.extractors;

import in.clayfish.pyry.utils.ApplicationProperties;

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
