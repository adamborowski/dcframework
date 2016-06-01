package pl.adamborowski.dcframework.comm;

import pl.adamborowski.dcframework.Task;
import pl.adamborowski.dcframework.api.GlobalId;

import java.util.Map;
import java.util.TreeMap;

/**
 * Stores local reference in cache and returns the transfer objecjt.
 * Allow to receive a real reference by specifing globalId (thwn wil be removed)
 */
public class TaskCache {

    private final Map<GlobalId, Task> tasksByID = new TreeMap<>();

    /**
     * put task into cache
     *
     * @param task
     */
    public void park(Task task) {
        tasksByID.put(task.getGlobalId(), task);
    }

    /**
     * Remove task by global id and return
     *
     * @param globalId
     * @return
     */
    public Task retrieve(final GlobalId globalId) {
        return tasksByID.remove(globalId);
    }

}
