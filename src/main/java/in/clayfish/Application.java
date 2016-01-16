package in.clayfish;

import in.clayfish.utils.ApplicationProperties;

import java.io.IOException;

/**
 * @author shuklaalok7
 * @since 16/01/16
 */
public class Application {

    public static void main(String[] args) throws IOException {
        ApplicationProperties props = new ApplicationProperties();
        System.out.println(props.getTargetUsername());
    }

}
