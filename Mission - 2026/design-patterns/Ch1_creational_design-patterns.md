
Creational Design Pattern

1. Singleton
2. Factory
Factory pattern is consider a factory, which builds anything for us based on our need.
Factory is a creational design pattern that removes object creation responsibility from the caller and centralises it inside a dedicated Factory class. The caller only knows the interface — never the concrete class. When a new type is added, only the Factory changes — nothing else in the codebase is touched. This directly solves DRY violation caused by scattered if-else or switch creation logic repeated across multiple files, and makes object creation mockable and testable in isolation.

3. Builder

The one-line summary to remember

1. Builder = another class that safely assembles all arguments, protects optional fields from null using defaults, then hands a fully valid immutable object to you.
2. Builder pattern is used when a class has many arguments, especially optional ones. A separate Builder class is responsible for collecting those arguments step by step. Optional fields get safe default values in the Builder so they are never null. Each method in the Builder sets one field and returns the same Builder. Finally build() validates everything and creates the real object. The real object has only getters — no setters — making it immutable and safe from being changed after creation.