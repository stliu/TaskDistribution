package org.taskdistribution;

import java.util.EnumSet;

/**
 * @author stliu at apache.org
 * @since 6/17/16
 */
public enum RequestType {
    UNKNOWN(-1), EXECUTE(1), RESULT(2), REMOVE(3);

    private final int index;

    RequestType(int index) {
        this.index = index;
    }

    public static RequestType get(int index) {

        EnumSet<RequestType> enumSet = EnumSet.allOf(RequestType.class);
        return enumSet.stream().filter(v -> v.index == index).findAny().orElse(UNKNOWN);
    }
}
