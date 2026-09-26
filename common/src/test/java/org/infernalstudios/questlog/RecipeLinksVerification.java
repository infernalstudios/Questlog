package org.infernalstudios.questlog;

import com.google.gson.JsonObject;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.Bootstrap;
import org.infernalstudios.questlog.core.quests.display.QuestDisplayData;

/** Parser checks; opening a viewer still needs an in-game test. */
public final class RecipeLinksVerification {
    private static int checks;

    public static void main(String[] args) {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        Component description = description("Use [Emerald](item:minecraft:emerald).");
        Component link = description.getSiblings().get(1);
        check(description.getString().equals("Use Emerald."), "Keep the readable label");
        check(link.getStyle().isUnderlined(), "Mark the recipe link");
        check(link.getStyle().getClickEvent().getAction() == ClickEvent.Action.CHANGE_PAGE, "Use the existing click route");
        check(link.getStyle().getClickEvent().getValue().equals("item:minecraft:emerald"), "Keep the recipe target");
        check(link.getStyle().getHoverEvent() == null, "Recipe links do not depend on item tooltips");
        Component missing = description("[Missing](item:questlog:missing_item)");
        check(missing.getString().equals("Missing"), "Unknown items remain readable");
        check(missing.getSiblings().get(1).getStyle().getClickEvent() == null, "Unknown items do not become links");
        check(missing.getSiblings().get(1).getStyle().getHoverEvent() == null, "Unknown items have no hover action");
        Component quest = description("[Next](quest:questlog:next)").getSiblings().get(1);
        check(quest.getStyle().getClickEvent().getValue().equals("questlog:next"), "Keep existing quest links");
        Component image = description("[Image](image:questlog:test.png)").getSiblings().get(1);
        check(image.getStyle().getHoverEvent().getAction() == HoverEvent.Action.SHOW_TEXT, "Keep existing hover images");
        System.out.println("Recipe links: " + checks + " checks passed (headless, no viewer opened).");
    }

    private static Component description(String text) {
        JsonObject data = new JsonObject();
        data.addProperty("title", "Test");
        data.addProperty("description", text);
        return new QuestDisplayData(data).getDescription();
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
        checks++;
    }
}
