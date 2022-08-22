package cc;

import cc.CCTextFrame;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

public class CCVideo {
    private final CCVideoSettings settings;
    private DataOutputStream stream;

    public CCVideo(CCVideoSettings settings, DataOutputStream stream) throws IOException {
        this.settings = settings;
        this.stream = stream;
        settings.writeContents(stream);
    }

    public void addFrame(CCTextFrame frame) throws IOException {
        if (!settings.equals(frame.getSettings())) {
            throw new IllegalArgumentException("Frame settings must match settings for full video");
        }
        frame.writeContents(stream);
    }
}
