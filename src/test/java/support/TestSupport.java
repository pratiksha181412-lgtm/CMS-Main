package support;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class TestSupport {

    private TestSupport() {
    }

    public static String randomAlpha(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append((char) ('a' + (int) (Math.random() * 26)));
        }
        return sb.toString();
    }

    public static Path testImagePath(Class<?> clazz) {
        for (String name : new String[] { "test-logo.jpg", "test-logo.png" }) {
            var resource = clazz.getClassLoader().getResource(name);
            if (resource != null) {
                try {
                    return Paths.get(resource.toURI());
                } catch (URISyntaxException e) {
                    throw new RuntimeException("Invalid test image path", e);
                }
            }
        }
        return Path.of("src/test/resources/test-logo.png");
    }
}
