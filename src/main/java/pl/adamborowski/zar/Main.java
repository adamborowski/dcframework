package pl.adamborowski.zar;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionHandlerFilter;
import pl.adamborowski.dcframework.BaseSolver;
import pl.adamborowski.dcframework.Problem;
import pl.adamborowski.dcframework.config.MasterConfigPhase;
import pl.adamborowski.dcframework.config.NodeConfig;
import pl.adamborowski.dcframework.config.SlaveConfigPhase;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;
import java.util.Collection;


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


        ActiveMQUtil.resetConnection(options);

        try {
            startNode(options);
        } catch (JMSException e) {
            Logger.getLogger(Main.class).error("Cannot instatiate solver", e);
        }
    }

    public static void startNode(ProgramArgs options) throws JMSException {
        final Integer nodeId = options.getNodeId();
        final boolean isMaster = nodeId == 0;

        Connection connection = createConnection(options);
        Collection<Integer> slaveIds = null;
        NodeConfig nodeConfig = null;

        if (isMaster) {
            nodeConfig = options.getNodeConfig();
            MasterConfigPhase masterConfigPhase = new MasterConfigPhase(connection.createSession(false, Session.AUTO_ACKNOWLEDGE));
            slaveIds = masterConfigPhase.perform(nodeConfig);
        } else {
            SlaveConfigPhase slaveConfigPhase = new SlaveConfigPhase(connection.createSession(true, Session.AUTO_ACKNOWLEDGE));
            nodeConfig = slaveConfigPhase.perform(nodeId);
        }

        //todo below we use options... to configure our solver, let's use a nodeConfig, which for slaves is given from master


        BaseSolver<DummyProblem.Params, Double> solver = new BaseSolver<>();//todo solver can be not generic - just BaseSolver using task - it doesn't require Params and Result templates
        Problem problem = new DummyProblem();
        solver.setup(problem, options.getNumThreads(), options.getNodeId(), options.getBatchSize());
        solver.setConnectionUrl(options.getConnectionUrl());
        Double result = solver.process(DummyProblem.Params.of(options.getStartParameter(), options.getEndParameter()));
        System.out.println(String.format("Result: %.6f", result));
        //todo INFO severity message: computation time, other statistics
    }

    private static Connection createConnection(ProgramArgs options) throws JMSException {
        final ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(options.getConnectionUrl());
        Connection connection = factory.createConnection();
        connection.start();
        return connection;
    }

}
