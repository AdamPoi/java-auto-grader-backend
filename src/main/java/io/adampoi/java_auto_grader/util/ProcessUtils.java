package io.adampoi.java_auto_grader.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ProcessUtils {

    public static String readOutput(Process process) throws IOException {
        return new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    public static String readErrors(Process process) throws IOException {
        return new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
    }
}
