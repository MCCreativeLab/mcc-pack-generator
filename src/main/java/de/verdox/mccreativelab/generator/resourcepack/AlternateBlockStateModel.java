package de.verdox.mccreativelab.generator.resourcepack;

import com.google.gson.JsonObject;
import de.verdox.mccreativelab.wrapper.annotations.MCCRequireVanillaElement;
import de.verdox.mccreativelab.wrapper.block.MCCBlockState;
import de.verdox.vserializer.util.gson.JsonObjectBuilder;
import net.kyori.adventure.key.Key;

import java.io.IOException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AlternateBlockStateModel extends ResourcePackResource {
    private final MCCBlockState blockData;
    private final Key alternativeModelKey;

    public AlternateBlockStateModel(@MCCRequireVanillaElement MCCBlockState blockData, Key alternativeModelKey) {
        super(blockData.key());
        blockData.requireVanilla();
        this.blockData = blockData;
        this.alternativeModelKey = alternativeModelKey;
    }

    @Override
    public void installResourceToPack(CustomResourcePack customPack) throws IOException {}

    String getVariantName() {
        return getVariantName(blockData);
    }

    private static String getVariantName(MCCBlockState blockData) {
        String regex = "\\[(.*?)\\]";
        String input = blockData.toBlockDataString();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        if (matcher.find())
            return matcher.group(1);
        else return "";
    }

    public MCCBlockState getBlockData() {
        return blockData;
    }

    public static JsonObject createBlockStateJson(Set<AlternateBlockStateModel> alternateBlockStateModels){
        var variants = JsonObjectBuilder.create();
        // Unreserved note-block states retain the vanilla model. Enumerate the
        // current native states so fallback and custom selectors never overlap.
        if (!alternateBlockStateModels.isEmpty()) {
            var sample = alternateBlockStateModels.iterator().next().blockData;
            if (sample.key().equals(Key.key("minecraft", "note_block"))) {
                for (MCCBlockState state : sample.getBlockType().getAllBlockStates())
                    variants.add(getVariantName(state), JsonObjectBuilder.create().add("model", "minecraft:block/note_block"));
            }
        }
        for (AlternateBlockStateModel alternateBlockStateModel : alternateBlockStateModels) {
            variants.add(alternateBlockStateModel.getVariantName(), JsonObjectBuilder.create().add("model", alternateBlockStateModel.alternativeModelKey.asString()));
        }
        return JsonObjectBuilder.create().add("variants",variants).build();
    }
}
