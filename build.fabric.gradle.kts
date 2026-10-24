plugins {
    id("dev.kikugie.loom-back-compat")
    id("me.modmuss50.mod-publish-plugin")
    id("maven-publish")
}

val minecraft = stonecutter.current.version
val mcVersion = prop("deps.minecraft")
val loader = "fabric"

fun prop(name: String) = project.property(name) as String

version = prop("mod.version")
group = prop("mod.group")
base.archivesName = "${prop("mod.id")}-$loader-$minecraft"

sourceSets.main {
    resources.srcDir(rootProject.file("src/main/overlays/$loader"))
    resources.srcDir(rootProject.file("src/main/overlays/$minecraft"))
}

repositories {
    mavenCentral()
    exclusiveContent {
        forRepository { maven("https://maven.parchmentmc.org/") { name = "ParchmentMC" } }
        filter { includeGroup("org.parchmentmc.data") }
    }
    exclusiveContent {
        forRepository { maven("https://api.modrinth.com/maven") { name = "Modrinth" } }
        filter { includeGroup("maven.modrinth") }
    }
    maven("https://maven.terraformersmc.com/") { name = "TerraformersMC" }
    maven("https://maven.shedaniel.me/") { name = "ClothConfig" }
    maven("https://maven.ladysnake.org/releases") { name = "Ladysnake Mods" }
    maven("https://maven.cafeteria.dev") { name = "Cafeteria" }
    maven("https://maven.jamieswhiteshirt.com/libs-release") { name = "JamiesWhiteShirt" }
    maven("https://jitpack.io") { name = "JitPack" }
    maven("https://maven.quiltmc.org/repository/release/") { name = "QuiltMC" }
}

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${prop("deps.parchment_minecraft")}:${prop("deps.parchment")}@zip")
    })

    modImplementation("net.fabricmc:fabric-loader:${prop("deps.fabric_loader")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${prop("deps.fabric_api")}")

    // EMI
    modRuntimeOnly("dev.emi:emi-fabric:${prop("deps.emi")}")

    // Cloth Config
    modImplementation("me.shedaniel.cloth:cloth-config-fabric:${prop("deps.cloth_config")}")
    modImplementation("com.terraformersmc:modmenu:${prop("deps.modmenu")}")

    // Triggers
    modImplementation("maven.modrinth:triggers:${prop("deps.triggers")}-fabric")
    include("maven.modrinth:triggers:${prop("deps.triggers")}-fabric")

    // Origins
    if (stonecutter.eval(minecraft, ">=1.21")) {
        modImplementation("maven.modrinth:origins:${prop("deps.origins")}")
        modImplementation("io.github.apace100:apoli:${prop("deps.apoli")}")
        modImplementation("io.github.apace100:calio:${prop("deps.calio")}")
        modImplementation("dev.onyxstudios.cardinal-components-api:cardinal-components-base:${prop("deps.cardinal_components")}")
        modImplementation("dev.onyxstudios.cardinal-components-api:cardinal-components-entity:${prop("deps.cardinal_components")}")
    } else {
        modCompileOnly("maven.modrinth:origins:${prop("deps.origins")}")
    }

    implementation("net.jodah:typetools:${prop("deps.typetools")}")
    include("net.jodah:typetools:${prop("deps.typetools")}")
    implementation("com.google.code.findbugs:jsr305:3.0.1")
}

loom {
    runs.configureEach {
        ideConfigGenerated(true)
        runDir("run/$name")
    }
}

val javaVer = prop("deps.java_version").toInt()
java {
    toolchain.languageVersion = JavaLanguageVersion.of(javaVer)
    withSourcesJar()
}

tasks {
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release = javaVer
    }

    processResources {
        val props = mapOf(
            "version" to project.version,
            "group" to project.group,
            "minecraft_version" to prop("mod.mc_dep_fabric"),
            "fabric_version" to prop("deps.fabric_api"),
            "fabric_loader_version" to prop("deps.fabric_loader"),
            "mod_name" to prop("mod.name"),
            "mod_author" to prop("mod.author"),
            "mod_id" to prop("mod.id"),
            "license" to prop("mod.license"),
            "description" to prop("mod.description"),
            "credits" to prop("mod.credits"),
            "java_version" to javaVer,
            "pack_format" to prop("mod.pack_format"),
        )
        inputs.properties(props)

        filesMatching(listOf("pack.mcmeta", "fabric.mod.json", "*.mixins.json")) {
            expand(props)
        }

        dependsOn("stonecutterGenerate")
    }

    jar {
        from(rootProject.file("LICENSE")) {
            rename { "${it}_${prop("mod.name")}" }
        }

        manifest.attributes(
            "Specification-Title" to prop("mod.name"),
            "Specification-Vendor" to prop("mod.author"),
            "Specification-Version" to archiveVersion,
            "Implementation-Title" to loader,
            "Implementation-Version" to archiveVersion,
            "Implementation-Vendor" to prop("mod.author"),
            "Built-On-Minecraft" to mcVersion,
        )
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        from(loomx.modJar.map { it.archiveFile })
        into(rootProject.layout.buildDirectory.dir("libs/${project.version}"))
        dependsOn("build")
    }
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            artifactId = base.archivesName.get()
            from(components["java"])
        }
    }
    repositories {
        System.getenv("local_maven_url")?.let { maven(it) }
    }
}

publishMods {
    file = loomx.modJar.flatMap { it.archiveFile }
    changelog = provider { rootProject.file("CHANGELOG-LATEST.md").readText() }
    type = STABLE
    version = "${project.version}-$minecraft-$loader"
    displayName = "${prop("mod.name")} Fabric $minecraft - ${project.version}"
    modLoaders.add(loader)

    curseforge {
        accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
        projectId = prop("publish.curseforge")
        minecraftVersions.add(mcVersion)
        client = true
        server = true

        requires("cloth-config")
    }

    modrinth {
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
        projectId = prop("publish.modrinth")
        minecraftVersions.add(mcVersion)

        requires("cloth-config")
    }
}
