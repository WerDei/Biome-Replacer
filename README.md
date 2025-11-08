<div align="center">
  <img src="https://cdn.modrinth.com/data/cached_images/49140522c0371f3ef71e14ad161300767b1fbc80_0.webp" alt="Biome Replacer Banner">
</div>

<div align="center">
  <img src="https://img.shields.io/modrinth/dt/biome-replacer?style=flat&logo=modrinth&logoColor=%2300AF5C&label=Modrinth&color=%2300AF5C&link=https%3A%2F%2Fmodrinth.com%2Fmod%2Fbiome-replacer" alt="Modrinth Downloads">
  <img src="https://img.shields.io/curseforge/dt/910274?style=flat&logo=CurseForge&logoColor=%23F16436&label=CurseForge&color=%23F16436&link=https%3A%2F%2Fwww.curseforge.com%2Fminecraft%2Fmc-mods%2Fbiome-replacer" alt="CurseForge Downloads">
  <img src="https://img.shields.io/badge/MC-1.18.2+-green?style=flat&logo=minecraft&logoColor=white" alt="Minecraft Versions">
  <img src="https://img.shields.io/badge/Side-Server-orange?style=flat" alt="Server Side">
  <img src="https://img.shields.io/github/license/WerDei/Biome-Replacer?style=flat&color=purple" alt="License">
  <a href="https://discord.gg/z3h4d3Ux3p" target="_blank">
      <img src="https://img.shields.io/discord/1206800378486726716?style=flat&logo=Discord&label=Unofficial%20Discord&color=%235765F2" alt="Discord">
  </a>
</div>

**Biome Replacer** is a simple and easy-to-use utility mod for removing unwanted biomes

## Features

- **Easy configuration:** Simple properties file with intuitive syntax
- **Designed for datapacks** Configure Terralith or other worldgen packs with ease\*
- **(Limited) mod integration:** Remove biomes from a mod that has no configuration of its own\*\*
- **Server-side:** No client installation needed (unless you want to use it in singleplayer, of course!)
- **High availability:** From 1.18.2 up to the most current Minecraft version, all equally supported
- **Negligible performance impact:** Runs only once on world start, does not slow down the generation

\*Please note that Biome Replacer **cannot change terrain shape**. This means that it's impossible 
to change oceans to land, create ocean-only worlds, etc.

\*\*The only currently supported biome libraries are TerraBlender & Blueprint, so you won't be able to change Biolith biomes, 
for example. Still, BR should not break, and will work fine alongside most world generation mods.

## Setup

1. Install the mod, then run your client/server once to generate configuration file
2. Find `biome_replacer.properties` in the `config` folder
3. Add your replacement rules (see examples below), then load up the world  

If you made any mistakes, you will see warnings in chat. Check for typos in your biome IDs, and try again.

Tip: on client, you don't deed to restart the game every time you change your rules, you can simply leave and enter the world again.

## Examples

Rules follow the format `old_biome > new_biome`. Here is an example with vanilla biomes:
```
minecraft:dark_forest > minecraft:cherry_grove
```
<div align="center">
  <img src="https://raw.githubusercontent.com/WerDei/Biome-Replacer/master/readme-files/example-1.png" alt="Dark forest replaced by cherry grove">
</div>


Using biome tags is supported:
```
#minecraft:is_forest > minecraft:desert
```
<div align="center">
  <img src="https://raw.githubusercontent.com/WerDei/Biome-Replacer/master/readme-files/example-5.png" alt="Two forest types replaced by Desert">
</div>


it's possible to completely remove biomes using a "null" keyword. For example, these rules will remove Terralith's skylands:
```
terralith:skylands_autumn > null
terralith:skylands_winter > null
terralith:skylands_spring > null
terralith:skylands_summer > null
```
You can also use a biome tag to achieve the same effect:
```
#terralith:skylands > null
```
Important: if you're not careful, using this method on biomes from vanilla and datapacks can cause crashes, or mess up biome distribution. 
Please **test thoroughly** before using it in a world you care about!
<div align="center">
  <img src="https://raw.githubusercontent.com/WerDei/Biome-Replacer/master/readme-files/example-6.png" alt="Sky Island is removed">
</div>

### Advanced features

To target specific dimensions, use headers with dimension ID: `[mod:dimension]`. Rules following it will only be applied to a specified dimension.
(Note that this typically isn't needed, unless you have custom dimensions that generate the same biomes, and you want to target only one of them)

```
[minecraft:overworld]
minecraft:desert > null

[custom:dimension]
minecraft:desert > minecraft:badlands
```

## Resources

* [Report bugs and issues here](https://github.com/WerDei/Biome-Replacer/issues)
* [Vanilla Biome Tags](https://mcreator.net/wiki/minecraft-biome-tags-list)
* [Vanilla Biome IDs](https://minecraft.wiki/w/Biome#Biome_IDs)
* [Source code](https://github.com/WerDei/Biome-Replacer)
