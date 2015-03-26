package io.pivotal.labs.cfenv;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ResourceUtils {

    public static String loadResource(String name) throws IOException {
        try (InputStream stream = openResource(name)) {
            Scanner scanner = new Scanner(stream, StandardCharsets.UTF_8.name()).useDelimiter("\\z");
            String content = scanner.next();
            IOException exception = scanner.ioException();
            if (exception != null) throw exception;
            return content;
        }
    }

    public static InputStream openResource(String name) throws FileNotFoundException {
        InputStream stream = ResourceUtils.class.getResourceAsStream(name);
        if (stream == null) throw new FileNotFoundException(name);
        return stream;
    }

}
