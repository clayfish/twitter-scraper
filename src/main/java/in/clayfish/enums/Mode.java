package in.clayfish.enums;

import org.apache.commons.lang3.StringUtils;

import java.util.stream.Stream;

/**
 * @author shuklaalok7
 * @since 18/01/16
 */
public enum Mode {
    TEST, DEV, PROD;

    public static Mode find(String search) {
        try {
            return Mode.valueOf(search.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Stream.of(Mode.values()).filter(mode -> StringUtils.containsIgnoreCase(search, mode.name())).findFirst().orElse(null);
        }
    }
}
