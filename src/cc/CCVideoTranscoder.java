package cc;

import image.ImageFrame;
import image.Palette;
import image.PaletteFrame;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.io.DataOutputStream;
import java.io.IOException;

public class CCVideoTranscoder {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public void convertVideo(String inputPath, DataOutputStream stream, int width, int height) throws IOException {
        System.out.println(inputPath);
        VideoCapture capture = new VideoCapture(inputPath);
        int fps = (int) capture.get(Videoio.CAP_PROP_FPS);
        System.out.println("FPS: " + fps);
        int frameCount = (int) capture.get(Videoio.CAP_PROP_FRAME_COUNT);

        CCVideoSettings settings = new CCVideoSettings(width, height, fps);
        CCVideo video = new CCVideo(settings, stream);
        Mat videoFrame = new Mat();

        for (int i = 0; i < frameCount; i++) {
            if (!capture.read(videoFrame)) {
                break;
            }
            ImageFrame iFrame = new ImageFrame(videoFrame, settings);

            Palette p = new Palette(iFrame, 16);
            PaletteFrame pFrame = new PaletteFrame(iFrame, p, true);

            CCTextFrame tFrame = new CCTextFrame(pFrame);
            video.addFrame(tFrame);
            System.out.println(i + " / " + frameCount);
        }

        stream.close();
    }
}
