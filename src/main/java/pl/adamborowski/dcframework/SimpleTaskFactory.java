package pl.adamborowski.dcframework;

import pl.adamborowski.dcframework.api.GlobalId;

import java.util.concurrent.atomic.AtomicInteger;

public class SimpleTaskFactory<Params, Result> implements TaskFactory<Params, Result> {

    public SimpleTaskFactory(int nodeId) {
        this.nodeId = nodeId;
    }

    private final int nodeId;
    private AtomicInteger lastId = new AtomicInteger(0);

    @Override
    public Task<Params, Result> createRootTask(Params params) {
        return new Task<>(createGlobalId(), params, true, true, false);
    }

    private GlobalId createGlobalId() {
        return new GlobalId(nodeId, lastId.getAndAdd(1));
    }

    @Override
    public Task<Params, Result> createChildTask(Params params, boolean isLeft) {
        return new Task<>(createGlobalId(), params, isLeft, false, false);
    }

    @Override
    public Task<Params, Result> createDelegatingTask(Params params, GlobalId globalId) {
        return new Task<>(globalId, params, false, false, true);
    }

}
