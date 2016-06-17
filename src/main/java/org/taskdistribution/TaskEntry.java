package org.taskdistribution;

import lombok.Data;
import org.jgroups.Address;
import org.jgroups.util.Promise;

/**
 * @author stliu at apache.org
 * @since 6/17/16
 */
@Data
public class TaskEntry {
    private final Task task;
    private final Address submitter;
    private final Promise<Object> promise = new Promise<>();

    public TaskEntry(Task task, Address submitter) {
        this.task = task;
        this.submitter = submitter;
    }
}
