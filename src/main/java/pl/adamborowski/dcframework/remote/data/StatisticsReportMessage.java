package pl.adamborowski.dcframework.remote.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.adamborowski.dcframework.config.Statistics;

@RequiredArgsConstructor
@Getter
public class StatisticsReportMessage implements TransferObject {
    private final int nodeId;
    private final Statistics statistics;
}
