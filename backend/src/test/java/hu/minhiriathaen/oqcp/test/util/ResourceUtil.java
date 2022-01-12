package hu.minhiriathaen.oqcp.test.util;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

public class ResourceUtil {

  public static String asString(final Resource resource) {
    try (Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8)) {
      return FileCopyUtils.copyToString(reader);
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
