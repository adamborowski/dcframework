package pl.adamborowski.dcframework.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@RequiredArgsConstructor
@Getter
@ToString
public class Statistics implements Serializable {
    private final long computationTime;
    private final long numTaskProcessed;
    private final long numTaskSentToGlobal; // when sharing queue sends task to global
    private final long numTaskReceivedFromGlobal; // when localQueueSupplier gets some others tasks to compute
    private final long numTaskSentBackToCreator; // when sharing queue sends computed task to owner's queue
    private final long numTaskReceivedBackToCreator; // when owning queue receiver gets computed task to merge
}
