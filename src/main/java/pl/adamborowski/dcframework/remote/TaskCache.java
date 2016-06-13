package pl.adamborowski.dcframework.remote;

import pl.adamborowski.dcframework.node.Task;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores local reference in cache and returns the transfer objecjt.
 * Allow to receive a real reference by specifing globalId (thwn wil be removed)
 */
public class TaskCache {

    private final Map<GlobalId, Task> tasksByID = new HashMap<>();

    /**
     * put task into cache
     *
     * @param task
     */
    public synchronized void park(Task task) {
        tasksByID.put(task.getGlobalId(), task);
    }

    /**
     * Remove task by global id and return
     *
     * @param globalId
     * @return
     */
    public synchronized Task retrieve(final GlobalId globalId) {
        return tasksByID.remove(globalId);
    }

}
