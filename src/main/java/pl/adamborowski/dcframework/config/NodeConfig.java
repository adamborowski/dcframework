package pl.adamborowski.dcframework.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class NodeConfig {
    private String connectionUrl;
    private int numThreads;
    //TODO optimization options here...
}
