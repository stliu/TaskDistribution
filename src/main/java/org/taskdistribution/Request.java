package org.taskdistribution;

import lombok.Data;
import org.jgroups.util.Streamable;
import org.jgroups.util.Util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * @author stliu at apache.org
 * @since 6/17/16
 */
@Data
public class Request implements Streamable {


    private RequestType requestType;
    private Task task;
    private ClusterID id;
    private Object result;


    public Request() {
    }

    public Request(RequestType requestType, Task task, ClusterID id, Object result) {
        this.requestType = requestType;
        this.task = task;
        this.id = id;
        this.result = result;
    }


    @Override
    public void writeTo(DataOutput out) throws Exception {
        out.writeInt(requestType.ordinal());
        try {
            Util.objectToStream(task, out);
        } catch (Exception e) {
            IOException ex = new IOException("failed marshalling of task " + task);
            ex.initCause(e);
            throw ex;
        }
        Util.writeStreamable(id, out);
        try {
            Util.objectToStream(result, out);
        } catch (Exception e) {
            IOException ex = new IOException("failed to marshall result object");
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    public void readFrom(DataInput in) throws Exception {
        int tmp = in.readInt();
        this.requestType = RequestType.get(tmp);
        try {
            task = (Task) Util.objectFromStream(in);
        } catch (Exception e) {
            InstantiationException ex = new InstantiationException("failed reading task from stream");
            ex.initCause(e);
            throw ex;
        }
        id = (ClusterID) Util.readStreamable(ClusterID.class, in);
        try {
            result = Util.objectFromStream(in);
        } catch (Exception e) {
            IOException ex = new IOException("failed to unmarshal result object");
            ex.initCause(e);
            throw ex;
        }
    }

}
