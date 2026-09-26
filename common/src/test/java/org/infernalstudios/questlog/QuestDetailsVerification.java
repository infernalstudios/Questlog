package org.infernalstudios.questlog;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.Bootstrap;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.client.gui.screen.QuestDetails;
import org.infernalstudios.questlog.core.quests.Quest;
import org.infernalstudios.questlog.core.quests.display.QuestDisplayData;
import java.util.ArrayList;

/** Headless checks; visual layout and navigation still require in-game testing. */
public final class QuestDetailsVerification {
    private static int checks;

    public static void main(String[] args) {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        for (String value : new String[] {null, "null", "\"\"", "\" \\n \\t\"", "[]", "{\"text\":\"\"}"}) {
            QuestDisplayData display = display(value);
            check(!display.hasDetails(), "Absent/empty details must hide the button: " + value);
            check(display.getDetails().getStyle().isItalic(), "Missing-details fallback must be italic");
            check(display.getDescription().getString().equals("Overview"), "Do not replace the overview");
        }
        QuestDisplayData plain = display("\"Extra instructions\\nSecond line\"");
        check(plain.hasDetails(), "Plain details are available");
        check(plain.getDetails().getString().equals("Extra instructions\nSecond line"), "Preserve line breaks");
        check(plain.matchesSearch("EXTRA INSTRUCTIONS"), "Search includes details");
        QuestDisplayData object = display("{\"text\":\"Formatted details\",\"italic\":true}");
        check(object.getDetails().getString().equals("Formatted details"), "Parse a JSON text object");
        check(object.getDetails().getStyle().isItalic(), "Preserve JSON formatting");
        QuestDisplayData array = display("[{\"text\":\"First\"},{\"text\":\" second\"}]");
        check(array.getDetails().getString().equals("First second"), "Parse a JSON text array");
        QuestDisplayData linked = display("\"See [Next quest](quest:questlog:next).\"");
        check(linked.getDetails().getString().equals("See Next quest."), "Parse existing inline quest links");
        Component questLink = linked.getDetails().getSiblings().get(1);
        check(questLink.getStyle().isUnderlined(), "Keep existing inline link formatting");
        check(questLink.getStyle().getClickEvent().getValue().equals("questlog:next"), "Keep existing quest navigation");
        Component imageLink = display("\"[Picture](image:questlog:test.png)\"").getDetails().getSiblings().get(1);
        check(imageLink.getStyle().getHoverEvent().getAction() == HoverEvent.Action.SHOW_TEXT,
                "Keep existing image links");
        JsonObject translated = definition("\"questlog.test.details\"");
        translated.addProperty("translatable", true);
        check(new QuestDisplayData(translated).getDetails().getString().equals("Translated extra instructions"),
                "Resolve details through the player's language");
        JsonObject disabled = definition("\"Additional text\"");
        disabled.addProperty("disable_details_button", true);
        disabled.addProperty("details_open_by_default", true);
        QuestDisplayData flags = new QuestDisplayData(disabled);
        check(flags.hasDetails() && flags.isDetailsButtonDisabled() && flags.isDetailsOpenByDefault(),
                "Keep both explicit display flags");
        QuestDetails overview = new QuestDetails(null, quest(plain));
        check(overview.getPreviousScreen() == null, "Overview returns to the caller");
        QuestDetails defaultDetails = new QuestDetails(null, quest(flags));
        check(defaultDetails.getPreviousScreen() instanceof QuestDetails, "Default details returns to an overview");
        Quest updated = quest(flags);
        QuestDetails refreshed = defaultDetails.refreshed(updated);
        check(((QuestDetails) refreshed.getPreviousScreen()).quest == updated, "Refresh updates the hidden overview too");
        check(refreshed.quest == updated, "Refresh updates the additional page");
        JsonObject absentDefault = definition(null);
        absentDefault.addProperty("details_open_by_default", true);
        check(new QuestDetails(null, quest(new QuestDisplayData(absentDefault))).getPreviousScreen() == null,
                "Do not open an empty additional page by default");
        System.out.println("Quest details: " + checks + " checks passed (headless, no game launched).");
    }

    private static JsonObject definition(String detailsJson) {
        JsonObject json = new JsonObject();
        json.addProperty("title", "Test quest");
        json.addProperty("description", "Overview");
        if (detailsJson != null) json.add("details", JsonParser.parseString(detailsJson));
        return json;
    }

    private static QuestDisplayData display(String detailsJson) {
        return new QuestDisplayData(definition(detailsJson));
    }

    private static Quest quest(QuestDisplayData display) {
        return new Quest(display, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                new ResourceLocation("questlog", "test"), null, false, false);
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
        checks++;
    }
}
