package com.sapienza;

import com.sapienza.helpers.RBMembershipOracle;
import com.sapienza.helpers.Traces;
import de.learnlib.algorithms.ttt.dfa.TTTLearnerDFA;
import de.learnlib.algorithms.ttt.dfa.TTTLearnerDFABuilder;
import de.learnlib.filter.statistic.oracle.CounterOracle;
import de.learnlib.oracle.equivalence.SampleSetEQOracle;
import de.learnlib.oracle.equivalence.WMethodEQOracle;
import de.learnlib.util.Experiment;
import de.learnlib.util.statistics.SimpleProfiler;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.visualization.Visualization;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.ListAlphabet;
import picocli.CommandLine;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;


@CommandLine.Command(name = "learnrb", mixinStandardHelpOptions = true, version = "learnrb 0.1",
        description = "Learn Restraining Bolt from demonstrations.")
public class App implements Callable<Integer>
{

    @CommandLine.Parameters(index = "0", description = "The file that contains the traces.")
    private File file;

    private static final int EXPLORATION_DEPTH = 12;

    public static void main(String... args) {
        int exitCode = new CommandLine(new App()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        Traces traces = new Traces(file.getAbsolutePath());

        // Wraps actions in alphabet
        Alphabet<String> inputs = new ListAlphabet<String>(new ArrayList<String>(traces.symbols));

        RBMembershipOracle<String> test = new RBMembershipOracle<String>(traces.positive, traces.negative);

        // oracle for counting queries wraps SUL
        CounterOracle<String,Boolean> mqOracle = new CounterOracle<>(test, "membership queries");

        // construct L* instance
        TTTLearnerDFA<String> lstar =
                new TTTLearnerDFABuilder<String>().withAlphabet(inputs) // input alphabet
                        .withOracle(mqOracle) // membership oracle
                        .create();

        // construct a W-method conformance test
        // exploring the system up to depth 4 from
        // every state of a hypothesis
//        WMethodEQOracle.DFAWMethodEQOracle<String> oracle = new WMethodEQOracle.DFAWMethodEQOracle<>(mqOracle, EXPLORATION_DEPTH);
        SampleSetEQOracle oracle = new SampleSetEQOracle(false);
        oracle.addAll(mqOracle, traces.allTraces);

        // construct a learning experiment from
        // the learning algorithm and the conformance test.
        // The experiment will execute the main loop of
        // active learning
        Experiment.DFAExperiment<String> experiment = new Experiment.DFAExperiment<>(lstar, oracle, inputs);

        // turn on time profiling
        experiment.setProfile(true);

        // enable logging of models
        experiment.setLogModels(true);

        // run experiment
        experiment.run();

        // get learned model
        DFA<?, String> result = experiment.getFinalHypothesis();

        // report results
        System.out.println("-------------------------------------------------------");

        // profiling
        System.out.println(SimpleProfiler.getResults());

        // learning statistics
        System.out.println(experiment.getRounds().getSummary());
        System.out.println(mqOracle.getStatisticalData().getSummary());

        // model statistics
        System.out.println("States: " + result.size());
        System.out.println("Sigma: " + inputs.size());

        // show model
        System.out.println();
        System.out.println("Model: ");
        GraphDOT.write(result, inputs, System.out); // may throw IOException!

        BufferedWriter bw = Files.newBufferedWriter(Paths.get(file.getAbsolutePath() + ".learned"));
        GraphDOT.write(result, inputs, bw); // may throw IOException!


        Visualization.visualize(result, inputs);

        return 0;
    }

}
