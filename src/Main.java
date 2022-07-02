import cc.CCTextFrame;
import cc.CCVideoSettings;
import image.ImageFrame;
import image.Palette;
import image.PaletteFrame;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.HighGui;
import org.opencv.videoio.VideoCapture;

import java.io.*;
import java.util.Arrays;

public class Main {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String args[]) throws IOException, InterruptedException {
        String videoPath = args[0];
        VideoCapture capture = new VideoCapture(videoPath);
        Mat videoFrame = new Mat();
        for (int i = 0; i < 1600; i++) {
            capture.grab();
        }
        for (int i = 0; i < 1; i++) {
            capture.read(videoFrame);
            ImageFrame ccFrame = new ImageFrame(videoFrame, new CCVideoSettings(280, 240, 30));
            Palette p = new Palette(ccFrame, 16);
            System.out.println("colors: " + p.getColors().length);
            PaletteFrame pFrame = new PaletteFrame(ccFrame, p, true);

            HighGui.imshow("Original", videoFrame);
            // HighGui.imshow("Image", ccFrame.getImageMat());
            System.out.println(Arrays.toString(pFrame.getColorCounts()));
            HighGui.imshow("PImage", pFrame.getImageFrame().getImageMat());

            CCTextFrame tFrame = new CCTextFrame(pFrame);
            HighGui.imshow("TImage", tFrame.getPaletteFrame().getImageFrame().getImageMat());

            HighGui.waitKey(0);
            HighGui.destroyAllWindows();

            DataOutputStream stream = new DataOutputStream(new FileOutputStream("./output/out.ccvideo"));
            ccFrame.getSettings().writeContents(stream);
            stream.close();

            DataInputStream streamIn = new DataInputStream(new FileInputStream("./output/out.ccvideo"));
            System.out.println("Stream in:");
            System.out.println(streamIn.readShort());
            System.out.println(streamIn.readShort());
            System.out.println(streamIn.readByte());
        }
        System.out.println("done");
        capture.release();
        System.exit(0);
    }
}
