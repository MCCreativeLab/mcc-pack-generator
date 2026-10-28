package de.verdox.mccreativelab.generator;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/** A Minecraft pack version, including the minor version introduced in 1.21.9. */
public record PackFormat(int major, int minor) {
    public static final PackFormat RESOURCE_26_3 = new PackFormat(97, 1);
    public static final PackFormat DATA_26_3 = new PackFormat(121, 0);

    public PackFormat {
        if (major < 0 || minor < 0) {
            throw new IllegalArgumentException("Pack version numbers must be non-negative");
        }
    }

    /** Exact supported version; old formats retain their legacy metadata representation. */
    public JsonObject metadata(String description, int lastLegacyFormat) {
        JsonObject metadata = new JsonObject();
        metadata.addProperty("description", description);
        if (major <= lastLegacyFormat) {
            if (minor != 0) {
                throw new IllegalArgumentException("Legacy pack formats do not support minor versions");
            }
            metadata.addProperty("pack_format", major);
        } else {
            JsonArray version = new JsonArray();
            version.add(major);
            version.add(minor);
            metadata.add("min_format", version);
            metadata.add("max_format", version.deepCopy());
        }
        return metadata;
    }
}
