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

import java.io.File;
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
        GraphDOT.write(result, inputs, System.out); // may throw IOException!

        Visualization.visualize(result, inputs);

        return 0;
    }

    private static List<String> getActions(){
        List<String> actions = new LinkedList();
        actions.add("A");
        actions.add("B");
        return actions;
    }
}


/*

/snap/intellij-idea-ultimate/173/jbr/bin/java
-javaagent:/snap/intellij-idea-ultimate/173/lib/idea_rt.jar=43169:/snap/intellij-idea-ultimate/173/bin
-Dfile.encoding=UTF-8
-classpath /home/marcofavorito/workfolder/learnlib-examples/target/classes:
           /home/marcofavorito/.m2/repository/junit/junit/4.12/junit-4.12.jar:
           /home/marcofavorito/.m2/repository/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar:
           /home/marcofavorito/.m2/repository/de/learnlib/learnlib-adt/0.14.0/learnlib-adt-0.14.0.jar:
           /home/marcofavorito/.m2/repository/net/automatalib/automata-api/0.8.0/automata-api-0.8.0.jar:
           /home/marcofavorito/.m2/repository/net/automatalib/automata-core/0.8.0/automata-core-0.8.0.jar:
           /home/marcofavorito/.m2/repository/net/automatalib/automata-util/0.8.0/automata-util-0.8.0.jar:
           /home/marcofavorito/.m2/repository/net/automatalib/automata-commons-util/0.8.0/automata-commons-util-0.8.0.jar:
           /home/marcofavorito/.m2/repository/com/google/code/findbugs/jsr305/3.0.2/jsr305-3.0.2.jar:
           /home/marcofavorito/.m2/repository/de/learnlib/learnlib-dhc/0.14.0/learnlib-dhc-0.14.0.jar:
           /home/marcofavorito/.m2/repository/com/google/guava/guava/27.0-jre/guava-27.0-jre.jar:
           /home/marcofavorito/.m2/repository/com/google/guava/failureaccess/1.0/failureaccess-1.0.jar:
           /home/marcofavorito/.m2/repository/com/google/guava/listenablefuture/9999.0-empty-to-avoid-conflict-with-guava/listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar:
           /home/marcofavorito/.m2/repository/org/checkerframework/checker-qual/2.5.2/checker-qual-2.5.2.jar:
           /home/marcofavorito/.m2/repository/com/google/errorprone/error_prone_annotations/2.2.0/error_prone_annotations-2.2.0.jar:
           /home/marcofavorito/.m2/repository/com/google/j2objc/j2objc-annotations/1.1/j2objc-annotations-1.1.jar:
           /home/marcofavorito/.m2/repository/org/codehaus/mojo/animal-sniffer-annotations/1.17/animal-sniffer-annotations-1.17.jar:
           /home/marcofavorito/.m2/repository/org/slf4j/slf4j-api/1.7.25/slf4j-api-1.7.25.jar:
           /home/marcofavorito/.m2/repository/de/learnlib/learnlib-discrimination-tree/0.14.0/learnlib-discrimination-tree-0.14.0.jar:
           /home/marcofavorito/.m2/repository/net/automatalib/automata-commons-smartcollections/0.8.0/automata-commons-smartcollections-0.8.0.jar:
           /home/marcofavorito/.m2/repository/de/learnlib/learnlib-discrimination-tree-vpda/0.14.0/learnlib-discrimination-tree-vpda-0.14.0.jar:
           /home/marcofavorito/.m2/repository/de/learnlib/learnlib-kearns-vazirani/0.14.0/learnlib-kearns-vazirani-0.14.0.jar:
           /home/marcofavorito/.m2/repository/de/learnlib/learnlib-lstar/0.14.0/learnlib-lstar-0.14.0.jar:
            /home/marcofavorito/.m2/repository/de/learnlib/learnlib-nlstar/0.14.0/learnlib-nlstar-0.14.0.jar:
            /home/marcofavorito/.m2/repository/de/learnlib/learnlib-ttt/0.14.0/learnlib-ttt-0.14.0.jar:
            /home/marcofavorito/.m2/repository/de/learnlib/learnlib-ttt-vpda/0.14.0/learnlib-ttt-vpda-0.14.0.jar:
            /home/marcofavorito/.m2/repository/de/learnlib/learnlib-rpni/0.14.0/learnlib-rpni-0.14.0.jar:
            /home/marcofavorito/.m2/repository/de/learnlib/learnlib-rpni-edsm/0.14.0/learnlib-rpni-edsm-0.14.0.jar:
            /home/marcofavorito/.m2/repository/de/learnlib/learnlib-rpni-mdl/0.14.0/learnlib-rpni-mdl-0.14.0.jar:
            /home/marcofavorito/.m2/repository/de/learnlib/learnlib-api/0.14.0/learnlib-api-0.14.0.jar:
            /home/marcofavorito/.m2/repository/de/learnlib/learnlib-acex/0.14.0/learnlib-acex-0.14.0.jar:
            /home/marcofavorito/.m2/repository/de/learnlib/learnlib-counterexamples/0.14.0/learnlib-counterexamples-0.14.0.jar:
            /home/marcofavorito/.m2/repository/de/learnlib/learnlib-settings/0.14.0/learnlib-settings-0.14.0.jar:
            /home/marcofavorito/.m2/repository/de/learnlib/learnlib-util/0.14.0/learnlib-util-0.14.0.jar:
            /home/marcofavorito/.m2/repository/de/learnlib/learnlib-datastructure-dt/0.14.0/learnlib-datastructure-dt-0.14.0.jar:
            /home/marcofavorito/.m2/repository/de/learnlib/learnlib-datastructure-list/0.14.0/learnlib-datastructure-list-0.14.0.jar:
            /home/marcofavorito/.m2/repository/de/learnlib/learnlib-datastructure-ot/0.14.0/learnlib-datastructure-ot-0.14.0.jar:
            /home/marcofavorito/.m2/repository/de/learnlib/learnlib-datastructure-pta/0.14.0/learnlib-datastructure-pta-0.14.0.jar:
            /home/marcofavorito/.m2/repository/de/learnlib/learnlib-drivers-basic/0.14.0/learnlib-drivers-basic-0.14.0.jar:
            /home/marcofavorito/.m2/repository/de/learnlib/learnlib-mapper/0.14.0/learnlib-mapper-0.14.0.jar:
            /home/marcofavorito/.m2/repository/de/learnlib/learnlib-drivers-simulator/0.14.0/learnlib-drivers-simulator-0.14.0.jar:
            /home/marcofavorito/.m2/repository/de/learnlib/learnlib-emptiness-oracles/0.14.0/learnlib-emptiness-oracles-0.14.0.jar:
            /home/marcofavorito/.m2/repository/de/learnlib/learnlib-equivalence-oracles/0.14.0/learnlib-equivalence-oracles-0.14.0.jar:
            /home/marcofavorito/.m2/repository/de/learnlib/learnlib-cache/0.14.0/learnlib-cache-0.14.0.jar:
            /home/marcofavorito/.m2/repository/net/automatalib/automata-incremental/0.8.0/automata-incremental-0.8.0.jar:
            /home/marcofavorito/.m2/repository/de/learnlib/learnlib-reuse/0.14.0/learnlib-reuse-0.14.0.jar:
            /home/marcofavorito/.m2/repository/de/learnlib/learnlib-statistics/0.14.0/learnlib-statistics-0.14.0.jar:
            /home/marcofavorito/.m2/repository/de/learnlib/learnlib-membership-oracles/0.14.0/learnlib-membership-oracles-0.14.0.jar:
            /home/marcofavorito/.m2/repository/de/learnlib/learnlib-parallelism/0.14.0/learnlib-parallelism-0.14.0.jar:
            /home/marcofavorito/.m2/repository/de/learnlib/learnlib-property-oracles/0.14.0/learnlib-property-oracles-0.14.0.jar:
            /home/marcofavorito/.m2/repository/net/automatalib/automata-brics/0.8.0/automata-brics-0.8.0.jar:
            /home/marcofavorito/.m2/repository/net/automatalib/automata-modelchecking-ltsmin/0.8.0/automata-modelchecking-ltsmin-0.8.0.jar:
            /home/marcofavorito/.m2/repository/net/automatalib/automata-serialization-aut/0.8.0/automata-serialization-aut-0.8.0.jar:
            /home/marcofavorito/.m2/repository/net/automatalib/automata-serialization-core/0.8.0/automata-serialization-core-0.8.0.jar:
            /home/marcofavorito/.m2/repository/net/automatalib/automata-serialization-dot/0.8.0/automata-serialization-dot-0.8.0.jar:
            /home/marcofavorito/.m2/repository/net/automatalib/automata-serialization-etf/0.8.0/automata-serialization-etf-0.8.0.jar:
            /home/marcofavorito/.m2/repository/net/automatalib/automata-serialization-fsm/0.8.0/automata-serialization-fsm-0.8.0.jar:
            /home/marcofavorito/.m2/repository/net/automatalib/automata-serialization-learnlibv2/0.8.0/automata-serialization-learnlibv2-0.8.0.jar:
            /home/marcofavorito/.m2/repository/net/automatalib/automata-serialization-saf/0.8.0/automata-serialization-saf-0.8.0.jar:
            /home/marcofavorito/.m2/repository/net/automatalib/automata-serialization-taf/0.8.0/automata-serialization-taf-0.8.0.jar:
            /home/marcofavorito/.m2/repository/net/automatalib/automata-dot-visualizer/0.8.0/automata-dot-visualizer-0.8.0.jar:
            /home/marcofavorito/.m2/repository/net/automatalib/automata-jung-visualizer/0.8.0/automata-jung-visualizer-0.8.0.jar:
            /home/marcofavorito/.m2/repository/net/sf/jung/jung-api/2.1.1/jung-api-2.1.1.jar:
            /home/marcofavorito/.m2/repository/net/sf/jung/jung-algorithms/2.1.1/jung-algorithms-2.1.1.jar:
            /home/marcofavorito/.m2/repository/net/sf/jung/jung-graph-impl/2.1.1/jung-graph-impl-2.1.1.jar:
            /home/marcofavorito/.m2/repository/net/sf/jung/jung-visualization/2.1.1/jung-visualization-2.1.1.jar:
            /home/marcofavorito/.m2/repository/com/github/misberner/graphviz-awt-shapes/graphviz-awt-shapes/0.0.1/graphviz-awt-shapes-0.0.1.jar:
            /home/marcofavorito/.m2/repository/info/picocli/picocli/4.0.4/picocli-4.0.4.jar
            com.sapienza.App

 */