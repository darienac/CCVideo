package image;

import cc.CCVideoSettings;
import image.Palette;

import java.io.IOException;
import java.io.OutputStream;

public class PaletteFrame {
    private final Palette palette;

    // different image format, instead holding number id's into the palette
    private int[] image;
    private int[] colorCounts;
    private CCVideoSettings settings;

    public PaletteFrame(ImageFrame image, Palette palette, boolean useDithering) {
        this.palette = palette;
        settings = image.getSettings();

        // Implements Floyd-Steinberg dithering
        ImageFrame pixels = new ImageFrame(image);
        this.image = new int[settings.getWidth() * settings.getHeight()];
        colorCounts = new int[palette.getColors().length];
        for (int y = 0; y < settings.getHeight(); y++) {
            for (int x = 0; x < settings.getWidth(); x++) {
                int[] oldPixel = pixels.getPixel(x, y);

                int newPixelIdx = palette.getBestColorIndex(oldPixel);
                int[] newPixel = palette.getColor(newPixelIdx);

                this.image[y * settings.getWidth() + x] = newPixelIdx;
                colorCounts[newPixelIdx]++;
                pixels.setPixel(x, y, newPixel);

                if (useDithering) {
                    int[] error = {oldPixel[0] - newPixel[0], oldPixel[1] - newPixel[1], oldPixel[2] - newPixel[2]};
                    addError(pixels, x + 1, y, error, 7.0 / 16);
                    addError(pixels, x - 1, y + 1, error, 3.0 / 16);
                    addError(pixels, x, y + 1, error, 5.0 / 16);
                    addError(pixels, x + 1, y + 1, error, 1.0 / 16);
                }
            }
        }
    }

    public PaletteFrame(int[] image, Palette palette, CCVideoSettings settings) {
        this.image = image;
        this.palette = palette;
        this.settings = settings;
    }

    private void addError(ImageFrame pixels, int x, int y, int[] error, double coef) {
        if (x == -1) {
            x = 0;
            return;
        }
        if (x == pixels.getSettings().getWidth()) {
            x = pixels.getSettings().getWidth() - 1;
            return;
        }
        if (y == pixels.getSettings().getHeight()) {
            y = pixels.getSettings().getHeight() - 1;
            return;
        }
        int[] out = pixels.getPixel(x, y);
        for (int i = 0; i < 3; i++) {
            out[i] += error[i] * coef;
        }
        pixels.setPixel(x, y, out);
    }

    public ImageFrame getImageFrame() {
        int[][] array = new int[settings.getWidth() * settings.getHeight()][3];
        for (int i = 0; i < image.length; i++) {
            int[] color = palette.getColor(image[i]);
            int[] outColor = new int[3];
            for (int j = 0; j < 3; j++) {
                outColor[2 - j] = color[j];
            }
            array[i] = outColor;
        }
        return new ImageFrame(array, settings);
    }

    public int getPixelColorRef(int x, int y) {
        return image[y * settings.getWidth() + x];
    }

    public int[] getColorCounts() {
        return colorCounts;
    }

    public int[] getImage() {
        return image;
    }

    public Palette getPalette() {
        return palette;
    }

    public CCVideoSettings getSettings() {
        return settings;
    }
}
