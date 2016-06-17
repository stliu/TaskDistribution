package org.taskdistribution;

import lombok.extern.slf4j.Slf4j;
import org.jgroups.Channel;
import org.jgroups.Message;

import java.util.function.Consumer;

/**
 * @author stliu at apache.org
 * @since 6/17/16
 */
@Slf4j
public class ChannelMessageSender implements Consumer<Message> {
    private final Channel channel;

    public ChannelMessageSender(Channel channel) {
        this.channel = channel;
    }

    @Override
    public void accept(Message message) {
        if (message != null) {
            try {
                channel.send(message);
            } catch (Exception e) {
                log.error("Failed to send Message", e);
            }
        }
    }
}
