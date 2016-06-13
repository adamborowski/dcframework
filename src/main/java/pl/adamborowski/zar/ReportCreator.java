package pl.adamborowski.zar;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.ObjectMapper;
import pl.adamborowski.dcframework.config.Statistics;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@RequiredArgsConstructor

public class ReportCreator<Params, Result> {
    private final Params initialParams;
    private final Result result;
    private final Map<Integer, Statistics> nodeStatistics;
    private final ProgramArgs programArgs;

    public void save(File f) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(JsonMethod.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.writeValue(f, new Report(initialParams, result, nodeStatistics, programArgs));
    }

    @RequiredArgsConstructor
    @Getter
    private class Report {
        private final Params initialParams;
        private final Result result;
        private final Map<Integer, Statistics> nodeStatistics;
        private final ProgramArgs args;
    }

}
