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
import de.verdox.mccreativelab.wrapper.registry.OpenRegistry;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;

import java.util.logging.Logger;

public class ResourcePackMapper {
    private static final Logger LOGGER = Logger.getLogger(ResourcePackMapper.class.getSimpleName());

    private MCCReference<OpenRegistry<CustomGUIBuilder>> guiRegistry;
    private MCCReference<OpenRegistry<CustomHud>> hudRegistry;
    private MCCReference<OpenRegistry<ItemTextureData>> itemTextureRegistry;
    private MCCReference<OpenRegistry<ModelFile>> modelRegistry;
    private MCCReference<OpenRegistry<ShaderRendered>> shaderRenderedRegistry;
    private MCCReference<OpenRegistry<SoundData>> soundRegistry;
    private MCCReference<OpenRegistry<CustomMenu>> menuRegistry;
    private MCCReference<OpenRegistry<LanguageFile>> languageFileRegistry;
    private MCCReference<OpenRegistry<Font>> fontRegistry;

    void init() {
        LOGGER.info("Creating registries");
        guiRegistry = createRegistry("gui");
        hudRegistry = createRegistry("hud");
        itemTextureRegistry = createRegistry("item_textures");
        modelRegistry = createRegistry("models");
        shaderRenderedRegistry = createRegistry("shader_rendered");
        soundRegistry = createRegistry("sounds");
        menuRegistry = createRegistry("menu");
        languageFileRegistry = createRegistry("language_file");
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
        LOGGER.info("Clearing registries");
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

    private <T> MCCReference<OpenRegistry<T>> createRegistry(String name) {
        return MCCPlatform.getInstance().getRegistryStorage().createOpenRegistry(Key.key("mccreativelab", name));
    }

    private <T> MCCReference<T> register(T value, Key key, MCCReference<OpenRegistry<T>> registry) {
        MCCTypedKey<T> typedKey = MCCPlatform.getInstance().getTypedKeyFactory().getKey(key, registry.unwrapKey().get().key());
        return registry.get().register(typedKey, value);
    }

    private <T extends MCCKeyedWrapper> MCCReference<T> register(T value, MCCReference<OpenRegistry<T>> registry) {
        MCCTypedKey<T> typedKey = MCCPlatform.getInstance().getTypedKeyFactory().getKey(value.key(), registry.unwrapKey().get().key());
        return registry.get().register(typedKey, value);
    }

    private <T extends Keyed> MCCReference<T> register(T value, MCCReference<OpenRegistry<T>> registry) {
        MCCTypedKey<T> typedKey = MCCPlatform.getInstance().getTypedKeyFactory().getKey(value.key(), registry.unwrapKey().get().key());
        return registry.get().register(typedKey, value);
    }

    private <T> void clearRegistry(MCCReference<OpenRegistry<T>> reference) {
        MCCPlatform.getInstance().getRegistryStorage().deleteCustomMinecraftRegistry(reference.unwrapKey().get().key());
    }

    public MCCReference<OpenRegistry<CustomGUIBuilder>> getGuiRegistry() {
        return guiRegistry;
    }

    public MCCReference<OpenRegistry<CustomHud>> getHudRegistry() {
        return hudRegistry;
    }

    public MCCReference<OpenRegistry<ItemTextureData>> getItemTextureRegistry() {
        return itemTextureRegistry;
    }

    public MCCReference<OpenRegistry<ModelFile>> getModelRegistry() {
        return modelRegistry;
    }

    public MCCReference<OpenRegistry<ShaderRendered>> getShaderRenderedRegistry() {
        return shaderRenderedRegistry;
    }

    public MCCReference<OpenRegistry<SoundData>> getSoundRegistry() {
        return soundRegistry;
    }

    public MCCReference<OpenRegistry<CustomMenu>> getMenuRegistry() {
        return menuRegistry;
    }

    public MCCReference<OpenRegistry<LanguageFile>> getLanguageFileRegistry() {
        return languageFileRegistry;
    }

    public MCCReference<OpenRegistry<Font>> getFontRegistry() {
        return fontRegistry;
    }
}
