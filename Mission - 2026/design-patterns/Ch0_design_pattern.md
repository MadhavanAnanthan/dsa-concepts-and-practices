# Why Design Patterns?

## The Bigger Picture

Software systems grow continuously:

- New features get added
- Business rules change
- Teams grow
- Codebases become larger

Without a good design, code becomes:

- Hard to maintain
- Hard to extend
- Full of duplication
- Tightly coupled
- Difficult to test

Design patterns emerged because developers repeatedly faced the same design problems and discovered similar solutions.

> Design Patterns are proven, reusable solutions to recurring software design problems.

---

# Relationship Between OOP, SOLID and Design Patterns

Think of it like this:

```text
OOP = Building Blocks

SOLID = Design Rules

Design Patterns = Proven Solutions
```

Or:

```text
OOP
 ↓
SOLID
 ↓
Design Patterns
```

### OOP gives us

- Encapsulation
- Abstraction
- Inheritance
- Polymorphism
- Composition

### SOLID gives us

- Single Responsibility Principle (SRP)
- Open Closed Principle (OCP)
- Liskov Substitution Principle (LSP)
- Interface Segregation Principle (ISP)
- Dependency Inversion Principle (DIP)

### Design Patterns give us

Repeated solutions to recurring problems.

Examples:

- Strategy
- Factory
- Observer
- Builder
- Decorator
- Specification

---

# My Favorite Way to Remember

SOLID tells you:

> How to design classes correctly.

Design Patterns tell you:

> How multiple classes should collaborate to solve common problems.

---

# Principles vs Patterns

SOLID gives you the principles to design each class well.

DRY tells you not to repeat logic across files.

KISS tells you not to add complexity you don't need.

YAGNI tells you not to build features you don't need yet.

Design patterns are the solutions you reach for when these principles are being violated.

> The principles help you identify the problem.  
> The design pattern often provides the solution.

---

# Are Design Patterns Only For OOP?

Mostly yes.

Most famous design patterns (GoF patterns) were created for Object-Oriented Programming and heavily rely on:

- Objects
- Interfaces
- Polymorphism
- Composition
- Encapsulation

Examples:

- Strategy
- Factory
- Observer
- Decorator
- Command

However, the underlying ideas can appear in other programming styles as well.

---

# Why Design Patterns Will Never Die

Technologies change:

```text
EJB -> Spring
VM -> Containers
Monolith -> Microservices
```

Languages change:

```text
Java
C#
Python
Go
TypeScript
```

But software design problems remain the same:

```text
How to create objects?
How to avoid large if-else blocks?
How to make code extensible?
How to reduce coupling?
How to assign responsibilities?
```

Design patterns solve these timeless problems.

That's why they remain relevant regardless of framework or language.

---

# Realization

Design patterns were not invented first.

The process was:

```text
Developers built many systems
↓
Certain solutions kept repeating
↓
Those solutions were documented
↓
The documented solutions became Design Patterns
```

> Design Patterns are discovered, not invented.

---

# Do I Need To Memorize Patterns?

No.

A better approach:

1. Learn OOP deeply
2. Learn SOLID deeply
3. Practice LLD problems
4. Observe recurring designs

Over time, many patterns appear naturally.

---

# Important Insight

If you consistently apply:

- OOP
- SOLID
- DRY
- KISS
- YAGNI

you will often end up implementing Design Patterns without intentionally thinking about them.

Example:

```text
Polymorphism + OCP + SRP
↓
Often leads to Strategy Pattern
```

```text
Encapsulating object creation
↓
Often leads to Factory Pattern
```

```text
Encapsulating business rules
↓
Often leads to Specification Pattern
```

---

# Senior Engineer Mental Model

Don't think:

"I need to use a Design Pattern."

Think:

- What responsibility changes frequently?
- What is tightly coupled?
- What may grow in the future?
- What violates SOLID?

Then choose a pattern only if it naturally fits.

---

# One-Line Summary

> Design Patterns are named, proven applications of OOP and SOLID principles that help manage object responsibilities, collaboration, extensibility, maintainability, and recurring software design problems.
