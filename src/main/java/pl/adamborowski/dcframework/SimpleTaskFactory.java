package pl.adamborowski.dcframework;

public class SimpleTaskFactory<Params, Result> implements TaskFactory<Params, Result> {

    public SimpleTaskFactory(int nodeId) {
        this.nodeId = nodeId;
    }

    private final int nodeId;

    public synchronized Task<Params, Result> createTask() {
        return new Task<>(nodeId);
    }
}
