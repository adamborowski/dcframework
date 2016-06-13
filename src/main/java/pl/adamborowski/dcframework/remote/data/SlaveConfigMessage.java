package pl.adamborowski.dcframework.remote.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.adamborowski.dcframework.config.NodeConfig;

@RequiredArgsConstructor
@Getter
public class SlaveConfigMessage implements TransferObject {
    private final NodeConfig nodeConfig;

    @RequiredArgsConstructor
    @Getter
    public static class Response implements TransferObject {
        private final Integer nodeId;
        private final NodeConfig nodeConfig;
        private final int nodeConfigHash;
    }
}
