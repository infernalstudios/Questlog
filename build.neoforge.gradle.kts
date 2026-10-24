plugins {
    id("net.neoforged.moddev")
    id("me.modmuss50.mod-publish-plugin")
    id("maven-publish")
}

val minecraft = stonecutter.current.version
val mcVersion = prop("deps.minecraft")
val loader = "neoforge"

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
        forRepository { maven("https://api.modrinth.com/maven") { name = "Modrinth" } }
        filter { includeGroup("maven.modrinth") }
    }
    maven("https://maven.terraformersmc.com/") { name = "TerraformersMC" }
    maven("https://maven.shedaniel.me/") { name = "ClothConfig" }
}

neoForge {
    version = prop("deps.neoforge")

    parchment {
        minecraftVersion = prop("deps.parchment_minecraft")
        mappingsVersion = prop("deps.parchment")
    }

    runs {
        configureEach {
            systemProperty("neoforge.enabledGameTestNamespaces", prop("mod.id"))
            ideName = "NeoForge ${name.replaceFirstChar(Char::uppercase)} ($minecraft)"
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
    compileOnly("org.jetbrains:annotations:26.0.2-1")

    // EMI
    runtimeOnly("dev.emi:emi-neoforge:${prop("deps.emi")}")

    // Cloth Config
    implementation("me.shedaniel.cloth:cloth-config-neoforge:${prop("deps.cloth_config")}")

    // Triggers
    implementation("maven.modrinth:triggers:${prop("deps.triggers")}-neoforge")
    jarJar("maven.modrinth:triggers:${prop("deps.triggers")}-neoforge")

    // Origins
    implementation("maven.modrinth:origins-neoforge:${prop("deps.origins")}")
    implementation("maven.modrinth:jupiter:${prop("deps.jupiter")}-neoforge")

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
            "neoforge_version" to prop("deps.neoforge"),
            "neoforge_loader_version_range" to prop("deps.neoforge_loader_version_range"),
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

        filesMatching(listOf("pack.mcmeta", "META-INF/neoforge.mods.toml", "*.mixins.json")) {
            expand(props)
        }
    }

    named("createMinecraftArtifacts") {
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
        from(jar.map { it.archiveFile })
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
    file = tasks.jar.flatMap { it.archiveFile }
    changelog = provider { rootProject.file("CHANGELOG-LATEST.md").readText() }
    type = STABLE
    version = "${project.version}-$minecraft-$loader"
    displayName = "${prop("mod.name")} NeoForge $minecraft - ${project.version}"
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
