- Added support for replacing biomes from Blueprint-based mods
- Added per-dimension overrides
  - Adding a header with dimension ID, like `[minecraft:overworld]`, will make following rules applied only to that. 
    This way, you can have different rules per dimension.
- If there are any issues with your configuration, you will now see a warning when loading into the world
- To prevent world corruption, using marker biomes from TerraBlender and Blueprint in rules is no longer possible
