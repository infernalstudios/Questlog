package org.infernalstudios.questlog.client.gui;

import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.util.ScrollbarTexture;
import org.infernalstudios.questlog.util.texture.Blittable;
import org.infernalstudios.questlog.util.texture.NineSliceTexture;
import org.infernalstudios.questlog.util.texture.Texture;

public class QuestlogGuiSet {

    public static final QuestlogGuiSet DEFAULT = new QuestlogGuiSet(
            ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "textures/gui/quest_page.png"),
            ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "textures/gui/quest_page.png"),
            ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "textures/gui/quest_peripherals.png"),
            275, 170, 166
    );

    public final ResourceLocation backgroundLoc;
    public final ResourceLocation rightPanelLoc;
    public final ResourceLocation peripheralLoc;
    public final ResourceLocation searchTabButtonsLoc;

    public final Blittable detailBackgroundLeft;
    public final Blittable detailBackgroundRight;
    public final Texture button;
    public final Texture buttonHovered;
    public final Texture buttonLong;
    public final Texture buttonLongHovered;
    public final Texture toast;
    public final Texture important;
    public final Texture panelHR;
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

    public final Texture expandButton;
    public final Texture expandButtonHovered;
    public final Texture condenseButton;
    public final Texture condenseButtonHovered;

    public final Texture hiddenButton;
    public final Texture hiddenButtonHovered;
    public final Texture visibleButton;
    public final Texture visibleButtonHovered;

    public QuestlogGuiSet(ResourceLocation backgroundLoc, ResourceLocation rightPanelLoc, ResourceLocation peripheralLoc, int leftPanelWidth, int rightPanelWidth, int panelHeight) {
        this.backgroundLoc = backgroundLoc;
        this.rightPanelLoc = rightPanelLoc;
        this.peripheralLoc = peripheralLoc;
        this.searchTabButtonsLoc = ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "textures/gui/questlog_search_tab_buttons.png");

        this.detailBackgroundLeft = new NineSliceTexture(backgroundLoc, leftPanelWidth, panelHeight, 375, 174, 275, 166, 1024, 512, 16, 16);
        this.detailBackgroundRight = new NineSliceTexture(rightPanelLoc, rightPanelWidth, panelHeight, 375, 174, 275, 166, 1024, 512, 16, 16);

        this.button = new Texture(peripheralLoc, 54, 18, 46, 65, 256, 256);
        this.buttonHovered = new Texture(peripheralLoc, 54, 18, 122, 65, 256, 256);
        this.buttonLong = new Texture(peripheralLoc, 88, 18, 12, 105, 256, 256);
        this.buttonLongHovered = new Texture(peripheralLoc, 88, 18, 122, 105, 256, 256);
        this.toast = new Texture(peripheralLoc, 173, 51, 81, 2, 256, 256);
        this.important = new Texture(peripheralLoc, 28, 36, 2, 2, 256, 256);
        this.panelHR = new Texture(peripheralLoc, 140, 9, 2, 157, 256, 256);
        this.smallHR = new Texture(peripheralLoc, 252, 9, 2, 135, 256, 256);
        this.bigHR = new Texture(peripheralLoc, 252, 9, 2, 146, 256, 256);
        this.scrollbar = new ScrollbarTexture(
                new Texture(peripheralLoc, 28, 36, 32, 2, 256, 256),
                new Texture(peripheralLoc, 16, 1, 62, 20, 256, 256),
                new Texture(peripheralLoc, 16, 1, 62, 19, 256, 256),
                new Texture(peripheralLoc, 16, 1, 62, 21, 256, 256)
        );

        this.searchTabMinimized = new Texture(this.searchTabButtonsLoc, 58, 55, 2, 2, 256, 256);
        this.searchTabMinimizedHovered = new Texture(this.searchTabButtonsLoc, 58, 55, 62, 2, 256, 256);
        this.searchTabExpanded = new Texture(this.searchTabButtonsLoc, 252, 55, 2, 59, 256, 256);
        this.searchTabExpandedHovered = new Texture(this.searchTabButtonsLoc, 252, 55, 2, 116, 256, 256);

        this.arrowLeft = new Texture(this.searchTabButtonsLoc, 28, 27, 2, 173, 256, 256);
        this.arrowLeftHovered = new Texture(this.searchTabButtonsLoc, 28, 27, 32, 173, 256, 256);
        this.arrowRight = new Texture(this.searchTabButtonsLoc, 28, 27, 2, 202, 256, 256);
        this.arrowRightHovered = new Texture(this.searchTabButtonsLoc, 28, 27, 32, 202, 256, 256);

        this.tabSecondary = new Texture(this.searchTabButtonsLoc, 54, 32, 62, 174, 256, 256);
        this.tabSecondaryActive = new Texture(this.searchTabButtonsLoc, 54, 39, 62, 208, 256, 256);
        this.tabMain = new Texture(this.searchTabButtonsLoc, 54, 32, 118, 174, 256, 256);
        this.tabMainActive = new Texture(this.searchTabButtonsLoc, 54, 39, 118, 208, 256, 256);

        this.expandButton = new Texture(this.searchTabButtonsLoc, 26, 26, 122, 2, 256, 256);
        this.expandButtonHovered = new Texture(this.searchTabButtonsLoc, 26, 26, 150, 2, 256, 256);
        this.condenseButton = new Texture(this.searchTabButtonsLoc, 26, 26, 122, 30, 256, 256);
        this.condenseButtonHovered = new Texture(this.searchTabButtonsLoc, 26, 26, 150, 30, 256, 256);

        this.hiddenButton = new Texture(this.searchTabButtonsLoc, 26, 26, 180, 2, 256, 256);
        this.hiddenButtonHovered = new Texture(this.searchTabButtonsLoc, 26, 26, 208, 2, 256, 256);
        this.visibleButton = new Texture(this.searchTabButtonsLoc, 26, 26, 180, 30, 256, 256);
        this.visibleButtonHovered = new Texture(this.searchTabButtonsLoc, 26, 26, 208, 30, 256, 256);
    }
}