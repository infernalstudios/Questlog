package org.infernalstudios.questlog.client.gui;

import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.util.ScrollbarTexture;
import org.infernalstudios.questlog.util.texture.Texture;

public class QuestlogGuiSet {

    public static final QuestlogGuiSet DEFAULT = new QuestlogGuiSet(
            ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "textures/gui/quest_page.png"),
            ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "textures/gui/quest_peripherals.png")
    );

    public final ResourceLocation backgroundLoc;
    public final ResourceLocation peripheralLoc;
    public final ResourceLocation searchTabButtonsLoc;

    public final Texture detailBackground;
    public final Texture button;
    public final Texture buttonHovered;
    public final Texture buttonLong;
    public final Texture buttonLongHovered;
    public final Texture toast;
    public final Texture important;
    public final Texture smallHR;
    public final Texture bigHR;
    public final ScrollbarTexture scrollbar;

    public final Texture searchTabMinimized;
    public final Texture searchTabMinimizedHovered;
    public final Texture searchTabExpanded;
    public final Texture searchTabExpandedHovered;
    public final Texture arrowLeft;
    public final Texture arrowLeftHovered;
    public final Texture arrowRight;
    public final Texture arrowRightHovered;
    public final Texture tabSecondary;
    public final Texture tabSecondaryActive;
    public final Texture tabMain;
    public final Texture tabMainActive;

    public QuestlogGuiSet(ResourceLocation backgroundLoc, ResourceLocation peripheralLoc) {
        this.backgroundLoc = backgroundLoc;
        this.peripheralLoc = peripheralLoc;
        this.searchTabButtonsLoc = ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "textures/gui/questlog_search_tab_buttons.png");

        this.detailBackground = new Texture(backgroundLoc, 1024, 512, 0, 0, 1024, 512);
        this.button = new Texture(peripheralLoc, 74, 38, 36, 55, 256, 256);
        this.buttonHovered = new Texture(peripheralLoc, 74, 38, 112, 55, 256, 256);
        this.buttonLong = new Texture(peripheralLoc, 108, 38, 2, 95, 256, 256);
        this.buttonLongHovered = new Texture(peripheralLoc, 108, 38, 112, 95, 256, 256);
        this.toast = new Texture(peripheralLoc, 173, 51, 81, 2, 256, 256);
        this.important = new Texture(peripheralLoc, 28, 36, 2, 2, 256, 256);
        this.smallHR = new Texture(peripheralLoc, 252, 9, 2, 135, 256, 256);
        this.bigHR = new Texture(peripheralLoc, 252, 9, 2, 146, 256, 256);
        this.scrollbar = new ScrollbarTexture(
                new Texture(peripheralLoc, 28, 36, 32, 2, 256, 256),
                new Texture(peripheralLoc, 16, 1, 62, 20, 256, 256),
                new Texture(peripheralLoc, 16, 1, 62, 19, 256, 256),
                new Texture(peripheralLoc, 16, 1, 62, 21, 256, 256)
        );

        this.searchTabMinimized = new Texture(this.searchTabButtonsLoc, 28, 18, 17, 21, 256, 256);
        this.searchTabMinimizedHovered = new Texture(this.searchTabButtonsLoc, 28, 18, 77, 21, 256, 256);
        this.searchTabExpanded = new Texture(this.searchTabButtonsLoc, 193, 18, 32, 78, 256, 256);
        this.searchTabExpandedHovered = new Texture(this.searchTabButtonsLoc, 193, 18, 32, 135, 256, 256);

        this.arrowLeft = new Texture(this.searchTabButtonsLoc, 8, 13, 11, 180, 256, 256);
        this.arrowLeftHovered = new Texture(this.searchTabButtonsLoc, 8, 13, 41, 180, 256, 256);
        this.arrowRight = new Texture(this.searchTabButtonsLoc, 8, 13, 12, 209, 256, 256);
        this.arrowRightHovered = new Texture(this.searchTabButtonsLoc, 8, 13, 42, 209, 256, 256);

        this.tabSecondary = new Texture(this.searchTabButtonsLoc, 28, 23, 75, 174, 256, 256);
        this.tabSecondaryActive = new Texture(this.searchTabButtonsLoc, 28, 29, 75, 208, 256, 256);
        this.tabMain = new Texture(this.searchTabButtonsLoc, 28, 23, 131, 174, 256, 256);
        this.tabMainActive = new Texture(this.searchTabButtonsLoc, 28, 29, 131, 208, 256, 256);
    }
}