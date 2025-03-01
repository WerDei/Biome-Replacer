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

		//TODO 1.19?
		//TODO dedicated lexforge 1.20.1 port?
		mc("1.20.1", listOf("fabric", "neoforge")) //Works across 1.19.4 - 1.21.1
		mc("1.21.4", listOf("fabric", "neoforge")) // 1.21.2 - 1.21.4

		vcsVersion("1.21.4-fabric")
	}
	create(rootProject)
}

rootProject.name = "Biome Replacer"
