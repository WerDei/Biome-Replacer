## Setup

### Find the config file

The config is **not** inside the mod `.jar` file.

1. Put the mod in your `mods` folder
2. Launch Minecraft once
3. Open the `config` folder (next to `mods`)
4. Edit `biome_replacer.properties`

### Save your changes

After editing the file, **save it** before loading a world.

If nothing changes, this is the problem more often than you'd think.

### Apply your changes

- **Singleplayer:** Leave the world and re-enter it. No full game restart needed.
- **Server:** Restart the server, or reload the world.

---

## Writing rules

### Use the correct format

Each rule must look like this:

```
old_biome > new_biome
```

**Wrong:**
```
minecraft:dark_forest -> minecraft:cherry_grove
minecraft:dark_forest = minecraft:cherry_grove
minecraft:dark_forest minecraft:cherry_grove
```

**Right:**
```
minecraft:dark_forest > minecraft:cherry_grove
```

To remove a biome entirely, use `null`:

```
terralith:skylands_autumn > null
```

### Remove the `!` in front of your rules

New config files come with example lines that start with `!`. Those lines are ignored.

**Wrong (ignored):**
```
! minecraft:dark_forest > minecraft:cherry_grove
```

**Right:**
```
minecraft:dark_forest > minecraft:cherry_grove
```

### Spell biome IDs exactly right

Biome IDs are **all lowercase**. Do not use capital letters.

**Wrong:**
```
Minecraft:Dark_Forest > Minecraft:Cherry_Grove
minecraft:Dark_Forest > minecraft:Cherry_Grove
```

**Right:**
```
minecraft:dark_forest > minecraft:cherry_grove
```

Check spelling in-game:

- Press **F3** and read the biome name on screen
- Type `/locate biome` and copy the exact name

The game shows IDs like `minecraft:dark_forest`. Write them the same way in your config.

### `#` is for tags, not single biomes

`#` marks a **tag** (a group of biomes). Do not put it in front of one biome.

**Wrong:**
```
#minecraft:plains > null
```

**Right:**
```
minecraft:plains > null
```

**Tag (correct):**
```
#minecraft:is_forest > minecraft:desert
```

Lines starting with `# ` (hash + space) are comments and are ignored.

### Common typos

| Wrong | Right |
|-------|-------|
| `biomesohplenty:...` | `biomesoplenty:...` |
| `minecraft:oceans` | `minecraft:ocean` |
| `terralith:infested_caves` | `terralith:cave/infested_caves` |

Wiki pages can be outdated. If a biome was removed in a mod update, the ID may no longer exist. Always check in-game with F3 or `/locate biome`.

---

## Making sure it works

### Only new chunks are affected

Biome Replacer runs when a world loads. It does **not** change chunks that already exist.

If you edit the config and reload the **same** old world, already-generated areas will look unchanged.

**To see changes:**

- Create a **new world**, or
- Explore **new** chunks in an existing world, or
- Delete old chunks with a tool like [MCA Selector](https://github.com/Querz/mcaselector) (back up your world first)

### Read the warnings in chat

On most versions, Biome Replacer shows config problems in chat when you join a world:

```
[BiomeReplacer] There are issues in the configuration file:
Line 5: Invalid biome ID 'minecraft:dark_forestt', ignoring rule
```

Fix the line number it points to.

If you see **no** warnings and nothing works, the mod may not have loaded. Check `logs/latest.log` for `[BiomeReplacer]`.

---

## Things that will not work

**Wrong server type:** Biome Replacer is a Fabric / Forge / NeoForge mod. It does not work on Paper, Spigot, Bukkit, or other plugin servers.

**Oceans and rivers:** You can swap ocean biomes for other ocean biomes, but Biome Replacer cannot change terrain shape. Replacing an ocean with a forest will not remove the water. You will mostly only see the change in F3.

**Chance / percentage rules:** Rules like `minecraft:plains > null 0.5` are not supported. Every rule is 100% or nothing.

**Some modded biomes:** Biome Replacer works with TerraBlender and Blueprint mods. It does not support Biolith and some other biome libraries.

**Cave biomes are separate rules:** Surface and cave biomes are different IDs. Replacing a surface biome will not affect cave biomes underneath. To change or remove a cave biome, add a rule for it directly (e.g. `minecraft:deep_dark > null`).

**Terrain shape:** You cannot turn oceans into land, remove all water, or reshape the world. Biome Replacer only swaps which biome the game thinks an area is.

---

## Quick checklist

Before asking for help:

- [ ] Config file is at `config/biome_replacer.properties`
- [ ] File is saved after editing
- [ ] No `!` at the start of active rules
- [ ] Format is `old_biome > new_biome`
- [ ] Biome IDs are all lowercase
- [ ] No `#` in front of single biomes (only in front of tags)
- [ ] IDs checked with F3 or `/locate biome`
- [ ] Biome Replacer version matches your Minecraft version and modloader
- [ ] Testing on a **new world** or **new chunks**
- [ ] Checked chat and `logs/latest.log` for warnings

---

## Still stuck?

- Read the [README](../README.md) for examples
- [Open a GitHub issue](https://github.com/WerDei/Biome-Replacer/issues) and include:
  - Minecraft version
  - Modloader (Fabric / Forge / NeoForge)
  - Biome Replacer version
  - Your `biome_replacer.properties` (copy-paste the text)
  - `logs/latest.log` from your Minecraft folder
