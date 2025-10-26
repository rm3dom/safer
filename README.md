[![Main](https://github.com/rm3dom/safer/actions/workflows/main.yml/badge.svg)](https://github.com/rm3dom/safer/actions/workflows/main.yml)


**Note: Safer CheckReturnValue now obsolete since kotlin 2.2.0**

Kotlin 2.2.0 introduced `@MustUseReturnValue` which is preferred over `@CheckReturnValue`, see: [KEEP-0412-unused-return-value-checker](https://github.com/Kotlin/KEEP/blob/main/proposals/KEEP-0412-unused-return-value-checker.md).

Enable it using free compiler args:

```kotlin
kotlin {
    compilerOptions {
        allWarningsAsErrors = true
        //Options: disable, check, full
        freeCompilerArgs.add("-Xreturn-value-checker=full")
    }
}
```
and by annotating your file with:

```kotlin
@file:MustUseReturnValue
```


# Safer - Kotlin Compiler Plugin

Safer is essentially a 'mass deprecation' tool. It checks your code for unsafe functions and warns you when you use them. 
An "unsafe" function is typically a function that can throw exceptions where there exists a safer alternative, for example:
```kotlin
// This will trigger a warning because get() might throw IndexOutOfBoundsException
val item = list[index]
// Better approach - use a safer explicit alternative
val item = list.getOrNull(index) ?: defaultValue
```

You can configure Safer to report on any functions by specifying their signatures. See the Configuration section for more details.


## Gradle Installation

Add the plugin to your project by including it in your `build.gradle.kts` file:

```kotlin
plugins {
    kotlin("multiplatform")
    id("com.swiftleap.safer") version "2.2.20-0.3.2"  // Match with your Kotlin version
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

| Kotlin | Gradle | Maven | Safer        |
|--------|--------|-------|--------------|
| 2.2.20 | 8.3 +  | 3+    | 2.2.20-0.3.3 |
| 2.2.0  | 8.3 +  | 3+    | 2.2.0-0.3.2  |
| 2.1.20 | 8.3 +  | 3+    | 2.1.20-0.3.1 |
| 2.1.0  | 8.3 +  | 3+    | 2.1.0-0.3.1  |
| 2.0.21 | 8.3 +  | 3+    | 2.0.21-0.3.1 |
| 2.0.10 | 8.3 +  | 3+    | 2.0.10-0.3.1 |

**Note:** The above is a guideline, and it may work perfectly fine with earlier/later versions of Gradle and Maven.

## Usage

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

**Function**: `package.ClassName.functionName(package.Type1, package.Type2)`
    - Example: `kotlin.collections.List.get(Int)`, `kotlin.text.replaceFirst(*, *)`

**Note:** `int` is `Int` since this is a Kotlin compiler plugin. The default package for types are `kotlin.` and can be
omitted.

### Wildcard Support

- `*` can be used as a wildcard for package names: `*.ClassName`
- `*` can also be used as a wildcard for parameter types: `function(*, Int)`

### Examples

Here are some examples of valid signatures:

```text
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
