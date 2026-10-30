# Development references

- [GUI integration guide](docs/guides/GUI_INTEGRATION.md): current wrapper-based GUI boundaries and migration checks.
- [Minecraft 26.3 migration](docs/update-26.3/MIGRATION.md): resource-pack formats, shaders and verification.
- Keep the generator independent of Bukkit/Paper. Convert platform types in the consuming plugin or platform adapter.
- Preserve existing resource keys and GUI behavior when updating consumers. Report graphical-client verification separately from compilation and pack-generation checks.

- [Sound resources](docs/guides/SOUND_RESOURCES.md): stream ownership, audio conversion and repeated pack builds.
