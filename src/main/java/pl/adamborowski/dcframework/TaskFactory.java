package pl.adamborowski.dcframework;

public interface TaskFactory<Params, Result> {
    Task<Params, Result> createTask();
}
