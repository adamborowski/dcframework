package pl.adamborowski.dcframework.node;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import pl.adamborowski.dcframework.remote.GlobalId;

@RequiredArgsConstructor
public class Task<Params, Result> {
    public enum State {
        AWAITING, COMPUTED, DEAD
    }

    @Getter
    private final GlobalId globalId;
    @Getter
    private final Params params;
    @Getter
    private final boolean left; // left task reference (in divide&conquer tree) if this is not a delegate
    @Getter
    private final boolean rootTask;
    @Getter
    private final boolean delegate;// A flag indicating if this Task instance is a remote delegate for task created by another unit

    @Getter
    private Task<Params, Result> brother;
    @Getter
    private Task<Params, Result> parent;

    @Getter
    private int computingNodeId;
    @Getter
    private Result result;
    @Setter
    private State state = State.AWAITING;

    @Override
    public String toString() {
        return "Task{" +
                (rootTask ? "root, " : "") +
                (delegate ? "delegate, " : "") +
                globalId +
                " " + state +

                ", params=" + params +
                ", result=" + result +
                '}';
    }

    /**
     * @param result          result data to be stored as a result of computation for task parameters
     * @param computingNodeId id of node whch computed this task
     */
    public void setComputed(Result result, int computingNodeId) {
        this.result = result;
        state = State.COMPUTED;
        this.computingNodeId = computingNodeId;
    }

    public void wire(Task<Params, Result> parent, Task<Params, Result> brother) {
        this.parent = parent;
        this.brother = brother;
    }


    public boolean inState(State state) {
        return this.state.equals(state);
    }

    public boolean readyToMerge() {
        return  brother != null
                && !brother.isDelegate()
                && inState(State.COMPUTED) && brother.inState(State.COMPUTED)
                && result != null && brother.getResult() != null;
    }


    public void markAsDead() {
        state = State.DEAD;
    }


}
