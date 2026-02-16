package cncs.academy.ess.service;

import org.casbin.jcasbin.main.Enforcer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class CasbinLoader {

    public static Enforcer loadFromResources() {
        try {
            Path model = copyResourceToTemp("model.conf");
            Path policy = copyResourceToTemp("policy.csv");
            return new Enforcer(model.toString(), policy.toString());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load Casbin model/policy", e);
        }
    }

    private static Path copyResourceToTemp(String resourceName) throws IOException {
        InputStream in = CasbinLoader.class.getClassLoader().getResourceAsStream(resourceName);
        if (in == null) throw new IOException("Resource not found: " + resourceName);

        Path tmp = Files.createTempFile(resourceName.replace(".", "_"), "");
        Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);
        in.close();
        tmp.toFile().deleteOnExit();
        return tmp;
    }
}
