Stream API is a library and all of the Stream operations are available in java.util.stream package, it comes under java.base module (it is the master module for all java native packages).

streamApi helps to process the data efficiently using declarative programming approach, we can process a collection or array data's, Stream API's advantage is its not manipulating the source as well as its not storing the data.

Defintion :
Stream API is mainly used for processing collections in a declarative and functional style — allowing transformation, filtering, aggregation, sorting, grouping, and parallel processing in a clean, readable pipeline. It reduces boilerplate, improves maintainability, and makes it easy to handle large datasets efficiently.

Few points to know
1. Stream extending Autoclosable internally, so we can use in try with resource, if we performing any IO operations, for collections related operations its not required.
2. Stream performs 3 level - Creation, Intermediate operation, terminal operation.
3. If we perform any intermediate operation, a new stream will be created, it will not change the source data.
4. Simply stream will expect Terminal Stage as mandatory to process the stream, otherwise it won't start processing.
5. Stream will process each element one by one, it will go to each Stage followed by Terminal Stage or directly Terminal Stage.
6. Also, if terminal operation is executed , the stream is closed. If we try to access the Stream will get this error - "stream has already been operated upon or closed".

## statefull vs Statefull
Some operation such as Distinct, Sorted Because these are statefull. Statefull represents to store the data. It will store all data then perform operation and produce results.
flow of the pipeline, when we using stateful operations.

```java
public static void main(String[] args) {
    List<String> list = Arrays.asList("sam", "anil", "sunil");
    list.stream().filter(name -> name.startsWith("s")).sorted().collect(Collectors.toList());
}
```
Step 1 — Filtering (filter(name -> name.startsWith("S")))

Processes each name one-by-one.

Passes only names starting with "S" immediately to the next stage (sorted).

At this point, sorted does not yet output anything — it’s just receiving elements.

Step 2 — Sorting (sorted())

Keeps an internal buffer to store all names it receives from filter.

Waits until the entire upstream source is exhausted (no more names coming from filter).

Sorts the buffered names using:

Natural order by default (Comparator.naturalOrder()), or

Your provided comparator if given.

Step 3 — Output to Collector (collect(Collectors.toList()))

After sorting is fully complete, it sends elements one-by-one downstream to the collector.

Collector builds your final list by adding each sorted element.

4. Stream internally using Splititerator (iterator extending iterable), based on the flag in the parameter it will perform the respective operation like sequential or parallel but both will call spliterator.
```java
default Stream<E> stream() {
        return StreamSupport.stream(spliterator(), false);
    }
```

```java
  default Stream<E> parallelStream() {
    return StreamSupport.stream(spliterator(), true);
}
```
5. Sequential Stream will process the data one by one, if we have 10 elements it'll run internally by 10 times.
6. Each stages or conditions in Stream are knows as pipelines.
   There are 3 stages in Streams
    1. Creation (invoke the data source).
    2. Intermediate Operation
    3. Terminal Operation
       Increase code readability and less number of lines.
7. 	1. Creation Operation

• This is where the stream is generated from a data source.
• Common sources include:
• Collections (List, Set, Map)
• Arrays
• I/O channels
• Generator functions (e.g., Stream.of, Stream.generate, Stream.iterate)


	2. Intermediate Operation in Stream

• These operations transform the stream into another stream.
• They are lazy, meaning they are executed only when a terminal operation is applied.
• Common intermediate operations include:
• filter: Filters elements based on a predicate.
• map: Transforms elements into a different type.
• flatMap: Maps each element to a stream and flattens the result.
• sorted: Sorts the stream elements.
• distinct: Removes duplicate elements.

	3. Terminal Operation

• This operation consumes the stream and produces a result.
• Once a terminal operation is executed, the stream is closed, and no further operations can be performed on it.
• Common terminal operations include:
• forEach: Performs an action for each element.
• collect: Collects elements into a collection.
• reduce: Reduces elements to a single value.
• anyMatch, allMatch, noneMatch: Performs boolean checks on elements.
findFirst, findAny: Returns an optional containing the first or any element.
