package pl.adamborowski.zar;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionHandlerFilter;
import pl.adamborowski.dcframework.config.MasterConfigPhase;
import pl.adamborowski.dcframework.config.MasterResultPhase;
import pl.adamborowski.dcframework.config.NodeConfig;
import pl.adamborowski.dcframework.config.SlaveConfigPhase;
import pl.adamborowski.dcframework.config.SlaveResultPhase;
import pl.adamborowski.dcframework.config.Statistics;
import pl.adamborowski.dcframework.node.BaseSolver;
import pl.adamborowski.dcframework.node.Problem;

import javax.jms.Connection;
import javax.jms.JMSException;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;


public class Main {

    public static final Logger log = Logger.getLogger(Main.class);

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
            MasterConfigPhase masterConfigPhase = new MasterConfigPhase(connection);
            slaveIds = masterConfigPhase.perform(nodeConfig);
        } else {
            SlaveConfigPhase slaveConfigPhase = new SlaveConfigPhase(connection);
            nodeConfig = slaveConfigPhase.perform(nodeId);
        }

        BaseSolver solver = startComputing(nodeId, connection, nodeConfig);

        if (isMaster) {
            MasterResultPhase masterResultPhase = new MasterResultPhase(connection);
            Map<Integer, Statistics> statistics = masterResultPhase.perform(slaveIds, solver.getStatistics());
            if (options.getReportFile() != null) {
                ReportCreator reportCreator = new ReportCreator(nodeConfig.getInitialParams(), solver.getResult(), statistics);
                try {
                    reportCreator.save(options.getReportFile());
                } catch (IOException e) {
                    log.error("Cannot save report", e);
                }
            }
            log.info("Statistics: " + statistics);
        } else {
            SlaveResultPhase slaveResultPhase = new SlaveResultPhase(connection);
            slaveResultPhase.perform(nodeId, solver.getStatistics());
            log.info("Slave statistics: " + solver.getStatistics());
        }


        connection.close();
        log.info("ActiveMQ connection closed");


    }

    private static BaseSolver startComputing(Integer nodeId, Connection connection, NodeConfig nodeConfig) {
        log.info("Node " + nodeId + " starting computing");
        log.info("Options: " + nodeConfig.toString());
        BaseSolver<DummyProblem.Params, Double> solver = new BaseSolver<>();//todo solver can be not generic - just BaseSolver using task - it doesn't require Params and Result templates
        Problem problem = new DummyProblem();
        solver.setup(problem, nodeConfig.getNumThreads(), nodeId, nodeConfig.getBatchSize());
        solver.setConnection(connection);
        solver.setOptimizeShortReturn(nodeConfig.isOptimizeShortReturn());


        Double result = solver.process((DummyProblem.Params) nodeConfig.getInitialParams());
        System.out.println(String.format("Result: %.6f", result));
        return solver;
    }

    private static Connection createConnection(ProgramArgs options) throws JMSException {
        final ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(options.getConnectionUrl());
        Connection connection = factory.createConnection();
        connection.start();
        return connection;
    }

}
