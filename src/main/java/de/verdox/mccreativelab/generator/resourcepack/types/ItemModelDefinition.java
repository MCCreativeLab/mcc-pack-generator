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
    private final Key backingModel;

    public ItemModelDefinition(Key namespacedKey) {
        this(namespacedKey, Key.key(namespacedKey.namespace(), "item/" + namespacedKey.value()));
    }

    public ItemModelDefinition(Key namespacedKey, Key backingModel) {
        super(namespacedKey);
        this.backingModel = backingModel;
    }

    @Override
    public void installResourceToPack(CustomResourcePack customPack) throws IOException {
        JsonObject items = JsonObjectBuilder.create()
                .add("model", JsonObjectBuilder.create()
                        .add("type", "minecraft:model")
                        .add("model", backingModel.asString())
                ).build();
        AssetUtil.createJsonAssetAndInstall(items, customPack, key(), ResourcePackAssetTypes.ITEMS);
    }
}
