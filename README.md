[![Main](https://github.com/rm3dom/safer/actions/workflows/main.yml/badge.svg)](https://github.com/rm3dom/safer/actions/workflows/main.yml)


# Safer - Kotlin Compiler Plugin

Safer is a Kotlin compiler plugin focused on enhancing code safety by ensuring return values are used and that
potentially unsafe function calls are made explicitly.

Another way of thinking of Safer is, it's a 'mass deprecation' tool and check return linter. 
With Safer you can "annotate" third party libraries and make them a little Safer to use.

## But why?

I have a condition called being a parent, so I have a foggy brain, and stupid little annoying bugs slip into my code
~~because safety is not the number one concern for Kotlin. (Which is fair, Kotlin has to solve many problems on many
targets)~~ (Kotlin will be much safer in upcoming versions and will include things like Rich Errors and CheckReturnValue, 
making Safer less useful).

I used to use [Elm](https://elm-lang.org/) a lot and a little Rust, but it's mostly Kotlin because memory and startup
times are not really a concern in my applications. Multiplatform and code reuse are most important in my use-cases.
What I like about those languages is their safety; especially Elm, zero runtime errors, or panics, in Elm.

**Example 1:**

I use sealed classes for error handling, but the compiler does not enforce its usage. I also hate the fact that I have
to annotate all my functions with `@CheckReturnValue` or `@Contract(pure=true)`. Ideally, I can just annotate my
boxed or sealed type, and all functions that return that type are automatically checked:

```kotlin
@CheckReturnValue
sealed interface MyResultType {
   object Ok : MyResultType
   //....
}
```

**Example 2:**

Consider the ambiguity in this common Kotlin code:

```kotlin
val list = listOf(1234)
//...
list[i]
```

Beyond the unused result, it's unclear if a potential IndexOutOfBoundsException is intentional or an oversight.
Relying on try-catch blocks higher up the call stack can be fragile.

A safer alternative, like this, improves clarity:

```kotlin
val list = listOf(1234)
//...
list.getOrNull(i) ?: return outOfBoundsError("Postal code") //or throw if you really must, at-least it's explicit
```

While Safer aims for even better solutions through deeper analysis, Safer currently prioritizes explicit safety 
inspired by Elm.
Note that primitive array indexing is not reported due to boxing.

**Example 3:**

```kotlin
File("").mkdirs()
```

The result of the `mkdirs` is not used and the compiler did not warn us. Does the directory already exist or did
something else go wrong?

**Nothing to lose**

You can adopt and later remove Safer without impacting your core code.
Leave it as a warning and enable errors per library as you go.
Use it alongside your other favorite linters, Safer is not aiming to replace any of them.

## Gradle Installation

Add the plugin to your project by including it in your `build.gradle.kts` file:

```kotlin
plugins {
    kotlin("multiplatform")
    id("com.swiftleap.safer") version "2.1.20-0.2-SNAPSHOT"  // Match with your Kotlin version
}

repositories {
    mavenCentral()
}

safer {
    // Configuration goes here (see Configuration section)
}
```

## Maven Installation

Add Safer to your kotlin maven build plugin: 

```xml
<plugin>
    <groupId>org.jetbrains.kotlin</groupId>
    <artifactId>kotlin-maven-plugin</artifactId>
    <version>${kotlin.version}</version>
    <!-- ... -->
    <configuration>
        <compilerPlugins>
            <plugin>com.swiftleap.safer</plugin>
        </compilerPlugins>
        <pluginOptions>
            <!-- see Safer Maven Configuration below -->
        </pluginOptions>
    </configuration>
    <dependencies>
        <dependency>
            <groupId>com.swiftleap</groupId>
            <artifactId>safer-maven-plugin</artifactId>
            <version>${kotlin.version}-${safer.version}</version>
        </dependency>
    </dependencies>
</plugin>
```

## Compatibility matrix

| Kotlin | Gradle | Maven | Safer               |
|--------|--------|-------|---------------------|
| 2.1.20 | 8.3 +  | 3+    | 2.1.20-0.3-SNAPSHOT |
| 2.1.0  | 8.3 +  | 3+    | 2.1.0-0.3-SNAPSHOT  |
| 2.0.21 | 8.3 +  | 3+    | 2.0.21-0.3-SNAPSHOT |
| 2.0.10 | 8.3 +  | 3+    | 2.0.10-0.3-SNAPSHOT |

**Note:** The above is a guideline, and it may work perfectly fine with earlier/later versions of Gradle and Maven.

## Usage

### Check Return Value

You can mark functions or classes whose return values must be used:

```kotlin
// Annotate a class - all functions returning this type must have their results used
@CheckReturnValue
class Result<T> {
    // ...
}

// Annotate a function - the return value of this function must be used
@Contract(pure=true)
fun computeValue(): Int {
    // ...
    return result
}
```

You can create your own annotations or use one provided by another library since the default checks are `*.@CheckReturnValue`, `*.@Contract(pure=true)`.

**Note:** that most providers do not mark their "pure" annotations with `@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)`. 
You can create your own with:
```kotlin
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class CheckReturnValue
```

### Unsafe Function Detection

When configured to do so, the plugin will warn you (or error) when you use functions that might throw exceptions:

```kotlin
// This will trigger a warning because get() might throw IndexOutOfBoundsException
val item = list[index]

// Better approach - use a safer explicit alternative
val item = list.getOrNull(index) ?: defaultValue
```

**Note:** warnings are only reported when there exists a safer alternative for an unsafe function.

## Gradle Configuration

Configure the plugin in your `build.gradle.kts` file:

```kotlin
safer {
    // Configure unused return value checking
    unused {
        // Enable or disable the feature (default: true)
        enabled = true

        // Treat warnings as errors (default: false)
        warnAsError = false

        // Check Kotlin standard library functions
        checkKotlinStdLib()

       // Check Kotlinx coroutine library functions
        checkKotlinCoroutines()

        // Check Java SDK functions (in a kotlin context)
        checkJavaExperimental()

        // Add custom signatures to check 
        // (or contribute to Safer and add checks for your libraries)
        checkSignatures(
            "java.io.File.mkdir()",
            "kotlin.Result",
            "arrow.core.Either",
            "java.util.Optional"
        )
    }

    // Configure unsafe function detection
    unsafe {
        // Enable or disable the feature (default: true)
        enabled = true

        // Treat warnings as errors (default: false)
        warnAsError = false

        // Check Kotlin standard library functions
        checkKotlinStdLib()

        // Check Kotlinx coroutine library functions
        checkKotlinCoroutines()

        // Check Java SDK functions (in a kotlin context)
        checkJavaExperimental()

        // Add custom signatures to check 
        // (or contribute to Safer and add checks for your libraries)
        checkSignatures(
            "kotlin.collections.max()",
            "java.util.Hashtable.get(*)",
        )
    }
}
```

## Maven Configuration

```xml
 <configuration>
     <compilerPlugins>
         <plugin>com.swiftleap.safer</plugin>
     </compilerPlugins>
     <pluginOptions>
         <option>com.swiftleap.safer:unusedEnabled=true</option>
         <option>com.swiftleap.safer:unusedWarnAsError=false</option>
         <option>com.swiftleap.safer:unusedPresetLibs="kotlin-stdlib; kotlin-coroutines; java</option>
         <!--
         Add custom signatures to check separated by a ';'. 
         (or contribute to Safer and add checks for your libraries as a preset lib) 
         -->
         <option>com.swiftleap.safer:unusedSignatures="kotlin.Result; java.util.Optional"</option>
         <option>com.swiftleap.safer:unsafeEnabled=true</option>
         <option>com.swiftleap.safer:unsafeWarnAsError=false</option>
         <!--
         Add custom signatures to check separated by a ';'. 
         (or contribute to Safer and add checks for your libraries as a preset lib)
         -->
         <option>com.swiftleap.safer:unsafeSignatures="kotlin.collections.max();java.util.Hashtable.get(*)"</option>
         <option>com.swiftleap.safer:unsafePresetLibs="kotlin-stdlib; kotlin-coroutines; java"</option>
     </pluginOptions>
 </configuration>
```

**Note:** An option may not contain a newline within, but surrounding whitespace is allowed. Your IDE may add new lines when formatting.

## Configuring Signatures

Signatures are used to specify which functions or types should be checked. The plugin supports several signature
formats:

### Signature Format

Signatures can be specified in the following formats:

1. **Class or Type**: `package.ClassName`
    - Example: `kotlin.Result`, `java.util.Optional`

2. **Annotation**: `*.@AnnotationName` or `package.@AnnotationName`
    - Example: `*.@CheckReturnValue`, `org.jetbrains.annotations.@Contract(pure=true)`

3. **Function**: `package.ClassName.functionName(package.Type1, package.Type2)`
    - Example: `kotlin.collections.List.get(Int)`, `kotlin.text.replaceFirst(*, *)`

**Note:** `int` is `Int` since this is a Kotlin compiler plugin. The default package for types are `kotlin.` and can be
omitted.

### Wildcard Support

- `*` can be used as a wildcard for package names: `*.ClassName`
- `*` can also be used as a wildcard for parameter types: `function(*, Int)`

### Examples

Here are some examples of valid signatures:

```text
# Type signatures
"kotlin.Result"
"java.util.Optional"
"java.util.OptionalInt"

# Annotation signatures
"*.@CheckReturnValue"
"*.@Contract(pure=true)"

# Function signatures
"kotlin.collections.List.get(Int)"
"kotlin.Array.get(Int)"
"kotlin.text.replaceFirst(*, *)"
```

Also, see the library checks [here.](safer-compiler-plugin/src/main/resources)  

## Building from Source

To build the plugin from source, clone the repository, then run any of the following:

* `./gradlew -P "safer.buildTool=gradle" :safer-compiler-plugin:test`
* `./gradlew -P "safer.buildTool=gradle" :gradle-dev-publish`
* `./gradlew -P "safer.buildTool=maven" :maven-dev-publish`

All tasks must be run with `-P "safer.buildTool=maven|gradle"`

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Disclaimer

Safer was used and tested on my own code base and is still Alpha, you may get false positives or your build may fail. 
These risks are minimal, and you can disable/remove Safer at any time without affecting your code; besides 
making it a little less "Safer".
