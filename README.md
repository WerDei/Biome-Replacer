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

## Overview

**Biome Replacer** lets you control biome distribution without changing terrain generation. 
Replace any biome with another while preserving the landscape's shape and only changing biome-specific features 
like colors, mob spawns, and vegetation.

### Use cases:

- Remove unwanted biomes
- Configure worldgen datapacks like Terralith
- Fix mod compatibility issues
- Enhance almost-perfect world seeds
- Create custom world experiences

## Features

- **Easy configuration:** Simple properties file with intuitive syntax
- **Negligible performance impact:** Runs only once, and does not slow down the generation
- **Tag support:** Replace entire biome categories at once
- **Server-side:** No client installation needed (unless you want to use it in singleplayer, of course!)

### Experimental features
These haven't been tested thoroughly yet, and can be changed or removed in future versions.
They can potentially damage your world, so be careful!
- **Full biome removal:** remove a biome completely instead of replacing it with another
- **Chance based replacement:** Replace any percentage of a biome


## Caveats

- **Works perfectly with:** Vanilla and datapack biomes (including Terralith)
- **Terrain will NOT be changed:** Biome Replacer can't remove or create new rivers, oceans, mountains, etc.
- **Cannot replace:** TerraBlender or Biolith biomes - you should use their native configs instead

Note: Currently there is a bug with TerraBlender. If you install it and try to replace a _vanilla_ biome, 
it might sometimes still appear in the world. See [this issue](https://github.com/WerDei/Biome-Replacer/issues/21) for more information.  
(Oh, and if you are a developer knowledgeable in TB's workings, we'd appreciate the help on fixing this!)

## Setup

1. Install the mod, then run your client/server once to generate configuration file
2. Find `biome_replacer.properties` in the `config` folder
3. Add your replacement rules, then load up the world  

Tip: on client, you don't deed to restart the game every time you change your rules, you can simply leave and enter the world again.

### Configuration

#### Basic replacement
```
minecraft:dark_forest > minecraft:cherry_grove
```

#### Tag-Based replacement
Note: this does not work on 1.18.2
```
#minecraft:is_forest > minecraft:desert
```

#### Options
```
! Disable chat notifications
muteChatInfo = true
```

<details>
<summary>Experimental features</summary>

Reminder: these aren't fully tested! 
Make sure everything works as it should before using these in a world you care about!

#### Remove biome without replacement
```
minecraft:desert > null
```

#### Chance-based replacement
```
! old_biome > new_biome [probability]
! For reference: 0.9 = 90%, 0.5 = 50%, 0.1 = 10%, etc.
minecraft:taiga > minecraft:desert 0.5
#minecraft:is_mountain > minecraft:badlands 0.35
```
</details>

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

<div align="center">
  <img src="https://raw.githubusercontent.com/WerDei/Biome-Replacer/master/readme-files/example-5.png" alt="Two forest types replaced by Desert">
</div>

### Adding mod compatibility

**Config:**
```
terralith:lavender_forest > aurorasdeco:lavender_plains
terralith:lavender_valley > aurorasdeco:lavender_plains
```

<div align="center">
  <img src="https://raw.githubusercontent.com/WerDei/Biome-Replacer/master/readme-files/example-2.png" alt="Lavender Forest replaced by Lavender Plains">
</div>

### Removing unwanted biomes

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
