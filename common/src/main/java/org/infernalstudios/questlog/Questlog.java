package org.infernalstudios.questlog;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.minecraft.world.InteractionResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.infernalstudios.questlog.config.QuestlogConfig;
import org.infernalstudios.questlog.event.QuestlogEventBus;

public class Questlog {
    public static final String MODID = "questlog";
    public static final Logger LOGGER = LogManager.getLogger();

    public static final QuestlogEventBus EVENTS = new QuestlogEventBus();

    public static void init() {
    }

    public static void initClient() {
        AutoConfig.register(QuestlogConfig.class, Toml4jConfigSerializer::new);

        AutoConfig.getConfigHolder(QuestlogConfig.class).registerSaveListener((manager, newData) -> {
            Questlog.LOGGER.debug("Reloading/Saving Questlog config");
            return InteractionResult.PASS;
        });
    }

    /**
     * Helper method to easily access the config instance from anywhere in your mod.
     */
    public static QuestlogConfig getConfig() {
        return AutoConfig.getConfigHolder(QuestlogConfig.class).getConfig();
    }
}