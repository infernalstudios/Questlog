package org.infernalstudios.questlog.platform;

import org.infernalstudios.questlog.platform.services.IPlatformHelper;

public class Services {

    //? if fabric {
    /*public static final IPlatformHelper PLATFORM = new FabricPlatformHelper();
    *///?} elif forge {
    /*public static final IPlatformHelper PLATFORM = new ForgePlatformHelper();
    *///?} else {
    public static final IPlatformHelper PLATFORM = new NeoForgePlatformHelper();
    //?}
}
