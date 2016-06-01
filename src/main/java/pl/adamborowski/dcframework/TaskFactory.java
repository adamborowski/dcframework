package pl.adamborowski.dcframework;

import pl.adamborowski.dcframework.api.GlobalId;

public interface TaskFactory<Params, Result> {
    Task<Params, Result> createRootTask(Params params);

    Task<Params, Result> createChildTask(Params params, boolean isLeft);

    Task<Params, Result> createDelegatingTask(Params params, GlobalId globalId);
}
