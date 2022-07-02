package image;

import cc.CCVideoSettings;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class ImageFrame {
    private CCVideoSettings settings;
    private int[][] imageArray;

    public ImageFrame(Mat videoFrame, CCVideoSettings settings) {
        this.settings = settings;

        double newAspect = (double) settings.getWidth() / settings.getHeight();
        double oldAspect = (double) videoFrame.width() / videoFrame.height();
        Mat shrunkFrame = new Mat();
        Size shrunkSize;
        if (newAspect > oldAspect) {
            shrunkSize = new Size(oldAspect * settings.getHeight(), settings.getHeight());
        } else {
            shrunkSize = new Size(settings.getWidth(), settings.getWidth() / oldAspect);
        }
        Imgproc.resize(videoFrame, shrunkFrame, shrunkSize, 0, 0, Imgproc.INTER_LINEAR);

        Mat borderFrame = new Mat();
        int shrunkWidth = shrunkFrame.width();
        int shrunkHeight = shrunkFrame.height();
        if (shrunkWidth < settings.getWidth()) {
            int left = ((settings.getWidth() - shrunkWidth) / 2);
            int right = settings.getWidth() - shrunkWidth - left;
            Core.copyMakeBorder(shrunkFrame, borderFrame, 0, 0, left, right, Core.BORDER_CONSTANT);
        } else {
            int top = ((settings.getHeight() - shrunkHeight) / 2);
            int bottom = settings.getHeight() - shrunkHeight - top;
            Core.copyMakeBorder(shrunkFrame, borderFrame, top, bottom, 0, 0, Core.BORDER_CONSTANT);
        }

        imageArray = makeImageArray(borderFrame);
    }

    public ImageFrame(ImageFrame image) {
        settings = image.getSettings();
        imageArray = new int[settings.getWidth() * settings.getHeight()][3];
        for (int x = 0; x < settings.getWidth(); x++) {
            for (int y = 0; y < settings.getHeight(); y++) {
                setPixel(x, y, image.getPixel(x, y));
            }
        }
    }

    public ImageFrame(int[][] imageArray, CCVideoSettings settings) {
        this.imageArray = imageArray;
        this.settings = settings;
    }

    private int[][] makeImageArray(Mat imageFrame) {
        int numChannels = imageFrame.channels();
        int frameSize = imageFrame.rows() * imageFrame.cols();
        byte[] byteBuffer = new byte[frameSize * numChannels];
        imageFrame.get(0, 0, byteBuffer);

        int[][] out = new int[frameSize][3];
        for (int i = 0; i < frameSize; i++) {
            if (numChannels < 3) {
                out[i][0] = byteToInt(byteBuffer[i * numChannels]);
                out[i][1] = byteToInt(byteBuffer[i * numChannels]);
                out[i][2] = byteToInt(byteBuffer[i * numChannels]);
            } else {
                out[i][0] = byteToInt(byteBuffer[i * numChannels]);
                out[i][1] = byteToInt(byteBuffer[i * numChannels + 1]);
                out[i][2] = byteToInt(byteBuffer[i * numChannels + 2]);
            }
        }

        return out;
    }

    private int byteToInt(byte num) {
        if (num < 0) {
            return num + 256;
        }
        return num;
    }

    private int[][] getImageArray() {
        return imageArray;
    }

    public int[] getPixel(int x, int y) {
        return getPixel(y * settings.getWidth() + x);
    }

    public int[] getPixel(int index) {
        int[] color = imageArray[index];
        int[] out = new int[3];
        for (int i = 0; i < 3; i++) {
            out[i] = color[2 - i];
        }
        return out;
    }

    public void setPixel(int x, int y, int[] color) {
        int out[] = new int[3];
        for (int i = 0; i < 3; i++) {
            out[i] = color[2 - i];
        }
        imageArray[y * settings.getWidth() + x] = out;
    }

    public int[][] getColorSpace() {
        int[] colorMin = {255, 255, 255};
        int[] colorMax = {0, 0, 0};
        for (int x = 0; x < settings.getWidth(); x++) {
            for (int y = 0; y < settings.getHeight(); y++) {
                int[] color = getPixel(x, y);
                for (int i = 0; i < 3; i++) {
                    if (color[i] < colorMin[i]) {
                        colorMin[i] = color[i];
                    }
                    if (color[i] > colorMax[i]) {
                        colorMax[i] = color[i];
                    }
                }
            }
        }
        return new int[][] {colorMin, colorMax};
    }

    public Mat getImageMat() {
        Mat out = new Mat();
        out.create(settings.getHeight(), settings.getWidth(), CvType.CV_8UC3);
        System.out.println(settings.getHeight() * settings.getWidth());
        for (int y = 0; y < settings.getHeight(); y++) {
            for (int x = 0; x < settings.getWidth(); x++) {
                int i = y * settings.getWidth() + x;
                byte[] colorOut = new byte[3];
                colorOut[0] = (byte) imageArray[i][0];
                colorOut[1] = (byte) imageArray[i][1];
                colorOut[2] = (byte) imageArray[i][2];
                out.put(y, x, colorOut);
            }
        }
        return out;
    }

    public CCVideoSettings getSettings() {
        return settings;
    }
}
