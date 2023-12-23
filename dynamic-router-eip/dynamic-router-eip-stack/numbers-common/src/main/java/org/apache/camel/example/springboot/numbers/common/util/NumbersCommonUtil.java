package org.apache.camel.example.springboot.numbers.common.util;

/**
 * Common constants and methods.
 */
public abstract class NumbersCommonUtil {

    private NumbersCommonUtil() {}

    public static final String ENDPOINT_DIRECT_COMMAND = "direct:command";

    public static final String ENDPOINT_DIRECT_LIST = "direct:list";

    public static final String COMMAND_PROCESS_NUMBER = "processNumber";

    public static final String COMMAND_RESET_STATS = "resetStats";

    public static final String COMMAND_STATS = "stats";

    public static final String HEADER_COMMAND = "command";

    public static final String HEADER_EVENT_LIMIT = "limit";

    public static final String HEADER_NUMBER = "number";
}
