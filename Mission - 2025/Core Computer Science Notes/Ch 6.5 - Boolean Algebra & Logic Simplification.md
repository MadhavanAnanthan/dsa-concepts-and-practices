# Boolean Algebra & Logic Simplification

## 1. Overview
Boolean algebra is the mathematical foundation of computer science and digital logic design. While standard algebra deals with real numbers and continuous quantities, Boolean algebra deals strictly with **binary variables** (`TRUE` / `FALSE`, or `1` / `0`).

It provides the mathematical rules used by engineers to design, analyze, and optimize the digital circuits (logic gates) inside processors and memory chips.

---

## 2. Core Boolean Operations (Logic Gates)
At the hardware level, everything computed by the CPU boils down to three primary Boolean operations:

* **AND (Conjunction - Symbol: $\cdot$ or implicit):**
    * Returns `1` only if **all** inputs are `1`.
    * *Logic:* $A \cdot B = 1$ only if $A = 1$ and $B = 1$.
* **OR (Disjunction - Symbol: $+$):**
    * Returns `1` if **at least one** input is `1`.
    * *Logic:* $A + B = 1$ if $A = 1$, $B = 1$, or both.
* **NOT (Negation - Symbol: $\bar{A}$ or $A'$):**
    * Inverts the input state.
    * *Logic:* If $A = 1$, $\bar{A} = 0$.

---

## 3. Fundamental Laws of Boolean Algebra
These rules allow computer scientists and hardware designers to manipulate and simplify expressions:

* **Identity Laws:**
    * $A + 0 = A$
    * $A \cdot 1 = A$
* **Null (Dominance) Laws:**
    * $A + 1 = 1$
    * $A \cdot 0 = 0$
* **Idempotent Laws:**
    * $A + A = A$
    * $A \cdot A = A$
* **Complement Laws:**
    * $A + \bar{A} = 1$
    * $A \cdot \bar{A} = 0$
* **Involution Law:**
    * $\bar{\bar{A}} = A$

---

## 4. Key Theorems for Logic Simplification

* **Commutative Law:** Order does not matter.
    * $A + B = B + A$
    * $A \cdot B = B \cdot A$
* **Associative Law:** Grouping does not matter.
    * $A + (B + C) = (A + B) + C$
    * $A \cdot (B \cdot C) = (A \cdot B) \cdot C$
* **Distributive Law:** Factoring and expanding expressions.
    * $A \cdot (B + C) = (A \cdot B) + (A \cdot C)$
    * $A + (B \cdot C) = (A + B) \cdot (A + C)$
* **De Morgan’s Theorems (Crucial for Hardware Optimization):**
    * $\overline{A + B} = \bar{A} \cdot \bar{B}$ (NOR is equivalent to inverted inputs combined with AND)
    * $\overline{A \cdot B} = \bar{A} + \bar{B}$ (NAND is equivalent to inverted inputs combined with OR)

---

## 5. Why Logic Simplification Matters in Computer Science

1. **Hardware Cost Reduction:** Fewer Boolean expressions mean fewer physical logic gates (transistors) needed on the silicon die, reducing manufacturing costs and chip size.
2. **Speed & Performance:** Simplified circuits have fewer gate delays (shorter propagation paths), allowing the system clock to run at higher frequencies.
3. **Power Efficiency:** Fewer active transistors draw less electrical current and generate less heat, which is critical for CPUs in modern mobile devices and laptops.