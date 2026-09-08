# OOP + SOLID + Design Patterns — Practical Learning Notes

## Big Picture

```text
OOP gives you tools/principles
SOLID gives you design guidelines
Design patterns give you reusable solutions
```

---

# OOP Principles — What They Really Help With

## Encapsulation
→ Protect object state and rules.

Think:
- Who owns this data?
- Who is allowed to change it?
- How should that state be changed safely?

**Rule:**  
The class that owns the state should own the rules for changing that state.

---

## Abstraction
→ Define boundaries and hide unnecessary details.

Think:
- What should the caller know?
- What should the caller NOT need to know?

Examples:
- `list.add()` hides internal resizing.
- `synchronized` hides low-level locking/memory details.
- A payment interface hides provider-specific implementation.

**Rule:**  
Expose only what the caller needs; hide how it is done.

---

## Inheritance
→ Model a valid IS-A relationship.

Examples:
- `CardPayment IS-A Payment`
- `Dog IS-A Animal`

**Rule:**  
Do not use inheritance only for code reuse.

Ask:
- Is this really an IS-A relationship?
- Would composition be cleaner?

---

## Polymorphism
→ Allow interchangeable implementations.

Example:

```text
PaymentGateway
→ PayPalGateway
→ RazorpayGateway
→ StripeGateway
```

Caller depends on:

```text
PaymentGateway
```

not on one concrete provider.

**Rule:**  
New implementations should be pluggable without forcing changes in existing caller logic.

---

# OOP → SOLID → Design Patterns

```text
OOP
→ building blocks

SOLID
→ rules for using those blocks well

Design Patterns
→ reusable arrangements of those blocks
```

A design can use all three together.

Example:

```text
PaymentGateway interface
        ↓
PayPalGateway
RazorpayGateway
StripeGateway
```

This can involve:

- Abstraction
- Polymorphism
- Composition
- Open/Closed Principle
- Dependency Inversion
- Strategy Pattern

---

# Questions to Ask While Designing

Before coding, repeatedly ask:

```text
Who owns this data?
Who is allowed to change it?
What should the caller know?
What should the caller NOT know?
Is this IS-A or HAS-A?
Can I add a new behavior without modifying existing code?
Is this class doing too many things?
```

These questions are more useful than memorizing definitions.

---

# Practical Design Order

Use this order when solving machine-coding / LLD problems:

```text
1. Understand requirements
2. Identify entities
3. Decide responsibilities
4. Encapsulate state
5. Define interfaces only where variation exists
6. Prefer composition where appropriate
7. Use polymorphism where implementations can vary
8. Check SOLID violations
9. Apply a design pattern only if it actually helps
```

## Important Rule

> Do not start with: “Which design pattern should I use?”

Start with the problem.

Let the pattern emerge from the design need.

---

# Example — Payment System

Requirements:

```text
PayPal
Razorpay
Stripe
```

Observation:

```text
Payment behavior varies by provider.
```

Possible abstraction:

```text
PaymentGateway
→ PayPalGateway
→ RazorpayGateway
→ StripeGateway
```

Benefit:

```text
PaymentService
→ depends on PaymentGateway
→ new provider can be added
→ existing caller logic may stay unchanged
```

This naturally leads to polymorphism and can resemble the Strategy Pattern.

---

# Machine Coding / LLD Practice Problems

Practice these gradually:

- Parking Lot
- Vending Machine
- Library Management
- Logger
- Notification System
- Payment System
- Splitwise
- Elevator
- Cache
- Rate Limiter
- BookMyShow-like booking

---

# Practice Every 2–3 Days

## Do one small machine-coding / LLD problem every 2–3 days.

For each problem:

```text
1. Solve without trying to force a pattern.
2. Identify entities and responsibilities.
3. Write the classes/interfaces.
4. Check encapsulation.
5. Check IS-A vs HAS-A.
6. Check where behavior varies.
7. Check whether polymorphism helps.
8. Review SOLID violations.
9. Only then identify any design pattern used.
10. Review the solution with AI.
```

---

# Learning Rule

> Reading definitions repeatedly will not build design skill.

Better approach:

```text
Learn concept
→ implement it
→ review mistakes
→ refactor
→ repeat
```

---

# Wall Notes / Rules to Remember

```text
Encapsulation
→ protect state and rules

Abstraction
→ set boundaries; expose what, hide how

Inheritance
→ use only for a meaningful IS-A relationship

Composition
→ HAS-A; often more flexible than inheritance

Polymorphism
→ same contract, interchangeable implementations

SOLID
→ helps classes stay focused, extensible, testable, maintainable

Design Patterns
→ use only when the problem naturally needs them

Do not force patterns.
Understand the problem first.

Who owns this data?
Who can change it?
What should the caller know?
Can I add new behavior without changing existing code?
Is this class doing too much?
```

---

# Best Way to Master

```text
Definitions → only enough to understand
Implementation → main learning
Code review → identify mistakes
Refactoring → build design judgment
Repetition → make it natural
```

**Goal:**  
Do not memorize OOP/SOLID/design-pattern definitions. Build enough practical experience that the right design decisions start becoming natural.
