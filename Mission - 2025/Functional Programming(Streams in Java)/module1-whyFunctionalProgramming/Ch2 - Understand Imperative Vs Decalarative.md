<h2>Imperative vs. Declarative Programming in Java</h2>

<h3>1. Imperative Approach (Focusing on "How")</h3>

<p>Imagine we want to find the sum of all even numbers in a list of integers.</p>

<pre><code class="language-java">
import java.util.ArrayList;
import java.util.List;

public class ImperativeExample {
    public static void main(Stringargs) {
        List&lt;Integer&gt; numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        int sum = 0;

        for (int number : numbers) { // Explicitly iterating
            if (number % 2 == 0) { // Checking the condition
                sum += number; // Modifying the 'sum' variable
            }
        }

        System.out.println("Sum of even numbers: " + sum);
    }
}
</code></pre>

<p><strong>Explanation:</strong></p>

<ul>
    <li><strong>Step-by-step instructions:</strong> The code explicitly tells the computer <em>how</em> to calculate the sum. It iterates through the list, checks each number, and updates the <code>sum</code> variable.</li>
    <li><strong>Mutable state:</strong> The <code>sum</code> variable is modified within the loop, representing a change in the program's state.</li>
</ul>

<h3>2. Declarative Approach (Focusing on "What") - Using Java Streams</h3>

<p>Now, let's achieve the same result using Java Streams, which employ a declarative style.</p>

<pre><code class="language-java">
import java.util.List;

public class DeclarativeExample {
    public static void main(Stringargs) {
        List&lt;Integer&gt; numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        int sum = numbers.stream()
                .filter(number -> number % 2 == 0) // What: Filter even numbers
                .reduce(0, Integer::sum); // What: Sum the filtered numbers

        System.out.println("Sum of even numbers: " + sum);
    }
}
</code></pre>

<p><strong>Explanation:</strong></p>

<ul>
    <li><strong>Focus on the result:</strong> The code describes <em>what</em> we want to achieve (filter even numbers and sum them), rather than explicitly specifying the steps.</li>
    <li><strong>Abstraction:</strong> The <code>stream</code>, <code>filter</code>, and <code>reduce</code> methods abstract away the underlying iteration and state management.</li>
    <li><strong>No explicit state modification:</strong> We don't manually modify a <code>sum</code> variable. The <code>reduce</code> operation handles the aggregation.</li>
</ul>

<h3>Key Differences Highlighted:</h3>

<ul>
    <li><strong>Imperative:</strong>
        <ul>
            <li>You control the loop.</li>
            <li>You control the state of variables.</li>
            <li>It is more like giving commands.</li>
        </ul>
    </li>
    <li><strong>Declarative:</strong>
        <ul>
            <li>You describe the desired result.</li>
            <li>The underlying system handles the execution.</li>
            <li>It is more like describing what you want.</li>
        </ul>
    </li>
</ul>

<h3>Another Example, SQL</h3>

<p>SQL is a very strong example of declarative programming.</p>

<pre><code class="language-sql">
SELECT SUM(salary) FROM employees WHERE department = 'Sales';
</code></pre>

<p>In this SQL statement, you declare <em>what</em> you want (the sum of salaries in the Sales department), and the database system figures out <em>how</em> to retrieve and calculate the result.</p>