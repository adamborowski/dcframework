package pl.adamborowski.dcframework.node;

import pl.adamborowski.dcframework.remote.GlobalId;

public interface TaskFactory<Params, Result> {
    Task<Params, Result> createRootTask(Params params);

    Task<Params, Result> createChildTask(Params params, boolean isLeft);

    Task<Params, Result> createDelegatingTask(Params params, GlobalId globalId);
}
