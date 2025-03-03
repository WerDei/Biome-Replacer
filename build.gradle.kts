plugins {
	id("dev.architectury.loom") version "1.7.+"
}

class ModData {
	val id = property("mod.id").toString()
	val name = property("mod.name")
	val version = property("mod.version")
	val group = property("mod.group").toString()
	val description = property("mod.description")
	val source = property("mod.source")
	val issues = property("mod.issues")
	val license = property("mod.license").toString()
	val modrinth = property("mod.modrinth")
}

class Dependencies {
	val modmenuVersion = property("deps.modmenu_version")
	val yaclVersion = property("deps.yacl_version")
	val devauthVersion = property("deps.devauth_version")
	val fapiVersion = property("deps.fabric_api")
}

class LoaderData {
	val loader = loom.platform.get().name.lowercase()
	val isFabric = loader == "fabric"
	val isNeoforge = loader == "neoforge"
}

class McData {
	val version = property("mod.mc_version")
	val dep = property("mod.mc_dep")
	val targets = property("mod.mc_targets").toString().split(", ")
	val codename = property("mod.mc_codename")
}

val mc = McData()
val mod = ModData()
val deps = Dependencies()
val loader = LoaderData()

version = "${mod.version}-${mc.codename}-${loader.loader}"
group = mod.group
base { archivesName.set(mod.id.replace("_", "")) }

stonecutter.const("fabric", loader.isFabric)
stonecutter.const("neoforge", loader.isNeoforge)

loom {
	silentMojangMappingsLicense()

	runConfigs.all {
		ideConfigGenerated(stonecutter.current.isActive)
		runDir = "../../run"
	}

	runConfigs.remove(runConfigs["server"])
}

repositories {
	maven("https://maven.parchmentmc.org") // Parchment
//	maven("https://maven.isxander.dev/releases") // YACL
//	maven("https://thedarkcolour.github.io/KotlinForForge") // Kotlin for Forge - required by YACL
	maven("https://maven.terraformersmc.com") // Mod Menu
	maven("https://maven.nucleoid.xyz/") { name = "Nucleoid" } // Placeholder API - required by Mod Menu
	maven("https://maven.neoforged.net/releases") // NeoForge
	maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1") // DevAuth
}

dependencies {
	minecraft("com.mojang:minecraft:${mc.version}")

	@Suppress("UnstableApiUsage")
	mappings(loom.layered {
		// Mojmap mappings
		officialMojangMappings()

		// Parchment mappings (it adds parameter mappings & javadoc)
		optionalProp("deps.parchment_version") {
			parchment("org.parchmentmc.data:parchment-${property("mod.mc_version")}:$it@zip")
		}
	})

	modRuntimeOnly("me.djtheredstoner:DevAuth-${loader.loader}:${deps.devauthVersion}")

	if (loader.isFabric)
	{
		modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
		modImplementation("net.fabricmc.fabric-api:fabric-api:${deps.fapiVersion}")
		modImplementation("com.terraformersmc:modmenu:${deps.modmenuVersion}")
//		if (mc.version == "1.21.3") modImplementation("dev.isxander:yet-another-config-lib:${deps.yaclVersion}+1.21.2-${loader.loader}")
//		else if (mc.version == "1.21.1") modImplementation("dev.isxander:yet-another-config-lib:${deps.yaclVersion}+1.21-${loader.loader}")
//		else modImplementation("dev.isxander:yet-another-config-lib:${deps.yaclVersion}+${mc.version}-${loader.loader}")
	}
	else if (loader.isNeoforge)
	{
		"neoForge"("net.neoforged:neoforge:${findProperty("deps.neoforge")}")
//		if (mc.version == "1.21.3") implementation("dev.isxander:yet-another-config-lib:${deps.yaclVersion}+1.21.2-${loader.loader}") {isTransitive = false}
//		else if (mc.version == "1.21.1") implementation("dev.isxander:yet-another-config-lib:${deps.yaclVersion}+1.21-${loader.loader}") {isTransitive = false}
//		else implementation("dev.isxander:yet-another-config-lib:${deps.yaclVersion}+1.21.2-${loader.loader}") {isTransitive = false}
	}
}

java {
	val java = if (stonecutter.compare(
			stonecutter.current.version,
			"1.21" // Not 1.20.5 because I want Elephant builds to load on more versions (which means using java 17)
		) >= 0
	) JavaVersion.VERSION_21 else JavaVersion.VERSION_17
	sourceCompatibility = java
	targetCompatibility = java
}

tasks.processResources {
	val props = buildMap {
		put("id", mod.id)
		put("name", mod.name)
		put("version", mod.version)
		put("minecraft", mc.dep)
		put("description", mod.description)
		put("source", mod.source)
		put("issues", mod.issues)
		put("license", mod.license)
		put("modrinth", mod.modrinth)

		if (loader.isNeoforge) {
			put("forgeConstraint", findProperty("modstoml.forge_constraint"))
		}
		if (mc.version == "1.20.1" || mc.version == "1.20.4") {
			put("forge_id", loader.loader)
		}
	}

	props.forEach(inputs::property)

	if (loader.isFabric) {
		filesMatching("fabric.mod.json") { expand(props) }
		exclude(listOf("META-INF/mods.toml", "META-INF/neoforge.mods.toml"))
	}

	if (loader.isNeoforge) {
		if (mc.version == "1.20.4") {
			filesMatching("META-INF/mods.toml") { expand(props) }
			exclude("fabric.mod.json", "META-INF/neoforge.mods.toml")
		} else {
			filesMatching("META-INF/neoforge.mods.toml") { expand(props) }
			exclude("fabric.mod.json", "META-INF/mods.toml")
		}
	}
}

if (stonecutter.current.isActive) {
	rootProject.tasks.register("buildActive") {
		group = "project"
		dependsOn(tasks.named("build"))
	}
}

@Suppress("TYPE_MISMATCH", "UNRESOLVED_REFERENCE")
fun <T> optionalProp(property: String, block: (String) -> T?): T? =
	findProperty(property)?.toString()?.takeUnless { it.isBlank() }?.let(block)

fun isPropDefined(property: String): Boolean {
	return property(property)?.toString()?.isNotBlank() ?: false
}
