plugins {
    id("dev.kikugie.stonecutter")
    id("me.modmuss50.mod-publish-plugin") version "0.5.1"
}
stonecutter active "1.19.2-fabric" /* [SC] DO NOT EDIT */

stonecutter registerChiseled tasks.register("chiseledBuild", stonecutter.chiseled) {
    group = "chiseled"
    ofTask("build")
}

stonecutter registerChiseled tasks.register("chiseledRunClient", stonecutter.chiseled) {
    group = "chiseled"
    ofTask("runClient")
}
