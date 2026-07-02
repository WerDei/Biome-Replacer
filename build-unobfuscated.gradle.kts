import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.neoforged.moddevgradle.dsl.NeoForgeExtension
import org.gradle.api.tasks.bundling.Jar

plugins {
	id("net.fabricmc.fabric-loom") version "1.17.0-alpha.19" apply false
	id("net.neoforged.moddev") version "2.0.141" apply false
	id("me.modmuss50.mod-publish-plugin") version "1.1.0"
}

val loader = property("loom.platform").toString()
val isFabric = loader == "fabric"
val isNeoforge = loader == "neoforge"
val mcVersion = stonecutter.current.version
val modId = property("mod.id").toString()
val modName = property("mod.name")
val modVersion = property("mod.version").toString()
val modCodename = property("mod.mc_codename").toString()
val modDescription = property("mod.description")
val modSource = property("mod.source")
val modIssues = property("mod.issues")
val modLicense = property("mod.license")
val modrinthId = property("mod.modrinth")
val mcDependency = property("mod.mc_dep")
val fabricLoader = property("deps.fabric_loader")
val mcTargets = property("mod.mc_targets").toString().split(", ")
val mixinSquared = property("deps.mixinsquared")

when {
	isFabric -> apply(plugin = "net.fabricmc.fabric-loom")
	isNeoforge -> apply(plugin = "net.neoforged.moddev")
	else -> throw GradleException("Unknown loader: $loader")
}

version = "$modVersion-$modCodename-${if (isNeoforge) "neo" else loader}"
group = property("mod.group").toString()
base { archivesName.set(modId.replace("_", "")) }

stonecutter {
	constants["fabric"] = isFabric
	constants["neoforge"] = isNeoforge
	constants["oldforge"] = false
	constants["forge-like"] = isNeoforge
	constants["unobfuscated"] = true
	constants["biolith"] = findProperty("deps.biolith_version") != null
	constants["lithostitched"] = findProperty("deps.lithostitched_version") != null

	replacements.string(current.parsed >= "1.21.11") {
		replace("ResourceLocation", "Identifier")
		replace(".location()", ".identifier()")
	}
}

repositories {
	maven("https://maven.terraformersmc.com")
	maven("https://maven.nucleoid.xyz/")
	maven("https://maven.neoforged.net/releases")
	maven("https://maven.minecraftforge.net/")
	maven("https://maven.bawnorton.com/releases")
	maven("https://api.modrinth.com/maven")
}

if (isFabric) {
	extensions.configure<LoomGradleExtensionAPI>("loom")
	{
		runConfigs.all {
			ideConfigGenerated(false)
			runDir = "../../run"
		}

		mods {
			register(modId) {
				sourceSet(sourceSets.main.get())
			}
		}
	}
} else {
	extensions.configure<NeoForgeExtension>("neoForge") {
		setVersion(property("deps.neoforge").toString())
		runs {
			register("client") {
				client()
				gameDirectory.set(rootProject.layout.projectDirectory.dir("run"))
			}
			register("server") {
				server()
				gameDirectory.set(rootProject.layout.projectDirectory.dir("run"))
			}
		}
		mods {
			register(modId) {
				sourceSet(sourceSets.main.get())
			}
		}
	}
}

dependencies {
	if (isFabric) {
		add("minecraft", "com.mojang:minecraft:$mcVersion")
		add("implementation", "net.fabricmc:fabric-loader:$fabricLoader")
		add("implementation", "net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")
		add("annotationProcessor", "com.github.bawnorton.mixinsquared:mixinsquared-fabric:$mixinSquared")
		add("implementation", "com.github.bawnorton.mixinsquared:mixinsquared-fabric:$mixinSquared")
		add("include", "com.github.bawnorton.mixinsquared:mixinsquared-fabric:$mixinSquared")

		if (property("deps.modmenu_version") != "[VERSIONED]")
			add("runtimeOnly", "com.terraformersmc:modmenu:${property("deps.modmenu_version")}")
	} else {
		add("annotationProcessor", "com.github.bawnorton.mixinsquared:mixinsquared-common:$mixinSquared")
		add("compileOnly", "com.github.bawnorton.mixinsquared:mixinsquared-common:$mixinSquared")
		add("implementation", "com.github.bawnorton.mixinsquared:mixinsquared-neoforge:$mixinSquared")
		add("jarJar", "com.github.bawnorton.mixinsquared:mixinsquared-neoforge:$mixinSquared")
	}

	optionalProp("deps.terrablender") {
		val terraBlender = "com.github.glitchfiend:TerraBlender-$loader:$mcVersion-$it"
		add(if (property("deps.terrablender_enabled").toString().toBoolean()) "implementation" else "compileOnly", terraBlender)
	}

	optionalProp("deps.biolith_version") {
		add(if (property("deps.biolith_enabled").toString().toBoolean()) "implementation" else "compileOnly", "com.terraformersmc:biolith-$loader:$it")
	}

	optionalProp("deps.lithostitched_version") {
		add("compileOnly", "maven.modrinth:lithostitched:$it")
	}
}

java {
	toolchain.languageVersion = JavaLanguageVersion.of(25)
	sourceCompatibility = JavaVersion.VERSION_25
	targetCompatibility = JavaVersion.VERSION_25
}

tasks.withType<JavaCompile>().configureEach {
	options.release.set(25)
}

tasks.processResources {
	val props = mutableMapOf<String, Any?>(
		"id" to modId,
		"name" to modName,
		"version" to "$modVersion-$modCodename",
		"minecraft" to mcDependency,
		"fabricLoader" to ">=$fabricLoader",
		"description" to modDescription,
		"source" to modSource,
		"issues" to modIssues,
		"license" to modLicense,
		"modrinth" to modrinthId
	)
	if (isNeoforge)
		props["forgeConstraint"] = findProperty("modstoml.forge_constraint")

	inputs.properties(props)
	if (isFabric) {
		filesMatching("fabric.mod.json") { expand(props) }
		exclude("META-INF/mods.toml", "META-INF/neoforge.mods.toml", "pack.mcmeta", "logo.png")
	} else {
		filesMatching("META-INF/neoforge.mods.toml") { expand(props) }
		exclude("fabric.mod.json", "META-INF/mods.toml", "pack.mcmeta", "icon.png")
	}
}

publishMods {
	file = tasks.named<Jar>("jar").get().archiveFile
	var displayMcVersion = if (mcTargets.count() > 1) "${mcTargets.first()}-${mcTargets.last()}" else mcTargets.first();
	displayName = "$modVersion ${loader.replaceFirstChar { it.titlecase() }} $displayMcVersion"
	changelog = rootProject.file("CHANGELOG.md").readText()
	type = BETA
	modLoaders.add(loader)

	modrinth {
		projectId = project.property("publish.modrinth").toString()
		accessToken = findProperty("modrinth_token").toString()
		mcTargets.forEach(minecraftVersions::add)
		projectDescription = rootProject.file("README.md").readText()
	}

	curseforge {
		projectId = project.property("publish.curseforge").toString()
		accessToken = findProperty("curseforge_token").toString()
		mcTargets.forEach(minecraftVersions::add)
	}
}


if (stonecutter.current.isActive) {
	rootProject.tasks.register("Build active project") {
		group = "stonecutter"
		dependsOn(tasks.named("build"))
	}
	rootProject.tasks.register("Run active Client") {
		group = "stonecutter"
		dependsOn(tasks.named("runClient"))
	}
	rootProject.tasks.register("Run active Server") {
		group = "stonecutter"
		dependsOn(tasks.named("runServer"))
	}
}

fun <T> optionalProp(property: String, block: (String) -> T?): T? =
	findProperty(property)?.toString()?.takeUnless { it.isBlank() }?.let(block)
