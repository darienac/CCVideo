package image;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class Palette {
    private int[][] palette;

    public Palette(ImageFrame image, int colorLimit) {
        int[][] colorSpace = image.getColorSpace();
        Palette bins = new Palette(new int[0][3]);
        for (int dim = 3; dim <= 8; dim++) {
            bins = new Palette(colorSpace, dim, dim, dim);
            int[] colorCounts = (new PaletteFrame(image, bins, false)).getColorCounts();
            List<Integer> nonzeroIndices = new LinkedList<Integer>();
            for (int i = 0; i < colorCounts.length; i++) {
                if (colorCounts[i] > 0) {
                    nonzeroIndices.add(i);
                }
            }
            if (nonzeroIndices.size() < colorLimit && dim < 8) {
                continue;
            }
            nonzeroIndices.sort(new Comparator<Integer>() {
                @Override
                public int compare(Integer o1, Integer o2) {
                    int val1 = colorCounts[o1];
                    int val2 = colorCounts[o2];
                    return val1 - val2;
                }
            });
            int[][] newColors = new int[colorLimit][3];
            for (int i = 0; i < colorLimit && i < nonzeroIndices.size(); i++) {
                newColors[i] = bins.getColor(nonzeroIndices.get(i));
            }
            bins = new Palette(newColors);
            break;
        }
        bins.tweakColorsToImage(image);
        this.palette = bins.getColors();
    }

    public Palette(int[][] palette) {
        this.palette = palette;
    }

    public Palette(int[][] colorSpace, int binsR, int binsG, int binsB) {
        palette = new int[binsR * binsG * binsB][3];
        for (int r = 0, i = 0; r < binsR; r++) {
            for (int g = 0; g < binsG; g++) {
                for (int b = 0; b < binsB; b++, i++) {
                    int[] color = {
                            (int) (colorSpace[0][0] + (double) r / (binsR - 1) * (colorSpace[1][0] - colorSpace[0][0])),
                            (int) (colorSpace[0][1] + (double) g / (binsG - 1) * (colorSpace[1][1] - colorSpace[0][1])),
                            (int) (colorSpace[0][2] + (double) b / (binsB - 1) * (colorSpace[1][2] - colorSpace[0][2])),
                    };
                    palette[i] = color;
                }
            }
        }
    }

    public void tweakColorsToImage(ImageFrame image) {
        PaletteFrame pFrame = new PaletteFrame(image, this, false);
        int[] pImage = pFrame.getImage();
        int[] colorCounts = pFrame.getColorCounts();
        int[][] sumColors = new int[colorCounts.length][3];
        for (int i = 0; i < pImage.length; i++) {
            int[] color = image.getPixel(i);
            sumColors[pImage[i]][0] += color[0];
            sumColors[pImage[i]][1] += color[1];
            sumColors[pImage[i]][2] += color[2];
        }
        for (int i = 0; i < sumColors.length; i++) {
            int[] color = new int[3];
            if (colorCounts[i] > 0) {
                color[0] = sumColors[i][0] / colorCounts[i];
                color[1] = sumColors[i][1] / colorCounts[i];
                color[2] = sumColors[i][2] / colorCounts[i];
                sumColors[i] = color;
            }
        }
        palette = sumColors;
    }

    public int[] getColor(int index) {
        return palette[index];
    }

    public int[][] getColors() {
        return palette;
    }

    public int getBestColorIndex(int[] color) {
        double closestDist = -1.0;
        int bestIndex = -1;
        for (int i = 0; i < palette.length; i++) {
            int[] pColor = palette[i];
            double dist = getColorDistance(color, pColor);
            if (dist < closestDist || closestDist == -1.0) {
                closestDist = dist;
                bestIndex = i;
            }
        }
        return bestIndex;
    }

    public double getColorDistance(int[] color1, int[] color2) {
        return Math.sqrt((color1[0]-color2[0])*(color1[0]-color2[0]) + (color1[1]-color2[1])*(color1[1]-color2[1])
                + (color1[2]-color2[2])*(color1[2]-color2[2]));
    }

    public double getColorDistance2(int[] color1, int[] color2) {
        int[] error = {color1[0] - color2[0], color1[1] - color2[1], color1[2] - color2[2]};
        return Math.abs(error[0]*error[0]) + Math.abs(error[1]*error[1]) + Math.abs(error[2]*error[2]);
    }

    public double getColorDistance3(int[] color1, int[] color2) {
        double[] hsl1 = RGBtoHSL(new double[] {color1[0] / 255.0, color1[1] / 255.0, color1[2] / 255.0});
        double[] hsl2 = RGBtoHSL(new double[] {color2[0] / 255.0, color2[1] / 255.0, color2[2] / 255.0});

        return 0.475 * Math.abs(hsl1[0] - hsl2[0]) + 0.2875 * Math.abs(hsl1[1] - hsl2[1])
                + 0.2375 * Math.abs(hsl1[2] - hsl2[2]);
    }

    private double[] RGBtoHSL(double[] rgb) {
        double xMax = Math.max(rgb[0], Math.max(rgb[1], rgb[2]));
        double xMin = Math.min(rgb[0], Math.min(rgb[1], rgb[2]));

        double c = xMax - xMin;
        double l = (xMax + xMin) / 2;
        double v = xMax;

        double h = 0;
        if (c == 0.0) {
            h = 0;
        } else if (v == rgb[0]) {
            h = Math.PI / 3 * ((rgb[1] - rgb[2]) / c);
        } else if (v == rgb[1]) {
            h = Math.PI / 3 * (2 + (rgb[2] - rgb[0]) / c);
        } else if (v == rgb[2]) {
            h = Math.PI / 3 * (4 + (rgb[0] - rgb[1]) / c);
        }

        double sL;
        if (l == 0 || l == 1) {
            sL = 0;
        } else {
            sL = c / (1 - Math.abs(v + v - c - 1));
        }

        return new double[] {h, sL, l};
    }

    public void writeContents(DataOutputStream stream) throws IOException {
        stream.writeByte(palette.length);
        for (int i = 0; i < palette.length; i++) {
            for (int j = 0; j < 3; j++) {
                stream.writeByte(palette[i][j]);
            }
        }
    }
}
