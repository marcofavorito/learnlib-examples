package com.sapienza.helpers;

import de.learnlib.api.oracle.SingleQueryOracle;
import de.learnlib.api.query.Query;
import net.automatalib.words.Word;

import java.util.Collection;
import java.util.logging.Logger;


public class RBMembershipOracle<I> implements SingleQueryOracle<I, Boolean> {
    // Membership Oracle for Restrainig Bolt

    private static final Logger LOGGER = Logger.getLogger(Traces.class.getName());

    private Collection<Word<I>> positive;
    private Collection<Word<I>> negative;

    public RBMembershipOracle(Collection<Word<I>> positive, Collection<Word<I>> negative) {
        // TODO: Check disjointness of positive and negative
        super();
        this.positive = positive;
        this.negative = negative;
    }

    @Override
    public void processQueries(Collection<? extends Query<I, Boolean>> queries) {
        queries.forEach(this::processQuery);
    }

    @Override
    public Boolean answerQuery(Word<I> prefix, Word<I> suffix) {
        Word<I> input = prefix.concat(suffix);
        System.out.println("input = " + input);
//      return (positive.contains(input) && !negative.contains(input));
        return (positive.contains(input));
    }
}
