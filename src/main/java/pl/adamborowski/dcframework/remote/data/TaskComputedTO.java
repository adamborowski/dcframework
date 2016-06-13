package pl.adamborowski.dcframework.remote.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.adamborowski.dcframework.remote.GlobalId;

/**
 * A Task transfer object for task which was computed on foreign node and has to be transferred to native node to continue merging
 */
@Getter
@AllArgsConstructor
public class TaskComputedTO<Result> implements TransferObject {
    private final GlobalId globalId;
    private final int computingNodeId;
    private final Result result;

    @Override
    public String toString() {
        return String.format("[node %s computed task %s for node %s = %s]", computingNodeId, globalId.getLocalId(), globalId.getNodeId(), result.toString());
    }
}
