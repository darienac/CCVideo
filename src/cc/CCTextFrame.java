package cc;

import image.Palette;
import image.PaletteFrame;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

public class CCTextFrame {
    private ColorChar[] text;
    private Palette palette;
    private CCVideoSettings settings;

    public CCTextFrame(PaletteFrame pFrame) {
        this.palette = pFrame.getPalette();
        this.settings = pFrame.getSettings();
        if (palette.getColors().length > 16) {
            throw new IllegalArgumentException("Palette cannot contain more than 16 colors");
        }
        if (settings.getWidth() % 2 != 0 || settings.getHeight() % 3 != 0) {
            throw new IllegalArgumentException("Frame must be divisible into 2x3 pixel cells");
        }

        text = new ColorChar[settings.getWidth() * settings.getHeight() / 6];
        int textPos = 0;
        for (int y = 0; y < settings.getHeight(); y += 3) {
            for (int x = 0; x < settings.getWidth(); x += 2) {
                int[] pixelGroup = {
                        pFrame.getPixelColorRef(x, y),
                        pFrame.getPixelColorRef(x + 1, y),
                        pFrame.getPixelColorRef(x, y + 1),
                        pFrame.getPixelColorRef(x + 1, y + 1),
                        pFrame.getPixelColorRef(x, y + 2),
                        pFrame.getPixelColorRef(x + 1, y + 2),
                };
                Map<Integer, Integer> colorCounts = new HashMap<>();
                for (int i = 0; i < pixelGroup.length; i++) {
                    if (!colorCounts.containsKey(pixelGroup[i])) {
                        colorCounts.put(pixelGroup[i], 1);
                    } else {
                        colorCounts.put(pixelGroup[i], colorCounts.get(pixelGroup[i]) + 1);
                    }
                }
                List<Integer> bestColors = new LinkedList<Integer>(colorCounts.keySet());
                bestColors.sort(new Comparator<Integer>() {
                    @Override
                    public int compare(Integer o1, Integer o2) {
                        return colorCounts.get(o2) - colorCounts.get(o1);
                    }
                });

                int fg = bestColors.get(0);
                if (bestColors.size() == 1) {
                    text[textPos] = new ColorChar((char) 128, fg, fg);
                    textPos++;
                    continue;
                }
                int bg = bestColors.get(1);
                boolean invertChar = (pixelGroup[5] == fg);
                char charNum = 128;
                for (int i = 0; i < pixelGroup.length; i++) {
                    if (invertChar) {
                        if (pixelGroup[i] != fg) {
                            charNum += 1 << i;
                        }
                    } else {
                        if (pixelGroup[i] == fg) {
                            charNum += 1 << i;
                        }
                    }
                }
                if (invertChar) {
                    text[textPos] = new ColorChar(charNum, bg, fg);
                } else {
                    text[textPos] = new ColorChar(charNum, fg, bg);
                }
                textPos++;
            }
        }
    }

    public PaletteFrame getPaletteFrame() {
        int[] image = new int[settings.getWidth() * settings.getHeight()];
        int textPos = 0;
        for (int y = 0; y < settings.getHeight() / 3; y++) {
            for (int x = 0; x < settings.getWidth() / 2; x++) {
                ColorChar colorChar = text[textPos];
                int bits = colorChar.getLetter() - 128;
                for (int cy = 0; cy < 3; cy++) {
                    for (int cx = 0; cx < 2; cx++) {
                        int imgX = x * 2 + cx;
                        int imgY = y * 3 + cy;
                        if (bits % 2 == 1) {
                            image[imgY * settings.getWidth() + imgX] = colorChar.getFg();
                        } else {
                            image[imgY * settings.getWidth() + imgX] = colorChar.getBg();
                        }
                        bits = bits >> 1;
                    }
                }
                textPos++;
            }
        }
        return new PaletteFrame(image, palette, settings);
    }

    private static class ColorChar {
        private char letter;
        private int fg;
        private int bg;

        public ColorChar(char letter, int fg, int bg) {
            this.letter = letter;
            this.fg = fg;
            this.bg = bg;
        }

        public char getLetter() {
            return letter;
        }

        public void setLetter(char letter) {
            this.letter = letter;
        }

        public int getFg() {
            return fg;
        }

        public void setFg(int fg) {
            this.fg = fg;
        }

        public int getBg() {
            return bg;
        }

        public void setBg(int bg) {
            this.bg = bg;
        }

        public void writeContents(DataOutputStream stream) throws IOException {
            stream.writeByte(letter);
            stream.writeByte(fg);
            stream.writeByte(bg);
        }
    }

    public Palette getPalette() {
        return palette;
    }

    public CCVideoSettings getSettings() {
        return settings;
    }

    public void writeContents(DataOutputStream stream) throws IOException {
        palette.writeContents(stream);
        for (ColorChar cChar : text) {
            cChar.writeContents(stream);
        }
    }
}
