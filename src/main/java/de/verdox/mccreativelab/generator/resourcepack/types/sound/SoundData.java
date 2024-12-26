package de.verdox.mccreativelab.generator.resourcepack.types.sound;

import de.verdox.mccreativelab.generator.Asset;
import de.verdox.mccreativelab.generator.resourcepack.AssetBasedResourcePackResource;
import de.verdox.mccreativelab.generator.resourcepack.CustomResourcePack;
import de.verdox.mccreativelab.generator.resourcepack.ResourcePackAssetTypes;
import de.verdox.mccreativelab.generator.resourcepack.ResourcePackResource;
import de.verdox.mccreativelab.util.AudioConverter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class SoundData extends ResourcePackResource {
    public static final Logger LOGGER = Logger.getLogger(SoundData.class.getName());
    private final Map<Key, SoundVariant> soundVariants = new HashMap<>();
    private boolean alreadyInstalled;
    private final boolean replace;
    private final String subtitle;
    private final Set<File> convertedFile = new HashSet<>();

    public SoundData(Key namespacedKey, boolean replace, String subtitle) {
        super(namespacedKey);
/*        if(!namespacedKey.namespace().equals("minecraft"))
            throw new IllegalArgumentException("Sounds can only be installed in minecraft namespace");*/
        this.replace = replace;
        this.subtitle = subtitle;
    }

    public SoundData withSoundVariant(Key namespacedKey, Asset<CustomResourcePack> soundAsset, float volume, float pitch) {
        return withSoundVariant(namespacedKey, soundAsset, "ogg", volume, pitch);
    }

    public SoundData withSoundVariant(Key namespacedKey, Asset<CustomResourcePack> soundAsset, String audioFileEnding, float volume, float pitch) {
        if (alreadyInstalled)
            throw new IllegalStateException("Cannot add more sounds during runtime.");
        soundVariants.put(namespacedKey, new SoundVariant(namespacedKey, soundAsset, audioFileEnding, volume, pitch));
        return this;
    }

    public Sound asSound(Sound.Source source, float volume, float pitch) {
        return Sound.sound(key(), source, volume, pitch);
    }

    @Override
    public void beforeResourceInstallation(CustomResourcePack customPack) throws IOException {
        for (Key namespacedKey : soundVariants.keySet()) {
            SoundVariant soundVariant = soundVariants.get(namespacedKey);
            if (soundVariant.audioFileEnding.contains("ogg"))
                continue;
            LOGGER.info(soundVariant.namespacedKey.asString() + " needs conversion from " + soundVariant.audioFileEnding + " to ogg.");


            File installedAssetWithWrongFormat = soundVariant.soundAsset().installAsset(customPack, namespacedKey, ResourcePackAssetTypes.SOUNDS, soundVariant.audioFileEnding);
            File tempOggConvertedFile = new File("audioConverterCache/" + UUID.randomUUID() + ".ogg");
            tempOggConvertedFile.getParentFile().mkdirs();
            AudioConverter.anyToOgg(installedAssetWithWrongFormat, tempOggConvertedFile);
            convertedFile.add(tempOggConvertedFile);
            AssetBasedResourcePackResource assetBasedResourcePackResource = new AssetBasedResourcePackResource(namespacedKey, new Asset<>(() -> {
                try {
                    return new FileInputStream(tempOggConvertedFile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }), ResourcePackAssetTypes.SOUNDS, "ogg");
            customPack.register(assetBasedResourcePackResource);
            installedAssetWithWrongFormat.delete();
        }
    }

    @Override
    public void installResourceToPack(CustomResourcePack customPack) throws IOException {
        alreadyInstalled = true;
        for (Key namespacedKey : soundVariants.keySet()) {
            SoundVariant soundVariant = soundVariants.get(namespacedKey);
            if (soundVariant.audioFileEnding.contains("ogg"))
                soundVariant.soundAsset().installAsset(customPack, namespacedKey, ResourcePackAssetTypes.SOUNDS, soundVariant.audioFileEnding);
        }
    }

    @Override
    public void afterResourceInstallation(CustomResourcePack customPack) throws IOException {
        FileUtils.deleteDirectory(new File("audioConverterCache"));
/*        for (File file : convertedFile) {
            file.delete();

            Bukkit.getLogger().info("Deleting converted file " + convertedFile);
        }*/
    }

    public Map<Key, SoundVariant> getSoundVariants() {
        return soundVariants;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public boolean isReplace() {
        return replace;
    }

    public record SoundVariant(Key namespacedKey, Asset<CustomResourcePack> soundAsset,
                               String audioFileEnding, float volume, float pitch) {
    }
}
