package org.infernalstudios.questlog;

import net.minecraft.client.KeyMapping;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.infernalstudios.questlog.event.QuestlogEventBus;
import org.lwjgl.glfw.GLFW;

public class Questlog {
  public static final String MODID = "questlog";
  public static final Logger LOGGER = LogManager.getLogger();

  public static final QuestlogEventBus EVENTS = new QuestlogEventBus();

  public static void init() {
  }
}