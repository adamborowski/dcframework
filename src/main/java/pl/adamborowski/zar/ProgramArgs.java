package pl.adamborowski.zar;

import lombok.Getter;
import org.apache.log4j.Level;
import org.kohsuke.args4j.Option;

@Getter
public class ProgramArgs {
    @Option(name = "-s", usage = "start parameter", required = true, aliases = "--start", metaVar = "<number>")
    private double startParameter = 0;
    @Option(name = "-e", usage = "end parameter", required = true, aliases = "--end", metaVar = "<number>")
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


}