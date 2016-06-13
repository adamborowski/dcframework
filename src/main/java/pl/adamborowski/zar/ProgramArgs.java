package pl.adamborowski.zar;

import lombok.Getter;
import org.apache.log4j.Level;
import org.kohsuke.args4j.Option;
import pl.adamborowski.dcframework.config.NodeConfig;

import java.io.File;

@Getter
public class ProgramArgs {
    @Option(name = "-s", usage = "start parameter", aliases = "--start", metaVar = "<number>")
    private double startParameter = 0;
    @Option(name = "-e", usage = "end parameter", aliases = "--end", metaVar = "<number>")
    private double endParameter = 10;
    @Option(name = "-n", usage = "num threads", aliases = "--num-threads", metaVar = "<number>")
    private int numThreads = 1;
    @Option(name = "-b", usage = "batch size", aliases = "--batch-size", metaVar = "<number>")
    private int batchSize = 10;
    @Option(name = "-i", usage = "node id", aliases = "--node-id", metaVar = "<number>")
    private int nodeId = 0;

    @Option(name = "-l", usage = "framework log level", aliases = "--framework-log-level", metaVar = "<TRACE|DEBUG|INFO|WARN|ERROR|FATAL|ALL>")
    public void setFrameworkLogLevel(String level) {
        frameworkLogLevel = Level.toLevel(level);
    }

    private Level frameworkLogLevel = Level.WARN;

    @Option(name = "-u", usage = "connection url", aliases = "--connection-url", metaVar = "<protocol>://<host>:<port>")
    private String connectionUrl = "tcp://localhost:61616";

    @Option(name="-r", usage = "report output file", aliases = "--report-output-file", metaVar = "<path to file>")
    private File reportFile;

    @Option(name = "-o1", usage = "optimize: don't return delegated task to the same unit using queue", aliases = "--optimize-short-return")
    private boolean optimizeShortReturn = false;
    @Option(name = "-o2", usage = "optimize: send some tasks to all slaves at startup", aliases = "--optimize-initial-distribution")
    private boolean optimizeInitialDistribution = false;

    public NodeConfig getNodeConfig() {
        return new NodeConfig(numThreads, batchSize, DummyProblem.Params.of(startParameter, endParameter), optimizeShortReturn, optimizeInitialDistribution);
    }
}
