package com.matyrobbrt.simpleminers.client;

import com.mojang.blaze3d.platform.NativeImage;

public class TextureRecolouring {
    public static void recolour(NativeImage image, int fromRGBA, int toRGBA) {
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                if (image.getPixelRGBA(x, y) == fromRGBA) {
                    image.setPixelRGBA(x, y, toRGBA);
                }
            }
        }
    }

    public static void overlay(NativeImage target, NativeImage source) {
        for (int x = 0; x < target.getWidth(); x++) {
            for (int y = 0; y < target.getHeight(); y++) {
                if (target.getPixelRGBA(x, y) == 0) {
                    target.setPixelRGBA(x, y, source.getPixelRGBA(x, y));
                }
            }
        }
    }

    public static void overlayAnimated(NativeImage target, NativeImage source, int divisions) {
        final int eachHeight = target.getHeight() / divisions;
        for (int x = 0; x < target.getWidth(); x++) {
            for (int y = 0; y < eachHeight; y++) {
                for (int a = 0; a < divisions; a++) {
                    if (target.getPixelRGBA(x, a * eachHeight + y) == 0) {
                        target.setPixelRGBA(x, a * eachHeight + y, source.getPixelRGBA(x, y));
                    }
                }
            }
        }
    }
}
