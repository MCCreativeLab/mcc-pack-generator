package de.verdox.mccreativelab.generator.resourcepack;

import de.verdox.mccreativelab.generator.resourcepack.types.sound.SoundData;
import de.verdox.vserializer.util.gson.JsonArrayBuilder;
import de.verdox.vserializer.util.gson.JsonObjectBuilder;
import de.verdox.mccreativelab.util.io.AssetUtil;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashSet;
import java.util.Set;

public class SoundFile extends ResourcePackResource {
    private final Set<SoundData> soundDataSet = new HashSet<>();

    @ApiStatus.Internal
    SoundFile(Key namespacedKey) {
        super(namespacedKey);
    }

    @ApiStatus.Internal
    void addSoundData(SoundData soundData) {
        soundDataSet.add(soundData);
    }

    @Override
    public void installResourceToPack(CustomResourcePack customPack) {
        JsonObjectBuilder soundsFileJson = JsonObjectBuilder.create();
        for (SoundData soundData : soundDataSet) {
            var soundObject = JsonObjectBuilder.create();
            var soundVariants = JsonArrayBuilder.create();
            soundData.getSoundVariants().forEach((namespacedKey, soundVariant) -> {
                var variant = JsonObjectBuilder.create();
                variant.add("name", namespacedKey.value());
                variant.add("pitch", soundVariant.pitch());
                variant.add("volume", soundVariant.volume());
                soundVariants.add(variant);
            });

            soundObject.add("sounds", soundVariants);
            soundObject.add("replace", soundData.isReplace());
            soundObject.add("subtitle", soundData.getSubtitle());

            soundsFileJson.add(soundData.key().value(), soundObject);
        }

        AssetUtil.createJsonAssetAndInstall(soundsFileJson.build(), customPack, key(), ResourcePackAssetTypes.SOUND_FILE);
    }
}
