package cc;

import cc.CCTextFrame;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

public class CCVideo {
    private final CCVideoSettings settings;
    private List<CCTextFrame> frames;

    public CCVideo(CCVideoSettings settings) {
        this.settings = settings;
        this.frames = new LinkedList<>();
    }

    public void addFrame(CCTextFrame frame) {
        if (!settings.equals(frame.getSettings())) {
            throw new IllegalArgumentException("Frame settings must match settings for full video");
        }
        frames.add(frame);
    }

    public void writeContents(DataOutputStream stream) throws IOException {
        settings.writeContents(stream);
        for (CCTextFrame frame : frames) {
            frame.writeContents(stream);
        }
    }
}
