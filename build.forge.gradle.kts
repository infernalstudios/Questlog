plugins {
    id("net.neoforged.moddev.legacyforge")
    id("me.modmuss50.mod-publish-plugin")
    id("maven-publish")
}

val minecraft = stonecutter.current.version
val loader = "forge"

fun prop(name: String) = project.property(name) as String

version = prop("mod.version")
group = prop("mod.group")
base.archivesName = "${prop("mod.id")}-$loader-$minecraft"

sourceSets.main {
    resources.srcDir(rootProject.file("src/main/overlays/$loader"))
    resources.srcDir(rootProject.file("src/main/overlays/$minecraft"))
}

val refmap = "${prop("mod.id")}.refmap.json"
val mixinConfigs = listOf("${prop("mod.id")}.mixins.json", "${prop("mod.id")}.forge.mixins.json")

repositories {
    mavenCentral()
    exclusiveContent {
        forRepository { maven("https://api.modrinth.com/maven") { name = "Modrinth" } }
        filter { includeGroup("maven.modrinth") }
    }
    maven("https://maven.terraformersmc.com/") { name = "TerraformersMC" }
    maven("https://maven.shedaniel.me/") { name = "ClothConfig" }
}

mixin {
    add(sourceSets["main"], refmap)
    mixinConfigs.forEach(::config)
}

legacyForge {
    version = "$minecraft-${prop("deps.forge")}"

    parchment {
        minecraftVersion = prop("deps.parchment_minecraft")
        mappingsVersion = prop("deps.parchment")
    }

    runs {
        configureEach {
            ideName = "Forge ${name.replaceFirstChar(Char::uppercase)} ($minecraft)"
            gameDirectory = file("run/$name")
        }
        register("client") {
            client()
        }
        register("server") {
            server()
        }
    }

    mods {
        register(prop("mod.id")) {
            sourceSet(sourceSets["main"])
        }
    }
}

dependencies {
    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
    compileOnly("org.jetbrains:annotations:26.0.2-1")

    // EMI
    modRuntimeOnly("dev.emi:emi-forge:${prop("deps.emi")}")

    // Cloth Config
    modImplementation("me.shedaniel.cloth:cloth-config-forge:${prop("deps.cloth_config")}")

    // Triggers
    modImplementation("maven.modrinth:triggers:${prop("deps.triggers")}-forge")
    "jarJar"("maven.modrinth:triggers:${prop("deps.triggers")}-forge")

    // Origins
    modCompileOnly("maven.modrinth:origins-forge:${prop("deps.origins")}")

    implementation("net.jodah:typetools:${prop("deps.typetools")}")
    implementation("com.google.code.findbugs:jsr305:3.0.1")
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
            "minecraft_version" to minecraft,
            "minecraft_version_range" to prop("mod.mc_dep_forgelike"),
            "forge_version" to prop("deps.forge"),
            "forge_loader_version_range" to prop("deps.forge_loader_version_range"),
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

        filesMatching(listOf("pack.mcmeta", "META-INF/mods.toml", "*.mixins.json")) {
            expand(props)
        }

        filesMatching(mixinConfigs) {
            filter { line -> line.replace("\"package\":", "\"refmap\": \"$refmap\",\n  \"package\":") }
        }
    }

    named("createMinecraftArtifacts") {
        dependsOn("stonecutterGenerate")
    }

    jar {
        finalizedBy("reobfJar")

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
            "Built-On-Minecraft" to minecraft,
            "MixinConfigs" to mixinConfigs.joinToString(","),
        )
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        from(named<Jar>("reobfJar").map { it.archiveFile })
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
    file = tasks.named<Jar>("reobfJar").flatMap { it.archiveFile }
    changelog = provider { rootProject.file("CHANGELOG-LATEST.md").readText() }
    type = STABLE
    version = "${project.version}-$minecraft-$loader"
    displayName = "${prop("mod.name")} Forge $minecraft - ${project.version}"
    modLoaders.add(loader)

    curseforge {
        accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
        projectId = prop("publish.curseforge")
        minecraftVersions.add(minecraft)
        client = true
        server = true

        requires("cloth-config")
    }

    modrinth {
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
        projectId = prop("publish.modrinth")
        minecraftVersions.add(minecraft)

        requires("cloth-config")
    }
}
