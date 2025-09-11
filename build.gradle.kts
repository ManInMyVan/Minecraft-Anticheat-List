import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.*
import de.marhali.json5.*
import net.jimblackler.jsonschemafriend.*
import java.lang.reflect.Field

buildscript {
    repositories {
        mavenCentral()
        maven("https://jitpack.io")
    }
    dependencies {
        classpath("de.marhali:json5-java:2.0.0")
        classpath("com.google.code.gson:gson:2.13.1")
        classpath("net.jimblackler.jsonschemafriend:core:0.12.5")
    }
}

plugins {
    kotlin("multiplatform") version "2.2.20"
}

repositories {
    mavenCentral()
}

dependencies {
    commonMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.10.2")
}

kotlin {
    js {
        browser {}
        binaries.executable()
    }

    sourceSets {
        val jsMain by getting {
            resources.exclude("**/anticheats/**", "**/schema.json")
        }
    }
}

val objectMapper = ObjectMapper()

tasks.register("compileAnticheats") {
    dependsOn(tasks.assemble)
    group = "build"

    val input = layout.projectDirectory.dir("src/jsMain/resources/anticheats")
    val output = layout.projectDirectory.file("build/dist/js/productionExecutable/anticheats.json")

    inputs.dir(input)
    outputs.file(output)

    doLast {
        val json5 = Json5.builder { it.build() }
        val gson = GsonBuilder().serializeNulls().setPrettyPrinting().create()
        val schema = SchemaStore().loadSchemaJson(File("src/jsMain/resources/schema.json").readText(Charsets.UTF_8))
        val validator = Validator()
        val out = JsonArray()
        val errors = mutableListOf<String>()
        input.asFile.listFiles()?.forEach { file ->
            when (val extension = file.extension) {
                "json5", "json" -> {
                    val reader = file.reader(Charsets.UTF_8)
                    val obj = if (extension == "json5") {
                        gson.fromJson(json5.serialize(json5.parse(reader)), JsonObject::class.java)
                    } else {
                        gson.fromJson(reader, JsonObject::class.java)
                    }

                    var invalid = false
                    validator.validate(schema, objectMapper.readValue(obj.toString(), Object::class.java)) { error ->
                        val value = objectMapper.writeValueAsString(error.`object`)
                        val message = format(error, value, error.uri.toString())
                        errors.add("${file.name}: $message")
                        invalid = true
                    }
                    if (invalid) return@forEach

                    if (obj.get("platforms") == JsonNull.INSTANCE) {
                        obj.add("platforms", JsonArray().also { it.add("Unknown") })
                    }

                    obj.addProperty("name", file.nameWithoutExtension)
                    out.add(obj)
                }
                else -> error("non-json file in anticheats directory: " + file.name)
            }
        }

        if (errors.isNotEmpty()) {
            error(errors.joinToString("\n"))
        }

        output.asFile.writeBytes(gson.toJson(out).toByteArray(Charsets.UTF_8))
    }
}.also {
    tasks.build.get().dependsOn(it)
}

@Suppress("PropertyName")
val `FormatError#reason`: Field = FormatError::class.java.getDeclaredField("reason").also { it.isAccessible = true }
fun format(error: ValidationError, value: String, path: String): String = when (error) {
    is ConstError -> "Value $value (at $path) must be " + objectMapper.writeValueAsString(error.schema.const)
    is EnumError -> "Value $value (at $path) must be one of " + error.schema.enums.map { objectMapper.writeValueAsString(it) }
    is DivisibleByError -> "Value $value (at $path) must be divisible by " + error.schema.divisibleBy
    is ExclusiveMaximumError -> "Value $value (at $path) must be less than " + error.schema.exclusiveMaximum
    is ExclusiveMinimumError -> "Value $value (at $path) must be greater than " + error.schema.exclusiveMinimum
    is FormatError -> "Value $value (at $path) must comply with format " + error.schema.format + " (" + `FormatError#reason`.get(error) + ")"
    is MaxContainsError -> "Value $value (at $path) may not have more than " + error.schema.maxContains + " element${if (error.schema.maxContains == 1) "" else "s"} matching " + objectMapper.writeValueAsString(error.schema.contains.schemaObject)
    is MinContainsError -> "Value $value (at $path) may not have less than " + error.schema.minContains + " element${if (error.schema.minContains == 1) "" else "s"} matching " + objectMapper.writeValueAsString(error.schema.contains.schemaObject)
    is MaxItemsError -> "Value $value (at $path) may not have more than " + error.schema.maxItems + " item${if (error.schema.maxItems == 1) "" else "s"}"
    is MinItemsError -> "Value $value (at $path) may not have less than " + error.schema.minItems + " item${if (error.schema.minItems == 1) "" else "s"}"
    is MaxLengthError -> "Value $value (at $path) may not be longer than " + error.schema.maxLength + " character${if (error.schema.maxLength == 1) "" else "s"}"
    is MinLengthError -> "Value $value (at $path) may not be shorter than " + error.schema.minLength + " character${if (error.schema.minLength == 1) "" else "s"}"
    is MaxPropertiesError -> "Value $value (at $path) may not have more than " + error.schema.maxProperties + " propert${if (error.schema.maxProperties == 1) "y" else "ies"}"
    is MinPropertiesError -> "Value $value (at $path) may not have more than " + error.schema.minProperties + " propert${if (error.schema.minProperties == 1) "y" else "ies"}"
    is MaximumError -> "Value $value (at $path) " + (if (error.schema.isExclusiveMaximumBoolean) "must be less than " else "may not be more than ") + error.schema.maximum
    is MinimumError -> "Value $value (at $path) " + (if (error.schema.isExclusiveMinimumBoolean) "must be more than " else "may not be less than ") + error.schema.minimum
    is MissingPropertyError -> "Value at $path must have property \"" + error.property + "\""
    is TypeError -> "Value $value (at $path) must be of type " + error.expectedTypes.joinToString(" or ") + " (found " + error.foundTypes.joinToString(" or ") + ")"
    else -> "Value $value (at $path): " + error.message
}
