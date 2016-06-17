package org.taskdistribution;


import lombok.extern.slf4j.Slf4j;
import org.jgroups.Message;
import org.jgroups.util.Util;

import java.util.function.Function;

/**
 * @author stliu at apache.org
 * @since 6/17/16
 */
@Slf4j
public class ChannelRequestMessageMapper implements Function<Request, Message> {

    @Override
    public Message apply(Request request) {
        try {
            byte[] buf = Util.streamableToByteBuffer(request);
            return new Message(null, buf);
        } catch (Exception e) {
            log.error("Failed to convert request to message", e);
        }
        return null;
    }
}
