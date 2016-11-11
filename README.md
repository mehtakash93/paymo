# Steps to run
1. Read files in paymo/paymo_input/ and download the corresponding files from links provided, convert them to .txt and replace the current files.
2. For testing:
cd insight_testsuite
sh run_tests.sh

2. If running on 8 gb ram please replace run.sh with the following:
```shell
cd src
javac Graph.java 
javac AntiFraud8.java
java -Xmx7000m AntiFraud8 ../paymo_input/batch_payment.txt ../paymo_input/stream_payment.txt ../paymo_output/output1.txt ../paymo_output/output2.txt ../paymo_output/output3.txt
```


# Data Structure
The choice of data structure i used is adjacency list as my graph is not too dense
For the adjacency list i used Map with key-->Integer and Value-->Set(Integers).
I chose the key as Set of Integers because i need simple set operations like getting value from set in O(1), set intersection and i do not want duplicate values.

There are two approaches that i have used, given the space/time tradeoff:

## 1. Unidirectional Search With PreComputation
If space is not an issue(need lots of ram) and the speed should be really fast during stream processing:
### Algorithm:
For allowing "trusted" till level x:

Precompute the graph with adjacency list for each vertex has neightbours till level x. This is done by simple level order BFS on the original graph till level x.

Now use this graph at the time of stream processing simply look into adjacency list of source to find the destiation if present, else it is unverified.

This simply becomes a O(1) lookup and would be really fast irrespecitve of the size of initial graph or level of depth of friendship x.

### Problems
Precomputed graph tends to be very large and if there isnt enough memory present in the system it will lead to lots of collisions in the hashset and eventually make it slow.


## 2. Middle Ground. Bidirectional with Precomputation
## Algorithm:

For allowing "trusted" till level x:

Precompute the graph with adjacency list for each vertex has neightbours till level x/2. This is done by simple level order BFS on the original graph till level x/2.

Now use this graph at the time of stream processing we need to take an interesection of adjacency list of both source and destinantion. If the intersection consists of even 1 node, then it is trusted else not.

This simply becomes a O(n) intersection and would be slower than the previous O(1) lookup but would be using lesser memory by storing nodes while precomputation only till depth x/2 rather than x.

### Problems
Slower than before as dependant on the number of nodes(n)

## 3.Simple(Low memory) Bidirectional without Precomputation
### Algorithm:

For allowing "trusted" till level x:

No precomputation

During the stream, go through the adjeceny list of original graph of both source and destinantion and create sets for both having node till depth x/2. Then take interesection of both these sets. If the intersection consists of even 1 node, then it is trusted else not.

This simply becomes a O(n^(x)) intersection and would be slowest and not recommended during stream processing.
I would not use after level2. Till level2 this is fine.

### Problems
Very slow at the time of batch processing and it will slow down as depth(x) increases or number of node in graph(n) increases.


# Submission Details
In my submission, I have made two files AntiFraud16 to run on 16gb ram and AntiFraud8 to run on 8gb ram.

According to my analysis:
On 8gb ram, doing precomputation results in lots of clashes hence it is advisable to use Method3.
On 16gb ram, we can real differecne in the Medthod3 and Method2. Hence i have used method2.
My times on 16gb for level4 with method2 were half of that with method3.





