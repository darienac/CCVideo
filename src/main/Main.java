package main;

import cc.CCVideoTranscoder;
import server.Server;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class Main {
    private static ProgramMode programMode;

    /**
     * The main method called when the program is run
     * @param args
     * <br>1. Input video location
     * <br>2. Output width (in CC subpixels)
     * <br>3. Output height (in CC subpixels)
     * <br>4. (Optional) output file location, else hosts files on a webserver
     * @throws IOException
     */
    public static void main(String args[]) throws IOException {
        CCVideoTranscoder transcoder = new CCVideoTranscoder();
        if (args.length < 3) {
            throw new RuntimeException("Not enough arguments provided");
        }
        if (args.length == 3) {
            programMode = ProgramMode.WEB_HOST;
        } else {
            programMode = ProgramMode.FILE_OUT;
        }

        OutputStream outputStream;
        if (programMode == ProgramMode.WEB_HOST) {
            outputStream = new ByteArrayOutputStream();
        } else {
            outputStream = new FileOutputStream(args[3]);
        }

        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
        if (args[0].endsWith(".ccvideo")) {
            Files.copy(Path.of(args[0]), dataOutputStream);
        } else {
            transcoder.convertVideo(args[0], dataOutputStream, Integer.parseInt(args[1]), Integer.parseInt(args[2]));
        }

        if (programMode == ProgramMode.WEB_HOST) {
            assert outputStream instanceof ByteArrayOutputStream;
            Server server = new Server(((ByteArrayOutputStream) outputStream).toByteArray());
            server.run(4444);
        } else {
            System.out.println("Done!");
            System.exit(0);
        }
    }

    public enum ProgramMode {
        FILE_OUT,
        WEB_HOST,
    }
}
