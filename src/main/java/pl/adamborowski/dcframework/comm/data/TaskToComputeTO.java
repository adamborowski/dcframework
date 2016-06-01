package pl.adamborowski.dcframework.comm.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.adamborowski.dcframework.api.GlobalId;

/**
 * A Task transfer object for task which has to be computed on foreign node and has to be transferred there to continue dividing/computing the task
 */
@Getter
@AllArgsConstructor
public class TaskToComputeTO<Params> implements TransferObject {
    private final GlobalId globalId;
    private final Params params;

    @Override
    public String toString() {
        return String.format("[compute task %s for node %s = %s]", globalId.getLocalId(), globalId.getNodeId(), globalId.getNodeId(), params.toString());
    }
}
