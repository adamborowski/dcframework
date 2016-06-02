package pl.adamborowski.zar;

import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionHandlerFilter;
import pl.adamborowski.dcframework.BaseSolver;
import pl.adamborowski.dcframework.Problem;


public class Main {
    public static void main(String[] args) throws CmdLineException {
        ProgramArgs options = new ProgramArgs();
        CmdLineParser parser = new CmdLineParser(options);
        try {
            // parse the arguments.
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            // print the list of available options
            parser.printUsage(System.err);
            System.err.println();
            // print option sample. This is useful some time
            System.err.println("  Example: java SampleMain" + parser.printExample(OptionHandlerFilter.ALL));

            return;
        }
        Logger.getLogger("pl.adamborowski.dcframework").setLevel(options.getFrameworkLogLevel());

        BaseSolver<Problem1.Params, Double> solver = new BaseSolver<>();//todo solver can be not generic - just BaseSolver using task - it doesn't require Params and Result templates
        Problem problem = new Problem1();
        solver.setup(problem, options.getNumThreads(), options.getNodeId(), options.getBatchSize());
        solver.setConnectionUrl(options.getConnectionUrl());
        Double result = solver.process(Problem1.Params.of(options.getStartParameter(), options.getEndParameter()));
        System.out.println(String.format("Result: %.6f", result));
        //todo INFO severity message: computation time, other statistics
    }
}
