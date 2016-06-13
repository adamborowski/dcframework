package pl.adamborowski.dcframework.config;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

@AllArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
public class NodeConfig<Params> implements Serializable {
    private int numThreads;
    private int batchSize;
    private Params initialParams;
    private boolean optimizeShortReturn;
    private boolean optimizeInitialDistribution;
    private float randomThreshold;
    private int minThreshold;
    private int maxThreshold;
}
