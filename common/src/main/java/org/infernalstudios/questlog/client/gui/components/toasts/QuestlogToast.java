package org.infernalstudios.questlog.client.gui.components.toasts;

import org.infernalstudios.questlog.client.gui.QuestlogGuiSet;
import org.infernalstudios.questlog.util.texture.Texture;

public abstract class QuestlogToast extends AbstractToast {

    @Override
    protected Texture getBackground() {
        return QuestlogGuiSet.DEFAULT.toast;
    }
}
