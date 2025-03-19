package org.infernalstudios.questlog;

import net.minecraft.client.KeyMapping;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.infernalstudios.config.Config;
import org.infernalstudios.questlog.config.QuestlogConfig;
import org.infernalstudios.questlog.event.QuestlogEventBus;
import org.infernalstudios.questlog.platform.Services;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;

public class Questlog {
  public static final String MODID = "questlog";
  public static final Logger LOGGER = LogManager.getLogger();

  public static final QuestlogEventBus EVENTS = new QuestlogEventBus();

  public static void init() {
  }

  public static void initClient() {
    try {
      QuestlogConfig.CONFIG = Config
        .builder(Services.PLATFORM.getConfigDirectory().resolve("questlog-client.toml"))
        .loadClass(QuestlogConfig.class, true)
        .build();
    } catch (IllegalStateException | IllegalArgumentException | IOException e) {
      throw new RuntimeException("Failed to load Questlog config", e);
    }

    QuestlogConfig.CONFIG.onReload(stage -> {
      if (stage == Config.ReloadStage.PRE) {
        Questlog.LOGGER.debug("Reloading Questlog config");
      }
    });
  }
}