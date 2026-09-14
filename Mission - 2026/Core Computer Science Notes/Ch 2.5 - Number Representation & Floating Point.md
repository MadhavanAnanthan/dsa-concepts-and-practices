# Number Representation & Floating Point

## 1. The Core Problem
Computers operate entirely using binary circuits (voltages representing $1$s and $0$s). While representing whole positive integers is straightforward, computers must also handle:
* Negative numbers
* Fractional / decimal numbers (e.g., $3.14159$)
* Extremely large or small scientific numbers (e.g., $6.022 \times 10^{23}$)

---

## 2. Integer Representation: Two's Complement
To handle both positive and negative integers cleanly without wasting circuit design on separate sign indicators, modern processors use **Two's Complement**.

* **The Sign Bit:** The most significant bit (the leftmost bit) acts as the sign flag:
    * `0` = Positive number
    * `1` = Negative number
* **How to Form a Negative Number:**
    1. Take the positive binary value.
    2. Invert all the bits ($0 \leftrightarrow 1$) — known as One's Complement.
    3. Add `1` to the resulting value.
* **Example (8-bit):**
    * $+5$ is stored as: `00000101`
    * $-5$ is computed by inverting (`11111010`) and adding `1`: `11111011`

---

## 3. Floating-Point Representation: IEEE 754 Standard
Physical ALUs primarily perform integer arithmetic and logic shifting. To handle fractions and decimals, the computing industry adopted the **IEEE 754 standard**, which maps a decimal number into scientific binary notation:

$$\text{Value} = (-1)^{\text{Sign}} \times \text{Mantissa} \times 2^{\text{Exponent}}$$

A 32-bit single-precision floating-point number breaks down into three hardware parts:
1. **Sign Bit ($1$ bit):** Determines if the number is positive ($0$) or negative ($1$).
2. **Exponent ($8$ bits):** Manages the magnitude, shifting the binary point left or right.
3. **Mantissa / Fraction ($23$ bits):** Holds the actual precision/significant digits of the number.

---

## 4. The Hidden Trap: Precision Loss
Because floating-point numbers must squeeze infinite possible fractions into a fixed number of bits, **they cannot accurately represent every decimal fraction**.

* **Classic Programming Issue:** $0.1 + 0.2$ often results in `0.30000000000000004` instead of a clean `0.3`.
* **The Root Cause:** Fractions like $1/10$ cannot be neatly expressed as finite sums of powers of two ($1/2, 1/4, 1/8, \dots$), leading to infinite repeating binary fractions that get cut off (truncated) by the fixed-width register limit.