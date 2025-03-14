import dev.kikugie.stonecutter.StonecutterSettings

pluginManagement {
	repositories {
		mavenCentral()
		gradlePluginPortal()
		maven("https://maven.fabricmc.net/")
		maven("https://maven.neoforged.net/releases/")
		maven("https://maven.architectury.dev")
		maven("https://maven.kikugie.dev/snapshots")
		maven("https://maven.kikugie.dev/releases")
	}
}

plugins {
	id("dev.kikugie.stonecutter") version "0.4.4"
}

extensions.configure<StonecutterSettings> {
	kotlinController= true
	centralScript = "build.gradle.kts"

	shared {
		fun mc(mcVersion: String, loaders: Iterable<String>) {
			for (loader in loaders) {
				vers("$mcVersion-$loader", mcVersion)
			}
		}

		// It's a good practice to append MC version to your mod's version, but since our builds work across
		// multiple Minecraft versions, it might be confusing to see, like, version 2.0-mc1.19.4 working on 1.21.
		// To solve this non-issue and make it harder to understand, Minecraft versions that a mod is loading on
		// will now have a codename, to group them.

		// Codename Toad
		// Works across 1.18.2 - 1.19.2
		// Unfortunately it crashes on 1.19.3, but that version is kinda irrelevant now so idk if its worth its own port
		mc("1.19.2", listOf("fabric", "lexforge"))

		// Codename Elephant
		// Works across 1.19.4 - 1.21.1 for Fabric, 1.20.5 - 1.21.1 for Neo, 1.20-1.20.1 for Forge
		// (neo had made some changes on 20.5, and I can't be bothered to make it load both on and before it)
		mc("1.20.6", listOf("fabric", "neoforge"))
		mc("1.20.1", listOf("lexforge"))

		// Codename Minnow
		// Works across 1.21.2 - 1.21.4
		mc("1.21.4", listOf("fabric", "neoforge"))

		vcsVersion("1.21.4-fabric")
	}
	create(rootProject)
}

rootProject.name = "Biome Replacer"
