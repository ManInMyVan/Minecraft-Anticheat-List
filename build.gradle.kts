import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.*
import de.marhali.json5.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.jimblackler.jsonschemafriend.*
import java.lang.reflect.Field
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.text.DateFormat
import java.time.Duration
import java.time.Instant
import java.util.Collections
import java.util.concurrent.atomic.AtomicBoolean

buildscript {
    repositories {
        mavenCentral()
        maven("https://jitpack.io")
    }
    dependencies {
        classpath("de.marhali:json5-java:3.0.0")
        classpath("com.google.code.gson:gson:2.14.0")
        classpath("net.jimblackler.jsonschemafriend:core:0.12.5")
    }
}

plugins {
    kotlin("multiplatform") version "2.4.20"
}

repositories {
    mavenCentral()
}

dependencies {
    commonMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.11.0")
}

kotlin {
    js {
        browser {}
        binaries.executable()
    }

    sourceSets {
        getByName("jsMain") {
            resources.exclude("**/anticheats/**", "**/schema.json")
        }
    }
}

val objectMapper: ObjectMapper = ObjectMapper()
val json5: Json5 = Json5.builder { it.build() }
val gson: Gson = GsonBuilder()
    .create()
val validator = Validator()
val fourMonthsAgo: Instant = Instant.now() - Duration.ofDays(4 * 30)
val httpClient: HttpClient = HttpClient.newBuilder()
    .followRedirects(HttpClient.Redirect.NORMAL)
    .connectTimeout(Duration.ofSeconds(30))
    .build()

tasks.register("compileAnticheats") {
    description = "compiles anticheats.json"
    dependsOn(tasks.assemble)
    group = "build"

    val input = layout.projectDirectory.dir("src/jsMain/resources/anticheats")
    val output = layout.projectDirectory.file("build/dist/js/productionExecutable/anticheats.json")

    inputs.dir(input)
    outputs.file(output)

    doLast {
        val schema = SchemaStore().loadSchemaJson(File("src/jsMain/resources/schema.json").readText(Charsets.UTF_8))
        val files = input.asFile.listFiles().orEmpty()
        val anticheats = Collections.synchronizedList(mutableListOf<JsonObject>())

        runBlocking {
            for (file in files) launch {
                val anticheat = handleAnticheatFile(file, schema)
                if (anticheat != null) {
                    anticheats.add(anticheat)
                }
            }
        }

        val out = JsonArray(anticheats.size)
        for (anticheat in anticheats) out.add(anticheat)
        output.asFile.writeBytes(gson.toJson(out).toByteArray(Charsets.UTF_8))
    }
}.also {
    tasks.build.get().dependsOn(it)
}

fun handleAnticheatFile(file: File, schema: Schema): JsonObject? {
    val reader = file.reader(Charsets.UTF_8)
    val obj = when (file.extension) {
        "json" -> gson.fromJson(reader, JsonObject::class.java)
        "json5" -> gson.fromJson(json5.serialize(json5.parse(reader)), JsonObject::class.java)
        else -> {
            logger.error("non-json file in anticheats directory: " + file.name)
            return null
        }
    }

    var invalid = false
    validator.validate(schema, objectMapper.readValue(obj.toString(), Any::class.java)) { error ->
        val value = objectMapper.writeValueAsString(error.`object`)
        val message = format(error, value, error.uri.toString())
        logger.error("${file.name}: $message")
        invalid = true
    }

    if (invalid) return null

    val platform = obj.get("platform")
    if (platform == null || platform is JsonNull) {
        obj.add("platform", JsonArray().also { it.add("Unknown") })
    } else if (platform as? JsonPrimitive != null && platform.isString) {
        obj.add("platform", JsonArray().also { it.add(platform.asString) })
    }

    val status = obj.get("status")
    if (status == null || status is JsonNull) {
        obj.addProperty("status", getStatus(obj))
    }

    obj.addProperty("name", file.nameWithoutExtension)
    return obj
}

val githubHasRateLimited = AtomicBoolean(false)
fun getStatus(obj: JsonObject): String? {
    val spigot = (obj.get("spigot") as? JsonPrimitive)?.let {
        if (it.isNumber) it.asInt else null
    }

    val github = (obj.get("github") as? JsonPrimitive)?.let {
        if (it.isString) it.asString else null
    }

    if (spigot == null && github == null) {
        return "Unknown"
    }

    var spigot404 = false
    val spigotUpdateDate = spigot?.let {
        val response = get("https://api.spiget.org/v2/resources/$spigot")
        when (response.statusCode()) {
            404 -> {
                spigot404 = true
                null
            }
            else -> {
                response.getJson().let { it as? JsonObject }
                    ?.let { it.get("updateDate") as? JsonPrimitive }
                    ?.let { if (it.isNumber) it.asInt else null }
            }
        }
    }

    if (spigotUpdateDate != null && fourMonthsAgo.epochSecond < spigotUpdateDate) {
        return "Active"
    }

    if (github != null) {
        val response = get("https://api.github.com/repos/$github")
        when (response.statusCode()) {
            403 -> {
                if (!githubHasRateLimited.getAndSet(true)) {
                    logger.warn("You have been rate limited by github!")
                }
            }
            404 -> if (spigotUpdateDate == null) return "Unavailable"
            else -> {
                val data = response.getJson().let { it as? JsonObject }
                if (data != null) {
                    val private = data.get("private").let { it as? JsonPrimitive }
                        ?.let { if (it.isBoolean) it.asBoolean else null }

                    if (private == true && spigotUpdateDate == null) {
                        return "Unavailable"
                    }

                    val archived = data.get("archived").let { it as? JsonPrimitive }
                        ?.let { if (it.isBoolean) it.asBoolean else null }

                    if (archived == true) {
                        return "Discontinued"
                    }

                    val pushedAt = data.get("pushed_at").let { it as? JsonPrimitive }
                        ?.let { if (it.isString) it.asString else null }

                    if (pushedAt != null) {
                        if (fourMonthsAgo.isBefore(Instant.parse(pushedAt))) {
                            return "Active"
                        }
                    }
                }
            }
        }
    }

    if (spigotUpdateDate != null) {
        return "Old"
    }

    if (spigot404) {
        return "Unavailable"
    }

    return null
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

fun get(url: String): HttpResponse<String> = httpClient.send(
    HttpRequest.newBuilder(URI.create(url))
        .GET()
        .build(),
    HttpResponse.BodyHandlers.ofString()
)
fun HttpResponse<String>.getJson(): JsonElement = gson.fromJson(body(), JsonElement::class.java)
fun getJson(url: String): JsonElement = get(url).getJson()
