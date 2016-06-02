package pl.adamborowski.dcframework.api;

import com.google.common.base.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@Getter
@AllArgsConstructor
public class GlobalId implements Serializable {
    private final int nodeId;
    private final int localId;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GlobalId globalId = (GlobalId) o;
        return nodeId == globalId.nodeId &&
                localId == globalId.localId;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(nodeId, localId);
    }

    @Override
    public String toString() {
        return "#" + localId + "@" + nodeId;
    }
}
