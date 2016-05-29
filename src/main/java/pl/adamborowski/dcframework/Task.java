package pl.adamborowski.dcframework;

import lombok.Getter;
import lombok.Setter;

public class Task<Params, Result> {
    public enum State {
        AWAITING, COMPUTED, DEAD
    }

    @Getter
    Params params = null;
    @Getter
    private Result result = null;
    @Getter
    private final int nativeNode;

    public Task(int nativeNode) {
        this.nativeNode = nativeNode;
    }

    @Setter
    private State state = null;
    @Getter
    Task<Params, Result> parent = null; // if null and state==done - finish program
    @Getter
    Task<Params, Result> brother = null;
    @Getter
    boolean remote = false;

    @Getter
    boolean rootTask = false;

    public void setComputed(Result result) {
        this.result = result;
        state = State.COMPUTED;
    }

    public void setup(Task<Params, Result> parent, Task<Params, Result> brother, Params params) {
        state = State.AWAITING;
        this.parent = parent;
        this.brother = brother;
        this.params = params;
    }

    public boolean inState(State state) {
        return this.state.equals(state);
    }

    public boolean readyToMerge() {
        return parent != null && brother != null
                && !parent.isRemote() && !brother.isRemote()
                && inState(State.COMPUTED) && brother.inState(State.COMPUTED);
    }

    public void markAsDead() {
        state = State.DEAD;
    }


}
