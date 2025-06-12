package io.adampoi.java_auto_grader.util;

import java.io.IOException;

public class ProcessUtils {

    public static String readOutput(Process process) throws IOException {
        return new String(process.getInputStream().readAllBytes());
    }

    public static String readErrors(Process process) throws IOException {
        return new String(process.getErrorStream().readAllBytes());
    }
}