- Reworked biome replacement code. This should be unnoticeable, but it will prevent rules-not-loading type of bugs from happening again in the future.
- Log messages are now a bit more informative
- Added functionality for removing biomes (instead of replacing them)

Biome removal is a potentially unstable feature. 
It can mess up biome distribution, or even cause crashes.
Please **test thoroughly** before using it in a world you care about!  
You can remove a biome using a "null" keyword:
```
minecraft:desert > null
```