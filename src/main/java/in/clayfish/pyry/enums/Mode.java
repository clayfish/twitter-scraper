package in.clayfish.pyry.enums;

import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

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
