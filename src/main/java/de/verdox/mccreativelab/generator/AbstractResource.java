package de.verdox.mccreativelab.generator;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public abstract class AbstractResource<C extends CustomPack<C>> implements Resource<C>  {
    private final Key namespacedKey;

    public AbstractResource(Key namespacedKey) {
        this.namespacedKey = namespacedKey;
    }

    @Override
    public void onRegister(C customPack) {

    }

    @Override
    public void beforeResourceInstallation(C customPack) throws IOException {

    }

    @Override
    public void afterResourceInstallation(C customPack) throws IOException {

    }

    @Override
    public final @NotNull Key key() {
        return namespacedKey;
    }
}
