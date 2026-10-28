# Pack generator: Minecraft 26.3

Build with JDK 25 and the checked-in Gradle 9.8.0 wrapper. MCC dependencies and artifacts use `26.3-R0.1-SNAPSHOT`.

Use `PackFormat.RESOURCE_26_3` (97.1) for CustomResourcePack and `PackFormat.DATA_26_3` (121.0) for CustomDataPack. Both now accept a PackFormat in addition to the old integer constructor. The getter `getPackFormat()` retains the integer major for source compatibility; `getTargetPackFormat()` also exposes the minor.

Modern metadata writes exact `[major, minor]` arrays to `min_format` and `max_format`. A high `pack_format` alone is no longer accepted by Minecraft. Resource formats through 64 and data formats through 81 keep their legacy metadata. Tests cover modern resource/data metadata, legacy metadata and invalid inputs. Formats were checked against the official client JAR's version.json and [Mojang's release notes](https://www.minecraft.net/en-us/article/minecraft-java-edition-26-3).

Text rendering now overrides `assets/minecraft/shaders/core/text.vsh`. The old rendertype_text JSON/program files are removed. The shader follows 26.3's uniform blocks, includes and explicit input/output locations. MCC screen anchors run only in the GUI variant; world text retains native lighting and fog.

To reproduce shader compilation/link validation, use a 26.3 client JAR and glslangValidator:

```bash
python3 scripts/verify-text-shader.py --client-jar /path/to/minecraft-client-26.3.jar
```

This checks GUI, grayscale GUI, world and see-through variants, with an actual inserted MCC instruction and the original client fragment shader/includes. It does not replace a visual check in a graphical Minecraft client.

The two legacy effect names used by menus are now SLOWNESS and MINING_FATIGUE. Existing item model definitions already use assets/items and minecraft:model. The hoster can send a named installed pack; regenerating ZIPs clears stale hash entries. Wrapper and extension now share a single pack/hoster, avoiding duplicate singleton setup and port binding.

The complete research and ordered workspace update log are in the sibling fork repository's `docs/update-26.3/UPDATE.md`.

LanguageStorage now initializes Adventure 5.2's TranslationStore.messageFormat rather than the removed TranslationRegistry. Adventure dependencies match the target Paper server. A regression test initializes the actual store and checks Unicode and language fallbacks.

Actual HTTP-hosting integration passes: the final ZIP bytes and SHA-1 match the server's advertised resource pack. HostingSettings.requireResourcePack is propagated instead of hardcoding required=true. ResourcePackFileHoster owns and closes its Vertx instance and waits for completion. HUD interruption is a normal shutdown path, and its running flag is volatile.
