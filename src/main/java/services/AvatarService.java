package services;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class AvatarService {

    /**
     * Propose 6 premium 3D avatars from resources.
     */
    public List<String> generateAvatarOptions(String seed, int count) {
        List<String> options = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            String base64 = loadAvatarFromResources(i);
            if (!base64.isEmpty()) {
                options.add(base64);
            }
        }
        return options;
    }

    private String loadAvatarFromResources(int index) {
        try (InputStream is = getClass().getResourceAsStream("/avatars/avatar" + index + ".png")) {
            if (is == null) return "";
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int n;
            while ((n = is.read(buffer)) != -1) {
                baos.write(buffer, 0, n);
            }
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Fallback for a single avatar (e.g. based on user initials if resources fail)
     */
    public String generateAvatar(String seed) {
        // Just return the first premium avatar as default
        return loadAvatarFromResources(1);
    }
}
