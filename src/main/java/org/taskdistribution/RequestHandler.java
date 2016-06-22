package org.taskdistribution;

import org.jgroups.Address;

/**
 * @author stliu at apache.org
 * @since 6/22/16
 */
public interface RequestHandler {
    RequestType getSupportedRequestType();
    void handle(Address senderAddress, Request req);
}
