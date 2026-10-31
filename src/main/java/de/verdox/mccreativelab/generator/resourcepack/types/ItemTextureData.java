package de.verdox.mccreativelab.generator.resourcepack.types;

import com.google.common.hash.Hashing;
import com.google.gson.JsonObject;
import de.verdox.mccreativelab.generator.Asset;
import de.verdox.mccreativelab.generator.resourcepack.CustomResourcePack;
import de.verdox.mccreativelab.generator.resourcepack.ResourcePackAssetTypes;
import de.verdox.mccreativelab.generator.resourcepack.ResourcePackResource;
import de.verdox.mccreativelab.wrapper.annotations.MCCRequireVanillaElement;
import de.verdox.mccreativelab.wrapper.block.MCCBlockFace;
import de.verdox.mccreativelab.wrapper.item.MCCItemStack;
import de.verdox.mccreativelab.wrapper.item.MCCItemType;
import de.verdox.mccreativelab.wrapper.typed.MCCDataComponentTypes;
import de.verdox.vserializer.util.gson.JsonArrayBuilder;
import de.verdox.vserializer.util.gson.JsonObjectBuilder;
import de.verdox.vserializer.util.gson.JsonUtil;
import de.verdox.mccreativelab.util.io.AssetUtil;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class ItemTextureData extends ResourcePackResource {
    public static final Logger LOGGER = Logger.getLogger(ItemTextureData.class.getName());

    /**
     * 1.21.4+ removed model overrides; custom_model_data only changes appearance
     * when the vanilla item's definition in assets/minecraft/items dispatches on it.
     * Collect every fake-item texture per vanilla base material so each used base
     * material gets one aggregated definition (covers stacks without an explicit
     * item_model component, e.g. recipe ingredient example stacks in the recipe book).
     */
    private static final Map<Key, List<ItemTextureData>> VANILLA_BASE_COLLECTOR = new ConcurrentHashMap<>();
    private static final Map<Key, Set<ItemTextureData>> PROCESSED_HOOKS = new ConcurrentHashMap<>();
    private static final Set<Key> WRITTEN_VANILLA_OVERRIDES = ConcurrentHashMap.newKeySet();
    private final MCCItemType material;
    private final int customModelData;
    private final Asset<CustomResourcePack> pngFile;
    private final @Nullable ModelType modelType;
    private final boolean useVanillaTexture;
    @Nullable
    private Consumer<MCCItemStack> stackConsumer;

    public ItemTextureData(@NotNull Key namespacedKey,
                           @NotNull @MCCRequireVanillaElement MCCItemType material,
                           int customModelData,
                           @Nullable Asset<CustomResourcePack> pngFile,
                           @Nullable ModelType modelType, boolean useVanillaTexture) {
        super(namespacedKey);
        material.requireVanilla();
        this.material = material;
        this.customModelData = customModelData;
        this.pngFile = pngFile;
        this.modelType = modelType;
        this.useVanillaTexture = useVanillaTexture;
    }

    public ItemTextureData(@NotNull Key namespacedKey,
                           @NotNull @MCCRequireVanillaElement MCCItemType material,
                           @Nullable Asset<CustomResourcePack> pngFile,
                           @Nullable ModelType modelType, boolean useVanillaTexture) {
        this(namespacedKey, material, Math.abs(Hashing.sha256().hashString(namespacedKey.asString(), StandardCharsets.UTF_8).asInt()), pngFile, modelType, useVanillaTexture);
    }

    public ItemTextureData(@NotNull Key namespacedKey,
                           @NotNull @MCCRequireVanillaElement MCCItemType material,
                           int customModelData,
                           @Nullable Asset<CustomResourcePack> pngFile,
                           @Nullable ModelType modelType) {
        this(namespacedKey, material, customModelData, pngFile, modelType, false);
    }

    public ItemTextureData(@NotNull Key namespacedKey,
                           @NotNull @MCCRequireVanillaElement MCCItemType material,
                           @Nullable Asset<CustomResourcePack> pngFile,
                           @Nullable ModelType modelType) {
        this(namespacedKey, material, pngFile, modelType, false);
    }

    public MCCItemStack createItem() {
        var stack = material.createItem();
        if (stackConsumer != null) {
            stackConsumer.accept(stack);
        }
        if (!useVanillaTexture) {
            stack.components().edit(MCCDataComponentTypes.ITEM_MODEL.get(), editor -> editor.set(key()));
        }
        stack.components().edit(MCCDataComponentTypes.ITEM_NAME.get(), componentMCCDataComponentEditor -> componentMCCDataComponentEditor.set(Component.translatable("item." + key().namespace() + "." + key().value())));
        return stack;
    }

    public void setStackConsumer(@Nullable Consumer<MCCItemStack> stackConsumer) {
        this.stackConsumer = stackConsumer;
    }

    @Override
    public void beforeResourceInstallation(CustomResourcePack customPack) throws IOException {
        LOGGER.info("DEBUG before hook " + key() + " vanillaTexture=" + useVanillaTexture + " cmd=" + customModelData);
        ItemModelDefinition itemModelDefinition = new ItemModelDefinition(key());
        customPack.register(itemModelDefinition);
        if (!useVanillaTexture)
            VANILLA_BASE_COLLECTOR
                    .computeIfAbsent(material.key(), k -> new CopyOnWriteArrayList<>())
                    .add(this);
        else
            LOGGER.info("Vanilla override collector skipped for " + key() + " (useVanillaTexture=true)");
    }

    @Override
    public void afterResourceInstallation(CustomResourcePack customPack) throws IOException {
        LOGGER.info("DEBUG after hook " + key() + " vanillaTexture=" + useVanillaTexture
                + " collected=" + VANILLA_BASE_COLLECTOR.size());
        if (useVanillaTexture) return;
        Key materialKey = material.key();
        List<ItemTextureData> registered = VANILLA_BASE_COLLECTOR.get(materialKey);
        if (registered == null || !registered.contains(this)) {
            LOGGER.warning("Vanilla override hook for " + key() + " on base " + materialKey
                    + " skipped (collected=" + (registered == null ? "none" : registered.size()) + ")");
            return;
        }
        // Count processed hooks instead of draining the shared list: removing each
        // texture left the last writer with an EMPTY snapshot (VANILLA_BASE_COLLECTOR
        // stores the same list object), so the aggregated definition was never written.
        // The last hook for a base material writes the full collected list once.
        Set<ItemTextureData> processed =
                PROCESSED_HOOKS.computeIfAbsent(materialKey, k -> ConcurrentHashMap.newKeySet());
        if (!processed.add(this)) return;
        if (processed.size() >= registered.size() && WRITTEN_VANILLA_OVERRIDES.add(materialKey)) {
            List<ItemTextureData> textures = VANILLA_BASE_COLLECTOR.remove(materialKey);
            PROCESSED_HOOKS.remove(materialKey);
            writeVanillaItemModelOverride(customPack, materialKey, textures);
        }
    }

    private static void writeVanillaItemModelOverride(CustomResourcePack customPack, Key materialKey,
                                                      List<ItemTextureData> textures) throws IOException {
        if (textures == null || textures.isEmpty()) return;
        Map<Integer, Key> modelsByCmd = new TreeMap<>();
        for (ItemTextureData texture : textures) {
            int cmd = texture.customModelData;
            // Vanilla stacks without custom_model_data dispatch as 0; never capture them.
            if (cmd <= 1) {
                LOGGER.warning("Fake item " + texture.key() + " has custom model data " + cmd
                        + " which collides with vanilla stacks; skipping vanilla item override entry");
                continue;
            }
            Key model = Key.key(texture.key().namespace(), "item/" + texture.key().value());
            Key previous = modelsByCmd.putIfAbsent(cmd, model);
            if (previous != null && !previous.equals(model))
                LOGGER.severe("Custom model data collision on base item " + materialKey + ": " + previous
                        + " vs " + model + " (cmd " + cmd + "); keeping " + previous);
        }
        if (modelsByCmd.isEmpty()) return;

        // range_dispatch selects the highest threshold <= the stack's cmd float.
        // Threshold cmd-1 therefore captures exactly this cmd (cmd >= 2) and nothing
        // higher, because the next registered entry takes over at its own cmd-1.
        JsonArrayBuilder entries = JsonArrayBuilder.create();
        for (Map.Entry<Integer, Key> entry : modelsByCmd.entrySet()) {
            entries.add(JsonObjectBuilder.create()
                    .add("threshold", entry.getKey() - 1)
                    .add("model", JsonObjectBuilder.create()
                            .add("type", "minecraft:model")
                            .add("model", entry.getValue().asString()))
                    .build());
        }
        JsonObject definition = JsonObjectBuilder.create()
                .add("model", JsonObjectBuilder.create()
                        .add("type", "minecraft:range_dispatch")
                        .add("property", "minecraft:custom_model_data")
                        .add("index", 0)
                        .add("entries", entries)
                        .add("fallback", JsonObjectBuilder.create()
                                .add("type", "minecraft:model")
                                .add("model", materialKey.namespace() + ":" + materialKey.value()))
                        .build())
                .build();
        AssetUtil.createJsonAssetAndInstall(definition, customPack, materialKey, ResourcePackAssetTypes.ITEMS);
        LOGGER.info("Wrote vanilla item model definition for " + materialKey + " with "
                + modelsByCmd.size() + " custom model entries");
    }

    @Override
    public void installResourceToPack(CustomResourcePack customPack) throws IOException {
        var hasCustomTexture = pngFile != null;

        if (hasCustomTexture) {
            pngFile.installAsset(customPack, Key.key(key().namespace() + ":item/" + key().value()), ResourcePackAssetTypes.TEXTURES, "png");
        }

        // Vanilla-only items have no custom PNG. Reference their native model
        // instead of creating a generated model pointing at a nonexistent texture.
        if (useVanillaTexture) {
            AssetUtil.createJsonAssetAndInstall(
                JsonObjectBuilder.create().add("parent", material.key().namespace() + ":item/" + material.key().value()).build(),
                customPack, Key.key(key().namespace(), "item/" + key().value()), ResourcePackAssetTypes.MODELS);
        } else {
            createModelFile(customPack);
        }
    }

    private void createModelFile(CustomResourcePack customPack) {
        JsonObject jsonToWriteToFile = createModelJson(this.material, Key.key(key().namespace() + ":item/" + key().value()), modelType);
        AssetUtil.createJsonAssetAndInstall(jsonToWriteToFile, customPack, Key.key(key().namespace() + ":item/" + key().value()), ResourcePackAssetTypes.MODELS);

/*        JsonObject items = JsonObjectBuilder.create()
                        .add("model", JsonObjectBuilder.create()
                                .add("type", "minecraft:model")
                                .add("model", key().asString())
                        ).build();
        AssetUtil.createJsonAssetAndInstall(items, customPack, key(), ResourcePackAssetTypes.ITEMS);*/
    }

    private static JsonObject createModelJson(@MCCRequireVanillaElement MCCItemType material, Key key, @Nullable ModelType modelType) {
        material.requireVanilla();
        JsonObject jsonToWriteToFile = new JsonObject();
        if (modelType != null)
            modelType.modelCreator().accept(key, jsonToWriteToFile);
        else {
            if (isHandheldItem(material.key()))
                ModelType.HAND_HELD.modelCreator.accept(key, jsonToWriteToFile);
            else
                ModelType.GENERATED_ITEM.modelCreator.accept(key, jsonToWriteToFile);
        }
        return jsonToWriteToFile;
    }

    private static boolean isHandheldItem(Key material) {
        return material.value().contains("sword") || material.value().contains("axe") || material.value().contains("hoe") || material.value().contains("shovel") || material.value().contains("fishing_rod") || material.value().equals("stick");
    }

    public record ModelType(String modelName, BiConsumer<Key, JsonObject> modelCreator) {

        public static ModelType fromJsonAsset(Asset<CustomResourcePack> asset, Consumer<JsonObject> consumer) {
            try {
                JsonObject jsonObject = JsonUtil.readJsonInputStream(asset.assetInputStream().get());
                return new ModelType("", (namespacedKey, jsonObject1) -> {
                    jsonObject1.asMap().putAll(jsonObject.asMap());
                    consumer.accept(jsonObject1);
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public static ModelType createJsonForModel(Consumer<JsonObject> consumer) {
            return new ModelType("", (namespacedKey, jsonObject1) -> consumer.accept(jsonObject1));
        }

        public static ModelType modifyExistingModelType(ModelType modelType, BiConsumer<Key, JsonObject> modelCreator) {
            return new ModelType(modelType.modelName, (namespacedKey, jsonObject) -> {
                modelType.modelCreator().accept(namespacedKey, jsonObject);
                modelCreator.accept(namespacedKey, jsonObject);
            });
        }

        public static ModelType createModelForBlockItem(String modelName, Key blockModel) {
            return new ModelType(modelName, (namespacedKey, jsonObject) ->
                    JsonObjectBuilder.create(jsonObject).add("parent", blockModel.toString())
                            .build());
        }

        public static ModelType createFullCubeModel(Key resourceKey) {
            return new ModelType("minecraft:block/cube_all", (namespacedKey, jsonObject) ->
                    JsonObjectBuilder.create().add("parent", "minecraft:block/cube_all")
                            .add("textures", JsonObjectBuilder.create().add("all", resourceKey.toString()))
                            .build());
        }

        public static ModelType createFullCubeWithSingleTexture(Key textureKey) {
            return createFullCubeWithSeparateTextures(Map.of(MCCBlockFace.UP, textureKey, MCCBlockFace.DOWN, textureKey, MCCBlockFace.NORTH, textureKey, MCCBlockFace.EAST, textureKey, MCCBlockFace.SOUTH, textureKey, MCCBlockFace.WEST, textureKey));
        }

        public static ModelType createFullCubeWithSeparateTextures(Map<MCCBlockFace, ? extends Keyed> texturesPerBlockFace) {
            var textures = JsonObjectBuilder.create();
            texturesPerBlockFace.forEach((blockFace, itemTextureData) -> {
                textures.add(blockFace.name().toLowerCase(Locale.ROOT), itemTextureData.key().toString());
                textures.add("particle", itemTextureData.key().toString());
            });

            return new ModelType("", (namespacedKey, jsonObject) ->
                    JsonObjectBuilder.create(jsonObject).add("parent", "block/cube")
                            .add("textures", textures));

        }

        public static ModelType createOnlyOneSideTextureOfCube(MCCBlockFace face) {

            String faceName;
            if (face.equals(MCCBlockFace.UP) || face.equals(MCCBlockFace.DOWN))
                faceName = face.name().toLowerCase(Locale.ROOT);
            else
                faceName = face.getOppositeFace().name().toLowerCase(Locale.ROOT);
            int posX = 0;
            int posY = 0;
            int posZ = 0;

            int sizeX = 0;
            int sizeY = 0;
            int sizeZ = 0;

            if (face == MCCBlockFace.DOWN) {
                posY = 8;
                sizeY = 8;

                sizeX = 16;
                sizeZ = 16;
            } else if (face == MCCBlockFace.EAST) {
                posX = 8;
                sizeX = 8;

                sizeY = 16;
                sizeZ = 16;
            } else if (face == MCCBlockFace.NORTH) {
                posZ = 8;
                sizeZ = 8;

                sizeX = 16;
                sizeY = 16;
            } else if (face == MCCBlockFace.SOUTH) {
                posZ = 8;
                sizeZ = 8;

                sizeX = 16;
                sizeY = 16;
            } else if (face == MCCBlockFace.UP) {
                posY = 8;
                sizeY = 8;

                sizeX = 16;
                sizeZ = 16;
            } else if (face == MCCBlockFace.WEST) {
                posX = 8;
                sizeX = 8;

                sizeY = 16;
                sizeZ = 16;
            }

            var element = JsonObjectBuilder.create()
                    .add("from", JsonArrayBuilder.create().add(posX).add(posY).add(posZ))
                    .add("to", JsonArrayBuilder.create().add(sizeX).add(sizeY).add(sizeZ))
                    .add("faces",
                            JsonObjectBuilder.create()
                                    .add(faceName,
                                            JsonObjectBuilder.create()
                                                    .add("texture", "#" + faceName)
                                                    .add("cullface", faceName)
                                    )
                    );


            return new ModelType("", (namespacedKey, jsonObject) ->
                    JsonObjectBuilder.create(jsonObject).add("parent", "block/block")
                            .add("elements", JsonArrayBuilder.create().add(element))
                            .add("textures",
                                    JsonObjectBuilder.create()
                                            .add("particle", namespacedKey.toString())
/*                                                      .add("down", emptyTexture.toString())
                                                      .add("up", emptyTexture.toString())
                                                      .add("north", emptyTexture.toString())
                                                      .add("east", emptyTexture.toString())
                                                      .add("south", emptyTexture.toString())
                                                      .add("west", emptyTexture.toString())*/
                                            .add(faceName, namespacedKey.toString())
                            )
                            .build());
        }

        public static ModelType useModelOfExistingItem(Key itemKey) {
            return new ModelType("minecraft:item/generated", (namespacedKey, jsonObject) ->
                    JsonObjectBuilder.create(jsonObject).add("parent", itemKey.asString())
                            .build());
        }

        public static final ModelType GENERATED_ITEM = new ModelType("minecraft:item/generated", (namespacedKey, jsonObject) ->
                JsonObjectBuilder.create(jsonObject).add("parent", "minecraft:item/generated")
                        .add("textures", JsonObjectBuilder.create().add("layer0", namespacedKey.toString()))
                        .build());
        public static final ModelType HAND_HELD = new ModelType("minecraft:item/handheld", (namespacedKey, jsonObject) ->
                JsonObjectBuilder.create(jsonObject).add("parent", "minecraft:item/handheld")
                        .add("textures", JsonObjectBuilder.create().add("layer0", namespacedKey.toString()))
                        .build());
        public static final ModelType FAKE_CROP = new ModelType("minecraft:block/crop", (namespacedKey, jsonObject) ->
                JsonObjectBuilder.create(jsonObject).add("parent", "minecraft:block/crop")
                        .add("textures", JsonObjectBuilder.create().add("crop", namespacedKey.toString()))
                        .build());

        public static final ModelType CUBE_ALL = new ModelType("minecraft:block/cube_all", (namespacedKey, jsonObject) ->
                JsonObjectBuilder.create(jsonObject).add("parent", "minecraft:block/cube_all")
                        .add("textures", JsonObjectBuilder.create().add("all", namespacedKey.toString()))
                        .build());

        public static final ModelType CUBE_ONLY_FACE_UP = new ModelType("minecraft:block/cube_all", (namespacedKey, jsonObject) ->
                JsonObjectBuilder.create(jsonObject).add("parent", "minecraft:block/cube_all")
                        .add("textures", JsonObjectBuilder.create().add("all", namespacedKey.toString()))
                        .build());

        public static final ModelType CLICKABLE_ITEM = new ModelType("minecraft:clickable_item", (namespacedKey, jsonObject) ->
                JsonObjectBuilder.create(jsonObject).add("parent", "minecraft:item/generated")
                        .add("textures", JsonObjectBuilder.create().add("layer0", namespacedKey.toString()))
                        .add("", "")
                        .add("display",
                                JsonObjectBuilder.create()
                                        .add("gui",
                                                JsonObjectBuilder.create()
                                                        .add("scale", JsonArrayBuilder
                                                                .create()
                                                                .add(1)
                                                                .add(1)
                                                                .add(1)))
                                        .add("thirdperson_righthand",
                                                JsonObjectBuilder.create()
                                                        .add("scale", JsonArrayBuilder
                                                                .create()
                                                                .add(0)
                                                                .add(0)
                                                                .add(0)))
                                        .add("thirdperson_lefthand",
                                                JsonObjectBuilder.create()
                                                        .add("scale", JsonArrayBuilder
                                                                .create()
                                                                .add(0)
                                                                .add(0)
                                                                .add(0)))
                                        .add("firstperson_righthand",
                                                JsonObjectBuilder.create()
                                                        .add("scale", JsonArrayBuilder
                                                                .create()
                                                                .add(0)
                                                                .add(0)
                                                                .add(0)))
                                        .add("firstperson_lefthand",
                                                JsonObjectBuilder.create()
                                                        .add("scale", JsonArrayBuilder
                                                                .create()
                                                                .add(0)
                                                                .add(0)
                                                                .add(0)))
                                        .add("ground",
                                                JsonObjectBuilder.create()
                                                        .add("scale", JsonArrayBuilder
                                                                .create()
                                                                .add(0)
                                                                .add(0)
                                                                .add(0)))
                                        .add("head",
                                                JsonObjectBuilder.create()
                                                        .add("scale", JsonArrayBuilder
                                                                .create()
                                                                .add(0)
                                                                .add(0)
                                                                .add(0)))
                        )
                        .build());
    }

    public MCCItemType getMaterial() {
        return material;
    }

    @Deprecated
    public int getCustomModelData() {
        return customModelData;
    }

    public Asset<CustomResourcePack> getPngFile() {
        return pngFile;
    }

    public ModelType getModelType() {
        return modelType;
    }
}
