#!/usr/bin/env python
# -*- coding: utf-8 -*-

from flloat.parser.ltlf import LTLfParser
from flloat.semantics.pl import PLInterpretation
import itertools
import sys
import argparse
from pathlib import Path

parser = argparse.ArgumentParser("generate_traces")
parser.add_argument("formula", type=str, help="LTLf formula.")
parser.add_argument("name", type=str, help="Name of the output.")
parser.add_argument("--dirpath", type=str, default="data", help="Directory where to save the output.")
parser.add_argument("--max-length", type=int, default=4, help="Max length of the traces.")
parser.add_argument("--fluents", type=str, nargs="+", required=False, default=None, help="The labels, e.g. --alphabet A B C")


def compute_traces(labels, max_length=4):
    """Compute traces from labels."""
    alphabet = set()
    for k in range(1, len(labels) + 1):
        for comb in itertools.combinations(labels, k):
            alphabet.add(PLInterpretation(comb))
    
    traces = []
    for n in range(max_length):
        traces.extend(itertools.product(alphabet, repeat=n))

    return traces


if __name__ == "__main__":
    arguments = parser.parse_args()
    formula = arguments.formula
    name = arguments.name

    path = Path(arguments.dirpath)
    path.mkdir(exist_ok=True)
    fp = open(str(path.joinpath(name)) + ".txt", mode="w")

    p = LTLfParser()
    f = p(arguments.formula)

    if arguments.fluents is None:
        labels = f.find_labels() 
    else:
        labels = arguments.fluents
    automaton = f.to_automaton(labels=labels)
    automaton.to_dot(str(path.joinpath(name)))

    for word in compute_traces(labels, max_length=arguments.max_length):
        outcome = automaton.accepts(word)
        print(
            ("Y" if outcome else "N") + "\t",
            ";".join([",".join(sorted(symbol)) for symbol in word]),
            file=fp, sep=''
        )
