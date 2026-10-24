package org.infernalstudios.questlog;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.infernalstudios.questlog.config.QuestlogConfig;
import org.infernalstudios.questlog.core.quests.objectives.ObjectiveListeners;
import org.infernalstudios.questlog.event.QuestlogEventBus;

public class Questlog {
    public static final String MODID = "questlog";
    public static final Logger LOGGER = LogManager.getLogger();

    public static final QuestlogEventBus EVENTS = new QuestlogEventBus();

    public static void init() {
        ObjectiveListeners.init();
    }

    public static void initClient() {
        AutoConfig.register(QuestlogConfig.class, Toml4jConfigSerializer::new);

        AutoConfig.getConfigHolder(QuestlogConfig.class).registerSaveListener((manager, newData) -> {
            Questlog.LOGGER.debug("Reloading/Saving Questlog config");
            return InteractionResult.PASS;
        });
    }

    /**
     * Helper method to easily access the config instance from anywhere.
     */
    public static QuestlogConfig getConfig() {
        return AutoConfig.getConfigHolder(QuestlogConfig.class).getConfig();
    }

    /**
     * Saves the current client configuration to disk.
     */
    public static void saveConfig() {
        AutoConfig.getConfigHolder(QuestlogConfig.class).save();
    }

    public static ResourceLocation id(String path) {
        return id(MODID, path);
    }

    public static ResourceLocation id(String namespace, String path) {
        //? if >=1.21 {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
        //?} else
        //return new ResourceLocation(namespace, path);
    }

    public static ResourceLocation parseId(String id) {
        //? if >=1.21 {
        return ResourceLocation.parse(id);
        //?} else
        //return new ResourceLocation(id);
    }

    public static ResourceLocation mcId(String path) {
        //? if >=1.21 {
        return ResourceLocation.withDefaultNamespace(path);
        //?} else
        //return new ResourceLocation(path);
    }
}