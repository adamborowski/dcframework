package pl.adamborowski.zar;

import lombok.Getter;
import org.kohsuke.args4j.Option;

@Getter
public class ProgramArgs {
    @Option(name = "-s", usage = "start parameter", required = true, aliases = "--start", metaVar = "<number>")
    private double startParameter = 0;
    @Option(name = "-e", usage = "end parameter", required = true, aliases = "--end", metaVar = "<number>")
    private double endParameter = 10;
    @Option(name = "-n", usage = "num threads", aliases = "--num-threads", metaVar = "<number>")
    private int numThreads = 10;
    @Option(name = "-b", usage = "batch size", aliases = "--batch-size", metaVar = "<number>")
    private int batchSize = 10;
    @Option(name = "-i", usage = "node id", aliases = "--node-id", metaVar = "<number>")
    private int nodeId = 1;

}
