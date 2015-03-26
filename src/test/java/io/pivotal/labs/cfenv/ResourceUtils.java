package io.pivotal.labs.cfenv;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ResourceUtils {

    public static String loadResource(String name) throws IOException {
        try (InputStream stream = openResource(name)) {
            StringBuilder buffer = new StringBuilder();
            InputStreamReader reader = new InputStreamReader(stream);
            while (true) {
                int character = reader.read();
                if (character == -1) break;
                buffer.append((char) character);
            }
            return buffer.toString();
        }
    }

    public static InputStream openResource(String name) throws FileNotFoundException {
        InputStream stream = ResourceUtils.class.getResourceAsStream(name);
        if (stream == null) throw new FileNotFoundException(name);
        return stream;
    }

}
