package pl.adamborowski.dcframework.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

@AllArgsConstructor
@Getter
@ToString
public class NodeConfig<Params> implements Serializable {
    private int numThreads;
    private int batchSize;
    private Params initialParams;
    //TODO optimization options here...
}
