package pl.adamborowski.dcframework.comm;

public class TaskQueueNameResolver {
    public static String getQueueNameFor(int nodeId) {
        return "queue-" + nodeId;
    }

    public static String getGlobalQueueName() {
        return "global";
    }
}
