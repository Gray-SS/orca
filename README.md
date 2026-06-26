# Orca

Orca is a statically-typed, JVM-targeting programming language with a clean syntax and first-class interop with Java libraries and native bindings. It compiles to JVM bytecode and integrates with the Gradle build system.

---

## Table of Contents

- [Getting Started](#getting-started)
- [Contributing](#contributing)
- [Language Reference](#language-reference)
  - [Hello, World!](#hello-world)
  - [Packages and Imports](#packages-and-imports)
  - [Primitive Types](#primitive-types)
  - [Type System](#type-system)
    - [Implicit widening conversions](#implicit-widening-conversions)
    - [No narrowing conversions](#no-narrowing-conversions)
  - [Variables](#variables)
    - [Declaration contexts](#declaration-contexts)
    - [Constants](#constants)
  - [Collections](#collections)
    - [Structural Typing](#structural-typing)
  - [Functions and Methods](#functions-and-methods)
    - [Instance Methods](#instance-methods)
    - [Static Methods](#static-methods)
    - [Free Functions](#free-functions)
  - [Static vs Instance Access](#static-vs-instance-access)
  - [Arrays](#arrays)
  - [Control Flow](#control-flow)
  - [Operators](#operators)
  - [Built-ins](#built-ins)
  - [Strings](#strings)
  - [Java Interop](#java-interop)
  - [Native Library Interop](#native-library-interop)
  - [Full Example](#full-example)

---

## Getting Started

**Prerequisites:** Java 21 or later.

```bash
# Clone the repository
git clone https://github.com/Gray-SS/orca.git
cd orca

# Build all modules
./gradlew build

# Run all tests
./gradlew test

# Run the compiler CLI; This will print the help message
./gradlew :modules:cli:run --args="--help"

```

---

## Contributing

1. Fork the repository and clone it locally
2. Create a branch for your work: `git checkout -b my-fix`
3. Build the project: `./gradlew build`
4. Run the tests to make sure everything passes: `./gradlew test`
5. Make your changes, then run the tests again
6. Open a pull request — a draft PR is welcome if you want early feedback

If you have questions at any point, feel free to comment on the issue.

---

## Language Reference

### Hello, World!

```orca
def main() {
    std::io::println("Hello, World!");
}
```

`std::io::println` and `std::io::print` accept any primitive type directly — no conversion needed for output.

---

### Packages and Imports

Files are organized into packages using `::` as the path separator. A file declares its package at the top, and other files are imported by their fully qualified path.

```orca
package mypackage::submodule;

import foo::bar;        # import the bar namespace (symbols accessed with bar::Baz)
import foo::bar::Baz;  # import a specific symbol (Baz can be accessed directly)
```

Java standard library types and third-party JVM libraries are imported the same way, making Java interop seamless.

---

### Primitive Types

| Type     | Description           |
| -------- | --------------------- |
| `byte`   | 8-bit integer         |
| `short`  | 16-bit integer        |
| `int`    | 32-bit integer        |
| `long`   | 64-bit integer        |
| `float`  | 32-bit floating point |
| `double` | 64-bit floating point |
| `char`   | Single character      |
| `string` | String of characters  |

---

### Type System

#### Implicit widening conversions

Numeric types widen implicitly along this chain when the target type is wider:

```
byte → short → int → long → float → double
```

`char` widens implicitly to `int`, `long`, `float`, and `double`.

This means you can assign a narrower type where a wider one is expected without any extra syntax:

```orca
let x: float = 10;     # int 10 widened to float
let y: double = x;     # float widened to double
let c: int = 'A';      # char widened to int (gives 65)
```

#### No narrowing conversions

Orca has no cast operator. Narrowing a type (e.g. `double` to `int`) is not possible in source code. Use a built-in like `floor` or `ceil` when you need to convert a floating-point value to an integer.

```orca
let n := floor(3.9);   # 3  — float → int via built-in
let m := ceil(1.1);    # 2  — float → int via built-in
```

---

### Variables

Variables are declared with `let` (immutable) or `var` (mutable). The `:=` operator declares and initializes; plain `=` reassigns.

```orca
let name := "Alice";   # immutable, type inferred as string
var count := 0;        # mutable, type inferred as int
count = count + 1;     # reassignment
```

A type annotation can be added explicitly with `:`:

```orca
var total: double;
```

Compound assignment operators are also available:

```orca
count += 1;
count -= 1;
count *= 2;
count /= 4;
count %= 3;
```

#### Declaration contexts

Where a variable is declared determines how it is accessed and what the compiler allows.

**Local variables** are declared inside a function or method body. They are scoped to the enclosing block and are not accessible from outside.

```orca
def add(a: int, b: int): int {
    let result := a + b;   # local — only visible inside add
    return result;
}
```

**Associated variables** are declared inside an `impl` block, outside of any method. They belong to the collection type and are accessed with `::`.

```orca
impl AppConfig {
    var retryCount := 3;          # mutable associated variable
    let defaultTimeout := 5000;   # immutable associated variable
}

AppConfig::retryCount = 5;
```

**Free variables** are declared at the module level, outside any `coll` or `impl`. They are accessible anywhere in the same file without qualification.

```orca
var requestCount := 0;    # module-level, mutable

def handleRequest() {
    requestCount += 1;
}
```

#### Constants

The `const` keyword declares a compile-time constant. Constants must have a **primitive type** and their initializer must be a compile-time foldable expression — a literal or an expression composed entirely of other constants.

```orca
const Pi := 3.14159;              # float constant
const MaxRetries := 5;            # int constant
const AppName := "MyApp";         # string constant
const Doubled := MaxRetries * 2;  # valid: folds to 10 at compile time
```

Constants are never reassignable. A collection instance or array cannot be `const`.

---

### Collections

A `coll` defines a data type — similar to a record or struct. Its body declares instance fields.

```orca
coll Point {
    x: float;
    y: float;
}
```

An `impl` block adds constants, static variables, and methods:

```orca
impl Point {
    const Origin := Point(0.0, 0.0);

    def new(x: float, y: float): Point {
        return Point(x, y);
    }

    def distanceTo(self, other: Point): float {
        let dx := self.x - other.x;
        let dy := self.y - other.y;
        return Math::sqrt(dx * dx + dy * dy);
    }
}
```

Instances are created by calling the collection name with positional arguments matching its declared fields:

```orca
let p := Point(1.0, 2.0);
```

#### Structural Typing

Collections use **structural typing**: two collections with the same field names and types are mutually assignable, regardless of their names.

```orca
coll Point2D { x: float; y: float; }
coll Vector2D { x: float; y: float; }

let p := Point2D(1.0, 2.0);
let v: Vector2D = p;  # valid — same shape
```

This means compatibility is determined by structure, not by declaration name.

---

### Functions and Methods

Functions are declared with `def`. The return type, if any, follows the parameter list after `:`. A function with no explicit return type returns nothing.

```orca
def main() { ... }                        # no return value
def add(a: int, b: int): int { ... }      # returns int
def isEven(n: int): bool { ... }          # returns bool
def greet(name: string): string { ... }   # returns string
```

#### Instance Methods

Instance methods receive the collection instance as an explicit `self` parameter:

```orca
def display(self): string {
    return "(" + str(self.x) + ", " + str(self.y) + ")";
}
```

#### Static Methods

Methods inside `impl` without `self` act as static functions scoped to the collection:

```orca
def fromAngle(angle: float, radius: float): Point {
    return Point(Math::cos(angle) * radius, Math::sin(angle) * radius);
}
```

Called as `Point::fromAngle(angle, radius)`.

#### Free Functions

Functions can also be declared at the module level, outside any `coll` or `impl`:

```orca
def clamp(value: int, min: int, max: int): int {
    if (value < min) { return min; }
    if (value > max) { return max; }
    return value;
}
```

---

### Static vs Instance Access

Static members (constants, static variables, static methods) declared inside `impl` are accessed with `::`:

```orca
Point::Origin
AppConfig::maxRetries
Logger::log("started")
```

Instance fields and methods are accessed with `.`, and can be chained:

```orca
p.x
p.distanceTo(other)
path.toAbsolutePath().normalize().toString()
```

---

### Arrays

Arrays use Java-style syntax. Multi-dimensional arrays are supported.

```orca
int[] scores;                 # 1D array field
let scores := int[](10);     # allocate array of 10 ints
scores[0] = 100;             # element assignment

int[][] grid;                 # 2D array field
let grid := int[][](rows);   # allocate outer array
grid[i] = int[](cols);      # allocate inner arrays
grid[i][j] = 0;             # element assignment
```

---

### Control Flow

#### If / Else

```orca
if (score >= 90) {
    return "A";
} else if (score >= 75) {
    return "B";
} else {
    return "C";
}
```

#### While

```orca
var i := 0;
while (i < 10) {
    std::io::println(i);
    i += 1;
}
```

#### For

```orca
for (var i := 0; i < 10; i++) {
    std::io::println(i);
}
```

---

### Operators

#### Arithmetic

| Operator | Meaning        |
| -------- | -------------- |
| `+`      | Addition       |
| `-`      | Subtraction    |
| `*`      | Multiplication |
| `/`      | Division       |
| `%`      | Modulo         |

#### Comparison

| Operator | Meaning               |
| -------- | --------------------- |
| `==`     | Equal                 |
| `!=`     | Not equal             |
| `<`      | Less than             |
| `<=`     | Less than or equal    |
| `>`      | Greater than          |
| `>=`     | Greater than or equal |

#### Logical

| Operator | Meaning     |
| -------- | ----------- |
| `&&`     | Logical and |
| `\|\|`   | Logical or  |
| `!x`     | Logical not |

#### Unary

| Operator | Meaning   |
| -------- | --------- |
| `-x`     | Negation  |
| `x++`    | Increment |
| `x--`    | Decrement |

#### Combined Examples

```orca
# range check
if (x >= 0 && x < width && y >= 0 && y < height) {
    std::io::println("in bounds");
}

# FizzBuzz
for (var i := 1; i <= 100; i++) {
    if (i % 15 == 0) {
        std::io::println("FizzBuzz");
    } else if (i % 3 == 0) {
        std::io::println("Fizz");
    } else if (i % 5 == 0) {
        std::io::println("Buzz");
    } else {
        std::io::println(i);
    }
}

# absolute value
def abs(x: float): float {
    if (x < 0.0) { return -x; }
    return x;
}

# test if a number is outside a range
def outOfRange(value: int, lo: int, hi: int): bool {
    return !(value >= lo && value <= hi);
}

# clamp with compound assignment
def clamp(v: int, lo: int, hi: int): int {
    var result := v;
    if (result < lo) { result = lo; }
    if (result > hi) { result = hi; }
    return result;
}
```

---

### Built-ins

Orca provides a small set of built-in functions available without any import.

#### `not(bool) -> bool`

Logical negation. Equivalent to the `!` prefix operator.

```orca
if (!isReady()) { return; }
while (!done) { ... }
```

#### `str(T) -> string`

Converts any primitive value to its string representation. Accepts `byte`, `short`, `int`, `long`, `float`, `double`, `bool`, `char`, and `string`.

```orca
let msg := "Count: " + str(count);
let label := str(3.14);
```

String concatenation with `+` requires both sides to already be strings — numeric types must be explicitly converted with `str()` first:

```orca
let age := 30;
std::io::println("Age: " + str(age));   # correct
# std::io::println("Age: " + age);      # type error
```

#### `floor(float) -> int`

Returns the largest integer less than or equal to the given value.

```orca
let n := floor(3.9);   # 3
let m := floor(-1.2);  # -2
```

#### `ceil(float) -> int`

Returns the smallest integer greater than or equal to the given value.

```orca
let n := ceil(3.1);    # 4
let m := ceil(-1.8);   # -1
```

#### `length(string) -> int` / `length(array) -> int`

Returns the length of a string or array.

```orca
let n := length("hello");       # 5
let k := length(scores);        # number of elements in scores array
```

---

### Strings

String literals support common escape sequences:

| Sequence | Meaning      |
| -------- | ------------ |
| `\n`     | Newline      |
| `\"`     | Double quote |
| `\\`     | Backslash    |

```orca
let msg := "Line one\nLine two";
let path := "C:\\Users\\alice";
let quoted := "He said \"hello\"";
```

---

### Java Interop

Orca can import and call Java standard library and third-party JVM classes directly:

```orca
import java::nio::file::Files;
import java::nio::file::Path;

let path := Path::of("data.txt");
if (!Files::exists(path)) {
    std::io::println("File not found");
}
let content := Files::readString(path);
```

---

### Native Library Interop

Orca supports bindings to native libraries via JNI. The following example uses a Raylib binding for windowing:

```orca
import com::raylib::Raylib;
import com::raylib::Colors;

Raylib::InitWindow(800, 600, "My App");
while (!Raylib::WindowShouldClose()) {
    Raylib::BeginDrawing();
    Raylib::ClearBackground(Colors::RAYWHITE);
    Raylib::DrawText("Hello, Orca!", 10, 10, 20, Colors::BLACK);
    Raylib::EndDrawing();
}
```

---

### Full Example

A small program that models a 2D point, computes distances, and prints a report:

```orca
package geometry;

import java::lang::Math;

coll Point {
    x: float;
    y: float;
}

impl Point {
    def new(x: float, y: float): Point {
        return Point(x, y);
    }

    def distanceTo(self, other: Point): float {
        let dx := self.x - other.x;
        let dy := self.y - other.y;
        return Math::sqrt(dx * dx + dy * dy);
    }

    def display(self): string {
        return "(" + str(self.x) + ", " + str(self.y) + ")";
    }
}

def main() {
    let points := Point[](3);
    points[0] = Point::new(0.0, 0.0);
    points[1] = Point::new(3.0, 0.0);
    points[2] = Point::new(3.0, 4.0);

    for (var i := 0; i < length(points); i++) {
        for (var j := i + 1; j < length(points); j++) {
            let a := points[i];
            let b := points[j];
            let dist := a.distanceTo(b);
            std::io::println(a.display() + " -> " + b.display() + " = " + str(dist));
        }
    }
}
```
