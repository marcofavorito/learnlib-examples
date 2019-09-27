# learnlib-examples
Learning regular languages using LearnLib

## Generate traces

`generate_traces.py` is a Python script that given an LTLf formula generates
a list of traces labelled with the satisfaction wrt the provided formula.

E.g. to generate traces for 'eventually A':

   python generate_traces.py "F A" eventually --fluents A B --max-length 4 --dirpath data

The output will be in the directory `data/`, and you'll get:

- `data/eventually.txt` that contains all the generated traces. The format of the file is one line per trace.
  A generic lines contains, in this order: 
  - the first character `Y` if the trace is accepted by the LTL formula or `N` otherwise, 
  - a `\t`
  - the sequence of symbols separated by `;`, where each symbol contains fluents separated by `,`. 
  Example:
```
Y\tFluent1,Fluent2,Fluent3;Fluent2;Fluent3
N\tFluent1;Fluent2
```

## CLI tool to learn from traces

- Install [Maven](https://maven.apache.org/)
- Compile

      mvn compile

- Run:

      mvn exec:java  -Dexec.mainClass=com.sapienza.App -Dexec data/eventually.txt

  alternatively:

      ./learnrb ./data/eventually.txt


