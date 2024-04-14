package org.infernalstudios.questlog.config;

import org.infernalstudios.config.annotation.Configurable;

// import org.infernalstudios.config.annotation.Configurable;

public class QuestlogConfig {
  @Configurable(description = "Quest definition registry JSON file (quests.json) location")
  public static String questRegistryLocation = "questlog:quests.json";
}
