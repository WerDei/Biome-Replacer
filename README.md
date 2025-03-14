<div align="center">
  <img src="https://cdn.modrinth.com/data/cached_images/49140522c0371f3ef71e14ad161300767b1fbc80_0.webp" alt="Biome Replacer Banner">
</div>

<div align="center">
  <img src="https://img.shields.io/modrinth/dt/biome-replacer?style=flat&logo=modrinth&logoColor=%2300AF5C&label=Modrinth&color=%2300AF5C&link=https%3A%2F%2Fmodrinth.com%2Fmod%2Fbiome-replacer" alt="Modrinth Downloads">
  <img src="https://img.shields.io/curseforge/dt/910274?style=flat&logo=CurseForge&logoColor=%23F16436&label=CurseForge&color=%23F16436&link=https%3A%2F%2Fwww.curseforge.com%2Fminecraft%2Fmc-mods%2Fbiome-replacer" alt="CurseForge Downloads">
  <img src="https://img.shields.io/badge/MC-1.19+-green?style=flat&logo=minecraft&logoColor=white" alt="Minecraft Versions">
  <img src="https://img.shields.io/badge/Side-Server-orange?style=flat" alt="Server Side">
  <img src="https://img.shields.io/github/license/WerDei/Biome-Replacer?style=flat&color=purple" alt="License">
<a href="https://discord.gg/z3h4d3Ux3p" target="_blank">
    <img src="https://img.shields.io/discord/1206800378486726716?style=flat&logo=Discord&label=Unofficial%20Discord&color=%235765F2" alt="Discord">
</a>
</div>

<div align="center">
  <strong>Server-side mod for precise biome control</strong>
</div>

## Overview

**Biome Replacer** lets you control biome distribution without changing terrain generation. Replace any biome with another while preserving the landscape's shape and only changing biome-specific features like colors, mob spawns, and vegetation.

### Use Cases:

* Remove unwanted biomes
* Fix mod compatibility issues
* Enhance almost-perfect world seeds
* Create custom world experiences
* Complement worldgen mods like Terralith

## Features

* **Simple Biome Swapping:** Replace biomes with an easy rule system
* **Tag Support:** Replace entire biome categories at once
* **Server-Side:** No client installation needed
* **Easy Config:** Simple properties file with intuitive syntax

## Experimental Features
* **Chance based replacement:** Replace any percentage of a biome


## Compatibility

* **Works With:** Vanilla biomes and most datapack biomes (including Terralith)
* **Limited Support:** TerraBlender, Biolith or Lithosphere biomes (use their native options instead)
* **Generally Compatible:** Works with most worldgen mods (report issues [here](https://github.com/WerDei/Biome-Replacer/issues))

## Setup

1. Install the mod on your server (or client if your playing singleplayer)
2. Run the server once to generate config
3. Find `biome_replacer.properties` in the `config` folder
4. Add your replacement rules

### Configuration

#### Basic Replacement

```
minecraft:dark_forest > minecraft:cherry_grove
```

#### Tag-Based Replacement

```
#minecraft:is_forest > minecraft:desert
```

#### Options

```
# Disable chat notifications
muteChatInfo = true
```

## Examples

### Forest to Cherry Grove

**Config:**
```
minecraft:dark_forest > minecraft:cherry_grove
```

<div align="center">
  <img src="https://raw.githubusercontent.com/WerDei/Biome-Replacer/master/readme-files/example-1.png" alt="Dark forest replaced by cherry grove">
</div>

### All Forests to Desert

**Config:**
```
#minecraft:is_forest > minecraft:desert
```
![Replacing all forests with deserts](https://cdn.modrinth.com/data/cached_images/03a6cb91021fd0a49b3c0be323c7427b097027fe_0.webp)


### Mod Integration

**Config:**
```
terralith:lavender_forest > aurorasdeco:lavender_plains
terralith:lavender_valley > aurorasdeco:lavender_plains
```

<div align="center">
  <img src="https://raw.githubusercontent.com/WerDei/Biome-Replacer/master/readme-files/example-2.png" alt="Lavender Forest replaced by Lavender Plains">
</div>

### Fixing Problem Biomes

**Config:**
```
terralith:cave/infested_caves > minecraft:dripstone_caves
```

<div align="center">
  <img src="https://raw.githubusercontent.com/WerDei/Biome-Replacer/master/readme-files/example-4.png" alt="Infested Caves replaced by Dripstone Caves">
</div>

## Resources

* [Minecraft Biome Tags Reference](https://mcreator.net/wiki/minecraft-biome-tags-list)
* [GitHub Repository](https://github.com/WerDei/Biome-Replacer)
* [Report Issues](https://github.com/WerDei/Biome-Replacer/issues)