package de.verdox.mccreativelab.generator;

import static org.junit.jupiter.api.Assertions.*;

import de.verdox.mccreativelab.generator.resourcepack.CustomResourcePack;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

class AssetTest {
    @Test
    void validationClosesEveryOpenedStream() {
        int[] opened = {0};
        int[] closed = {0};
        var asset =
                new Asset<CustomResourcePack>(
                        () -> {
                            opened[0]++;
                            return new ByteArrayInputStream(new byte[] {1}) {
                                @Override
                                public void close() {
                                    closed[0]++;
                                }
                            };
                        });
        assertTrue(asset.isInputStreamValid());
        assertTrue(asset.isInputStreamValid());
        assertEquals(2, opened[0]);
        assertEquals(opened[0], closed[0]);
    }
}
