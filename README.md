# EzHook
An AOP framework for KotlinMultiplatform, supporting Kotlin/Native and Kotlin/JS.
## Project Configuration
EzHook consists of two main components: a Gradle plugin and a runtime library. The Gradle plugin handles compile-time data collection and IR transformation, while the library provides essential annotations and runtime support.
To enable EzHook, add the Gradle plugin in your top-level project configuration:
```kotlin
// Root-level build.gradle.kts
buildscript {
    dependencies {
        classpath("io.github.xdmrwu:ez-hook-gradle-plugin:1.0.0")
    }
}
// Apply the EzHook plugin in your top-level module
plugins {
    id("io.github.xdmrwu.ez-hook-gradle-plugin")
}
```
Then add the EzHook runtime library to any module where you want to use its features:
```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.xdmrwu:ez-hook-library:1.0.0")
        }
    }
}
```
To ensure EzHook works correctly, you need to disable Kotlin/Native caching by adding the following line to your gradle.properties file:
```kotlin
// gradle.properties
kotlin.native.cacheKind=none
```
## Usage
EzHook is an AOP framework similar to [Lancet](https://github.com/eleme/lancet). It allows you to replace any method at compile time with a specified custom method. 
To use EzHook, create a hook method and annotate it with `@EzHook`, specifying the target method’s fully qualified name.

**Example:**
```kotlin
@HiddenFromObjC
@EzHook("kotlin.time.Duration.toInt")
fun toInt(unit: DurationUnit): Int {
    println("Hook to int")
    return 10086
}
```
In this example, the `toInt` method of the Duration class will be replaced by the custom `toInt` method.

**Key Considerations:**
- The `@EzHook` annotation takes the fully qualified name (FQN) of the target method. It also supports top-level functions.
- The name of the hook method can be arbitrary, but the number, types of parameters, and the return type must match the target method.
- Hook methods must be top-level functions.
- If the target platform includes iOS, the `@HiddenFromObjC` annotation needs to be added.
### Calling the Original Method
EzHook allows you to invoke the original method and modify its parameters within the hook. 
Here’s how to do it:
1.	Create a variable with the same name and type as the original parameter to override its value.
2.	Use `callOrigin<T>()` to invoke the original method.

**Example:**
```kotlin
@EzHook("kotlin.time.Duration.toInt")
fun toInt(unit: DurationUnit): Int {
    val unit = DurationUnit.HOURS
    return callOrigin<Int>()
}
```
### Inline Hook Functions for Kotlin/JS
In Kotlin/JS, circular dependencies between modules can easily cause runtime crashes.
To prevent this, you can inline the hook function directly into the target module. This ensures the hook logic is part of the module, reducing the risk of dependency-related issues.

Here’s how to configure inlining for Kotlin/JS:
```kotlin
@EzHook("kotlin.time.Duration.toInt", true)
fun toInt(unit: DurationUnit): Int {
    val unit = DurationUnit.HOURS
    return callOrigin<Int>()
}
```
### Limitations
- Only supports Kotlin/Native and Kotlin/JS targets
- Extension functions and constructors are not supported
- Currently compatible with Kotlin version 2.0.21 only
