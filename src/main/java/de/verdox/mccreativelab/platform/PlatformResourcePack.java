package de.verdox.mccreativelab.platform;

import de.verdox.mccreativelab.Singleton;
import de.verdox.mccreativelab.generator.resourcepack.CustomResourcePack;

public interface PlatformResourcePack {
    Singleton<CustomResourcePack> INSTANCE = new Singleton<>(CustomResourcePack.class);
}
