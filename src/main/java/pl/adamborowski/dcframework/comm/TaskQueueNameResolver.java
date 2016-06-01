package pl.adamborowski.dcframework.comm;

public class TaskQueueNameResolver {
    public static String getQueueNameFor(int nodeId) {
        return "queue-" + nodeId;
    }

    public static String getGlobalQueueNae() {
        return "global";
    }
}
