package org.taskdistribution;

import lombok.extern.slf4j.Slf4j;
import org.jgroups.Address;
import org.jgroups.Message;
import org.jgroups.util.Util;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * @author stliu at apache.org
 * @since 6/17/16
 */
@Slf4j
class Handler implements Callable<Message>, Supplier<Message> {
    final ClusterID id;
    final Address sender;
    final Task task;

    public Handler(ClusterID id, Address sender, Task task) {
        this.id = id;
        this.sender = sender;
        this.task = task;
    }

    @Override
    public Message call() throws Exception {
        Object result = null;
        if (task != null) {
            try {
                log.info("executing " + id);
                result = task.execute();
            } catch (Throwable t) {
                log.error("failed executing " + id, t);
                result = t;
            }
        }
        Request response = new Request(RequestType.RESULT, null, id, result);
        byte[] buf = Util.streamableToByteBuffer(response);
        return new Message(sender, buf);
    }

    @Override
    public Message get() {
        try {
            return call();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
