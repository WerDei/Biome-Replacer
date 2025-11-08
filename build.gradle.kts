import org.gradle.internal.extensions.stdlib.capitalized

plugins {
	id("dev.architectury.loom") version "1.13.+"
	id("me.modmuss50.mod-publish-plugin") version "0.8.4"
}

class ModData {
	val id = property("mod.id").toString()
	val name = property("mod.name")
	val version = property("mod.version")
	val versionWithCodename = "${property("mod.version")}-${property("mod.mc_codename")}"
	val group = property("mod.group").toString()
	val description = property("mod.description")
	val source = property("mod.source")
	val issues = property("mod.issues")
	val license = property("mod.license").toString()
	val modrinth = property("mod.modrinth")
}

class Dependencies {
	val modmenuVersion = property("deps.modmenu_version")
	val modmenuEnabled = modmenuVersion != "[VERSIONED]"
	val fapiVersion = property("deps.fabric_api")
	val mixinsquaredVersion = property("deps.mixinsquared")
	val mixinExtrasVersion = property("deps.mixinextras")
	val terrablenderEnabled = property("deps.terrablender_enabled").toString().toBoolean()
}

class LoaderData {
	val loader = loom.platform.get().name.lowercase()
	val loaderShort = if (loader == "neoforge") "neo" else loader
	val isFabric = loader == "fabric"
	val isNeoforge = loader == "neoforge"
	val isOldforge = loader == "forge"
	val isForgeLike = isNeoforge || isOldforge
}

class McData {
	val version = stonecutter.current.version
	val dep = property("mod.mc_dep")
	val targets = property("mod.mc_targets").toString().split(", ")
}

val mc = McData()
val mod = ModData()
val deps = Dependencies()
val loader = LoaderData()

version = "${mod.versionWithCodename}-${loader.loaderShort}"
group = mod.group
base { archivesName.set(mod.id.replace("_", "")) }

stonecutter {
	constants["fabric"] = loader.isFabric
	constants["neoforge"] = loader.isNeoforge
	constants["oldforge"] = loader.isOldforge
	constants["forge-like"] = loader.isForgeLike
}

loom {
	silentMojangMappingsLicense()

	runConfigs.all {
		ideConfigGenerated(false)
		runDir = "../../run"
	}
	if (loader.isOldforge)
		forge.mixinConfig("biome_replacer.mixins.json")
}

repositories {
	maven("https://maven.parchmentmc.org") // Parchment
	maven("https://maven.terraformersmc.com") // Mod Menu, Biolith
	maven("https://maven.nucleoid.xyz/") { name = "Nucleoid" } // Placeholder API - required by Mod Menu
	maven("https://maven.neoforged.net/releases") // NeoForge
	maven("https://maven.minecraftforge.net/") // TerraBlender
	maven("https://maven.bawnorton.com/releases") // MixinSquared
}

dependencies {
	minecraft("com.mojang:minecraft:${mc.version}")

	@Suppress("UnstableApiUsage")
	mappings(loom.layered {
		// Mojmap mappings
		officialMojangMappings()

		// Parchment mappings (it adds parameter mappings & javadoc)
		optionalProp("deps.parchment_version") {
			if (mc.version == "1.20.1" && loader.isOldforge) {
				// Skip Parchment for 1.20.1 Forge to avoid obfuscation mapping conflicts
			} else {
				parchment("org.parchmentmc.data:parchment-${mc.version}:$it@zip")
			}
		}
	})

	// Jank for optional TerraBlender integration
	optionalProp("deps.terrablender") {
		val terraBlender = "com.github.glitchfiend:TerraBlender-${loader.loader}:${mc.version}-$it"
		if (deps.terrablenderEnabled)
			modImplementation(terraBlender)
		else
			modCompileOnly(terraBlender) // API still needs to be present for compilation, but mod won't be in the running game
	}

	when {
		loader.isFabric -> {
			modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
            include(implementation(annotationProcessor("com.github.bawnorton.mixinsquared:mixinsquared-fabric:${deps.mixinsquaredVersion}")!!)!!)
            modRuntimeOnly("net.fabricmc.fabric-api:fabric-api:${deps.fapiVersion}")
            if (deps.modmenuEnabled) modRuntimeOnly("com.terraformersmc:modmenu:${deps.modmenuVersion}")
		}
		loader.isNeoforge -> {
			"neoForge"("net.neoforged:neoforge:${findProperty("deps.neoforge")}")
			compileOnly(annotationProcessor("com.github.bawnorton.mixinsquared:mixinsquared-common:${deps.mixinsquaredVersion}")!!)
			implementation(include("com.github.bawnorton.mixinsquared:mixinsquared-neoforge:${deps.mixinsquaredVersion}")!!)
		}
		loader.isOldforge -> {
			"forge"("net.minecraftforge:forge:${mc.version}-${findProperty("deps.forge")}")
			compileOnly(annotationProcessor("io.github.llamalad7:mixinextras-common:${deps.mixinExtrasVersion}")!!)
			implementation(include("io.github.llamalad7:mixinextras-forge:${deps.mixinExtrasVersion}")!!)
			compileOnly(annotationProcessor("com.github.bawnorton.mixinsquared:mixinsquared-common:${deps.mixinsquaredVersion}")!!)
			implementation(include("com.github.bawnorton.mixinsquared:mixinsquared-forge:${deps.mixinsquaredVersion}")!!)
		}
		else -> {
			throw GradleException("Unknown loader: ${loader.loader}")
		}
	}
}

java {
	// I want codename Elephant builds on fabric to load before 1.20.5 too, so it's built for java 17
	val java = if (stonecutter.current.version < "1.20.5"
		|| (stonecutter.current.version == "1.20.6" && loader.isFabric))
		JavaVersion.VERSION_17 else JavaVersion.VERSION_21
	sourceCompatibility = java
	targetCompatibility = java
}

tasks.processResources {
	val props = buildMap {
		put("id", mod.id)
		put("name", mod.name)
		put("version", mod.versionWithCodename)
		put("minecraft", mc.dep)
		put("description", mod.description)
		put("source", mod.source)
		put("issues", mod.issues)
		put("license", mod.license)
		put("modrinth", mod.modrinth)

		if (loader.isForgeLike) {
			put("forgeConstraint", findProperty("modstoml.forge_constraint"))
		}
	}

	props.forEach(inputs::property)

	if (loader.isFabric)
	{
		filesMatching("fabric.mod.json") { expand(props) }
		exclude(listOf("META-INF/mods.toml", "META-INF/neoforge.mods.toml", "pack.mcmeta", "logo.png"))
	}
	else if (loader.isNeoforge)
	{
		filesMatching("META-INF/neoforge.mods.toml") { expand(props) }
		exclude("fabric.mod.json", "META-INF/mods.toml", "pack.mcmeta", "icon.png")
	}
	else if (loader.isOldforge)
	{
		filesMatching("META-INF/mods.toml") { expand(props) }
		filesMatching("pack.mcmeta") { expand(props) }
		exclude("fabric.mod.json", "META-INF/neoforge.mods.toml", "icon.png")
	}
}

publishMods {
	file = project.tasks.remapJar.get().archiveFile
	displayName = "${mod.version} ${loader.loader.capitalized()} ${mc.targets.first()}-${mc.targets.last()}"
	changelog = rootProject.file("CHANGELOG.md").readText()
	type = BETA

	modLoaders.add(loader.loader)

	modrinth {
		projectId = property("publish.modrinth").toString()
		accessToken = findProperty("modrinth_token").toString()
		mc.targets.forEach(minecraftVersions::add)
		// Hm, project description is changed with every published version. So 6+ times in a row. Hopefully this isn't an issue?
		projectDescription = rootProject.file("README.md").readText()
	}

	curseforge {
		projectId = property("publish.curseforge").toString()
		accessToken = findProperty("curseforge_token").toString()
		mc.targets.forEach(minecraftVersions::add)
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

@Suppress("TYPE_MISMATCH", "UNRESOLVED_REFERENCE")
fun <T> optionalProp(property: String, block: (String) -> T?): T? =
	findProperty(property)?.toString()?.takeUnless { it.isBlank() }?.let(block)