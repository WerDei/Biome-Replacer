plugins {
    id("dev.kikugie.stonecutter")
}
stonecutter active "1.21.4-fabric" /* [SC] DO NOT EDIT */

stonecutter tasks {
    // Order by version, and then by loader
    val ordering = versionComparator.thenComparingInt {
        when (it.metadata.project.split('-')[1]){
            "fabric" -> 2 // published last, so will appear first in version list
            "neoforge" -> 1
            else -> 0
        }
    }
    order("publishModrinth", ordering)
    order("publishCurseforge", ordering)
}