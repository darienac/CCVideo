package cc;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class CCVideoSettings {
    private int width;
    private int height;
    private int framerate;

    public CCVideoSettings(int width, int height, int framerate) {
        this.width = width;
        this.height = height;
        this.framerate = framerate;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getFramerate() {
        return framerate;
    }

    public void setFramerate(int framerate) {
        this.framerate = framerate;
    }

    public void writeContents(DataOutputStream stream) throws IOException {
        stream.writeShort(width);
        stream.writeShort(height);
        stream.writeByte(framerate);
    }
}
