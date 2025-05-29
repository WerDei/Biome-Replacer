# Mod Integration Guide

Biome Replacer supports integration with other biome mods to provide better compatibility and avoid conflicts. When compatible mods are detected, Biome Replacer will automatically use their APIs instead of direct biome manipulation.

## Currently Supported Integrations

### Biolith
[Biolith](https://github.com/TerraformersMC/Biolith) is a biome placement API that works great with TerraBlender and other worldgen mods. When Biolith is installed, Biome Replacer will automatically use it for biome replacement.

**Why this matters:**
- Better compatibility with TerraBlender mods
- More stable worldgen 
- No conflicts with other biome-modifying mods

## How It Works

### Automatic Detection
The mod checks for compatible integrations at startup using reflection, so it works regardless of which mod loader you're using. If a supported mod is found, integration happens automatically.

### Configuration Compatibility  
Your existing config files work exactly the same - no changes needed! Whether you're using direct biome replacements, tag-based rules, or probability settings, everything transfers over seamlessly.

### Examples
```properties
# These all work with integrations
minecraft:desert > minecraft:plains
biomesoplenty:redwood_forest > minecraft:forest
#minecraft:is_ocean > minecraft:plains 0.5
```

### Fallback System
If integration fails or the other mod isn't available, Biome Replacer falls back to its direct replacement system. You'll see clear log messages about which system is being used.


## User Experience

### With Integration Active
```
[BiomeReplacer] Biolith detected! Using Biolith for biome replacement.
[BiomeReplacer] Converted 3 replacement rules for Biolith integration.
[BiomeReplacer] Biome replacement handled by Biolith.
```

### Without Integration  
```
[BiomeReplacer] Using direct biome replacement system.
[BiomeReplacer] Loaded 3 rules (2 direct, 1 tag-based).
```

### Integration Error
```
[BiomeReplacer] Failed to initialize Biolith integration: [details]
[BiomeReplacer] Falling back to direct biome replacement.
```

## Notes

- Integration happens automatically - no user configuration required
- All existing config files continue to work unchanged  
- Performance is the same or better with integrations
- Clear logging shows which system is handling replacements
- Graceful fallback ensures the mod always works
