package de.verdox.mccreativelab.generator.resourcepack;

import de.verdox.mccreativelab.generator.Resource;
import de.verdox.mccreativelab.generator.resourcepack.types.hud.CustomHud;
import de.verdox.mccreativelab.generator.resourcepack.types.ItemTextureData;
import de.verdox.mccreativelab.generator.resourcepack.types.ModelFile;
import de.verdox.mccreativelab.generator.resourcepack.types.rendered.ShaderRendered;
import de.verdox.mccreativelab.generator.resourcepack.types.font.Font;
import de.verdox.mccreativelab.generator.resourcepack.types.gui.CustomGUIBuilder;
import de.verdox.mccreativelab.generator.resourcepack.types.lang.LanguageFile;
import de.verdox.mccreativelab.generator.resourcepack.types.menu.CustomMenu;
import de.verdox.mccreativelab.generator.resourcepack.types.sound.SoundData;
import de.verdox.mccreativelab.wrapper.MCCKeyedWrapper;
import de.verdox.mccreativelab.wrapper.platform.MCCPlatform;
import de.verdox.mccreativelab.wrapper.registry.MCCReference;
import de.verdox.mccreativelab.wrapper.registry.MCCRegistry;
import de.verdox.mccreativelab.wrapper.registry.MCCTypedKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;

public class ResourcePackMapper {
    private MCCReference<MCCRegistry<CustomGUIBuilder>> guiRegistry;
    private MCCReference<MCCRegistry<CustomHud>> hudRegistry;
    private MCCReference<MCCRegistry<ItemTextureData>> itemTextureRegistry;
    private MCCReference<MCCRegistry<ModelFile>> modelRegistry;
    private MCCReference<MCCRegistry<ShaderRendered>> shaderRenderedRegistry;
    private MCCReference<MCCRegistry<SoundData>> soundRegistry;
    private MCCReference<MCCRegistry<CustomMenu>> menuRegistry;
    private MCCReference<MCCRegistry<LanguageFile>> languageFileRegistry;
    private MCCReference<MCCRegistry<Font>> fontRegistry;

    void init() {
        guiRegistry = createRegistry("gui");
        hudRegistry = createRegistry("hud");
        itemTextureRegistry = createRegistry("itemTextures");
        modelRegistry = createRegistry("models");
        shaderRenderedRegistry = createRegistry("shaderRendered");
        soundRegistry = createRegistry("sounds");
        menuRegistry = createRegistry("menu");
        languageFileRegistry = createRegistry("languageFile");
        fontRegistry = createRegistry("font");
    }

    void register(Resource<CustomResourcePack> resource) {
        if (resource instanceof CustomGUIBuilder customGUIBuilder)
            register(customGUIBuilder, guiRegistry);
        else if (resource instanceof CustomHud customHud)
            register(customHud, hudRegistry);
        else if (resource instanceof ItemTextureData itemTextureData)
            register(itemTextureData, itemTextureRegistry);
/*        else if(resource instanceof ModelFile modelFile)
            modelsRegistry.register(modelFile.key(), modelFile);*/
        else if (resource instanceof ShaderRendered shaderRendered)
            register(shaderRendered, shaderRenderedRegistry);
        else if (resource instanceof CustomMenu customMenu)
            register(customMenu, menuRegistry);
        else if (resource instanceof LanguageFile languageFile)
            register(languageFile, languageFileRegistry);
        else if (resource instanceof Font font)
            register(font, fontRegistry);
        else if (resource instanceof SoundData soundData)
            register(soundData, soundRegistry);
    }

    void clear() {
        clearRegistry(guiRegistry);
        clearRegistry(hudRegistry);
        clearRegistry(itemTextureRegistry);
        clearRegistry(modelRegistry);
        clearRegistry(shaderRenderedRegistry);
        clearRegistry(soundRegistry);
        clearRegistry(menuRegistry);
        clearRegistry(languageFileRegistry);
        clearRegistry(fontRegistry);
    }

    private <T> MCCReference<MCCRegistry<T>> createRegistry(String name) {
        return MCCPlatform.getInstance().getRegistryStorage().createMinecraftRegistry(Key.key("mcc", name));
    }

    private <T> MCCReference<T> register(T value, Key key, MCCReference<MCCRegistry<T>> registry) {
        MCCTypedKey<T> typedKey = MCCPlatform.getInstance().getTypedKeyFactory().getKey(key, registry.unwrapKey().get().key());
        return registry.get().register(typedKey, value);
    }

    private <T extends MCCKeyedWrapper> MCCReference<T> register(T value, MCCReference<MCCRegistry<T>> registry) {
        MCCTypedKey<T> typedKey = MCCPlatform.getInstance().getTypedKeyFactory().getKey(value.key(), registry.unwrapKey().get().key());
        return registry.get().register(typedKey, value);
    }

    private <T extends Keyed> MCCReference<T> register(T value, MCCReference<MCCRegistry<T>> registry) {
        MCCTypedKey<T> typedKey = MCCPlatform.getInstance().getTypedKeyFactory().getKey(value.key(), registry.unwrapKey().get().key());
        return registry.get().register(typedKey, value);
    }

    private <T> void clearRegistry(MCCReference<MCCRegistry<T>> reference) {
        // TODO: MCCPlatform.getInstance().getRegistryStorage().deleteCustomMinecraftRegistry(reference.unwrapKey().get().key());
    }
}
