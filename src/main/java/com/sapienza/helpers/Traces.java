package com.sapienza.helpers;

import net.automatalib.words.Word;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Traces {

    private static final Logger LOGGER = Logger.getLogger(Traces.class.getName());

    public final String file;
    public final String symbolSeparator;
    public final String literalSeparator;
    public final String labelSeparator = "\t";

    public static final String ACCEPTED = "Y";
    public static final String NOT_ACCEPTED = "N";

    public final List<Word<String>> allTraces = new ArrayList<>();
    public final List<Word<String>> positive = new ArrayList<>();
    public final List<Word<String>> negative = new ArrayList<>();
    public Set<String> symbols = new HashSet<>();

    public Traces(String file, String symbolSeparator, String literalSeparator) throws IOException {
        this.file = file;
        this.symbolSeparator = symbolSeparator;
        this.literalSeparator = literalSeparator;
    }

    public Traces(String file) throws IOException {
        this.file = file;
        this.symbolSeparator = ";";
        this.literalSeparator = ",";

        this.parseFile();
        System.out.println(this.symbols.toString());
    }

    private void parseFile() throws IOException {
        LOGGER.log(Level.FINE, "Parsing {0} file", this.file);
        List<String> lines = Files.readAllLines(Paths.get(file));
        for (String l : lines){
            this.parseLine(l);
        }
    }

    private void parseLine(String line) throws IOException {
        LOGGER.log(Level.FINER, "Parsing line: {0}", line);
        String[] result = line.split(labelSeparator, -1);
        String traceStr;
        String outcomeStr;
        boolean outcome;

        if (line.equals("")){
            LOGGER.log(Level.WARNING, "Empty string. Skipping...");
            return;
        }

        outcomeStr = result[0].strip();
        traceStr = result[1].strip();

        if (!outcomeStr.equals(ACCEPTED) && !outcomeStr.equals(NOT_ACCEPTED)){
            throw new IOException("Outcome string not recognized: " + outcomeStr);
        }
        outcome = outcomeStr.equals(ACCEPTED);

        String[] propositionsStrList;
        if (traceStr.equals("")){
            propositionsStrList = new String[]{};
        }
        else{
            propositionsStrList = traceStr.split(this.symbolSeparator, -1);
        }
        for (int i = 0; i < propositionsStrList.length; i++) { propositionsStrList[0] = propositionsStrList[0].strip(); }
        Word<String> trace = Word.fromList(Arrays.asList(propositionsStrList));

        // update data
        this.saveTrace(trace, outcome);
    }

    private void saveTrace(Word<String> trace, boolean outcome){
        LOGGER.log(Level.FINER, "Saving trace with outcome {0}, {1}", new String[]{outcome ? ACCEPTED : NOT_ACCEPTED, trace.toString()});
        this.allTraces.add(trace);

        this.symbols.addAll(trace.asList());
        if (outcome){ this.positive.add(trace); }
        else{ this.negative.add(trace); }

    }

}
