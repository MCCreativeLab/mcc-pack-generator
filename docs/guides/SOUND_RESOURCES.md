# Sound resources and repeated pack builds

Register `SoundData` with a stable resource key and a `SoundVariant` whose `Asset` supplier opens a fresh stream on every invocation. Original OGG files can be copied directly. Other formats are converted through FFmpeg before installation.

Converted OGG files are temporary resources owned by that `SoundData` instance. Install them directly during `installResourceToPack`, then delete only those files in `afterResourceInstallation`. Do not register temporary files as permanent pack resources: a second pack build would retain references to files removed after the first build. Do not delete a shared converter directory while other sounds still use it.

Keep the original input asset available across builds. A second installation must convert from that original input again. `Asset.isInputStreamValid()` opens and closes one probe stream; callers must still provide a supplier that can reopen the original asset.

The native OneBlock code fixture exercises two conversion/install/cleanup cycles with a real WAV input and checks the generated OGG header. Full resource-pack generation and graphical Minecraft validation are separate checks.
