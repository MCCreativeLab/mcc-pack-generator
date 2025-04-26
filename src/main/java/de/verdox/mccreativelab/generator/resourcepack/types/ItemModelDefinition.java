package de.verdox.mccreativelab.generator.resourcepack.types;

import com.google.gson.JsonObject;
import de.verdox.mccreativelab.generator.resourcepack.CustomResourcePack;
import de.verdox.mccreativelab.generator.resourcepack.ResourcePackAssetTypes;
import de.verdox.mccreativelab.generator.resourcepack.ResourcePackResource;
import de.verdox.mccreativelab.util.io.AssetUtil;
import de.verdox.vserializer.util.gson.JsonObjectBuilder;
import net.kyori.adventure.key.Key;

import java.io.IOException;

public class ItemModelDefinition extends ResourcePackResource {
    public ItemModelDefinition(Key namespacedKey) {
        super(namespacedKey);
    }

    @Override
    public void installResourceToPack(CustomResourcePack customPack) throws IOException {
        JsonObject items = JsonObjectBuilder.create()
                .add("model", JsonObjectBuilder.create()
                        .add("type", "minecraft:model")
                        .add("model", key().asString())
                ).build();
        AssetUtil.createJsonAssetAndInstall(items, customPack, key(), ResourcePackAssetTypes.ITEMS);
    }
}
