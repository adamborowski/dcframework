package pl.adamborowski.dcframework.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class Statistics {
    private final int computationTime;
    private final int numTaskProcessed;
    private final int numTaskSentToGlobal; // when sharing queue sends task to global
    private final int numTaskReceivedFromGlobal; // when localQueueSupplier gets some others tasks to compute
    private final int numTaskSentBackToCreator; // when sharing queue sends computed task to owner's queue
    private final int numTaskReceivedBackToCreator; // when owning queue receiver gets computed task to merge
}
