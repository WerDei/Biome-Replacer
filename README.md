**This mod has a Forge/Neoforge port:** https://modrinth.com/mod/biome-replacer-neoforge

----
<!-- modrinth_exclude.start -->
# Biome Replacer
Download: 
[Modrinth](https://modrinth.com/mod/biome-replacer) | 
[Curseforge](https://www.curseforge.com/minecraft/mc-mods/biome-replacer)
<!-- modrinth_exclude.end -->

Small mod that replaces one biome with another, without affecting other parts of the generation. 
Useful for preventing certain datapack biomes from generating, fixing modded biomes 
not appearing, or polishing up that almost-perfect seed.

Configuration is very simple. 
Find `biome-replacer.properties` in your configuration folder, and put replacement rules in the format
`old_biome > new_biome`. You can find some examples below.

Works fully server-side.

Note that this mod is intended mostly for replacing vanilla and datapack biomes.
**Biomes added using libraries like TerraBlender or Biolith cannot be replaced**; 
you should use the mod's config to tweak or remove them instead.  
That said, this mod can safely be used alongside these libraries, 
and it's even possible to replace vanilla/datapack biomes *with* modded ones.

## Examples

A case with vanilla biomes. This is what configuration file looks like:
```
minecraft:dark_forest > minecraft:cherry_grove
```
And here's how the world looks, before and after:
![Dark forest is replaced by a cherry grove](https://raw.githubusercontent.com/WerDei/Biome-Replacer/master/readme-files/example-1.png)

When using Terralith, Lavender Plains biome from Aurora's Decorations won't generate. 
Here's how we can fix it:
```
terralith:lavender_forest > aurorasdeco:lavender_plains
terralith:lavender_valley > aurorasdeco:lavender_plains
```
![Lavender Forest is replaced by Lavender Plains](https://raw.githubusercontent.com/WerDei/Biome-Replacer/master/readme-files/example-2.png)
![Same area, but from a higher perspective](https://raw.githubusercontent.com/WerDei/Biome-Replacer/master/readme-files/example-3.png)

Spiders are yucky. Let's remove Terralith's Infested caves:
```
terralith:cave/infested_caves > minecraft:dripstone_caves
```
![Infested Caves are replaced by Dropstone Caves](https://raw.githubusercontent.com/WerDei/Biome-Replacer/master/readme-files/example-4.png)

## Contributors
Big thanks to these cool people:
- Etos2
- LegendarySpy 