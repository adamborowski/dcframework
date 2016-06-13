package pl.adamborowski.dcframework.remote;

public class TaskQueueNameResolver {
    public static String getQueueNameFor(int nodeId) {
        return "queue-" + nodeId;
    }

    public static String getGlobalQueueName() {
        return "global";
    }

    public static String getConfigTopicName() {
        return "master-config";
    }

    public static String getConfigResponseQueueName() {
        return "queue-config-response";
    }

    public static String getFinishTopicName() {
        return "finish";
    }
}
