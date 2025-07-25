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
	id("dev.kikugie.stonecutter") version "0.7"
}

stonecutter {
	kotlinController = true
	centralScript = "build.gradle.kts"

	shared {
		fun add(minecraft: String, vararg loaders: String) =
            loaders.forEach { loader -> vers("$minecraft-$loader", minecraft) }

		// It's a good practice to append MC version to your mod's version, but since our builds work across
		// multiple Minecraft versions, it might be confusing to see, like, version 2.0-mc1.19.4 working on 1.21.
		// To solve this non-issue and make it harder to understand, Minecraft versions that a mod is loading on
		// will now have a codename, to group them.

		// Codename Toad
		// Works across 1.18.2 - 1.19.2
		add("1.19.2", "fabric", "oldforge")

		// Codename Gecko
		// Works on 1.19.3, which is largely irrelevant was trivial to port to
		add("1.19.3", "fabric", "oldforge")

		// Codename Hippo
		// Works across 1.19.4 - 1.21.1, with 1.20.5 being the point where we switch to Neoforge
		add("1.20.1", "oldforge")
		add("1.20.6", "fabric", "neoforge")

		// Codename Minnow
		// Works across 1.21.2 - 1.21.8
		add("1.21.4", "fabric", "neoforge")

		vcsVersion = "1.21.4-fabric"
	}
	create(rootProject)
}

rootProject.name = "Biome Replacer"
