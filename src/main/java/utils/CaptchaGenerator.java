package utils;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

public class CaptchaGenerator {
    private String captchaText;
    private Image captchaImage;

    public CaptchaGenerator() {
        generateCaptcha();
    }

    public void generateCaptcha() {
        // 1. Generate random text
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        this.captchaText = sb.toString();

        // 2. Generate Image
        int width = 160;
        int height = 50;
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bufferedImage.createGraphics();

        // Background
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        // Add some noise (lines)
        g2d.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i < 15; i++) {
            int x1 = random.nextInt(width);
            int y1 = random.nextInt(height);
            int x2 = random.nextInt(width);
            int y2 = random.nextInt(height);
            g2d.drawLine(x1, y1, x2, y2);
        }

        // Draw text with random colors and slight rotation
        Font font = new Font("Arial", Font.BOLD, 30);
        g2d.setFont(font);
        
        for (int i = 0; i < captchaText.length(); i++) {
            g2d.setColor(new Color(random.nextInt(150), random.nextInt(150), random.nextInt(150)));
            int x = 10 + (i * 22);
            int y = 35 + random.nextInt(10) - 5;
            
            // Random rotation
            double angle = (random.nextDouble() - 0.5) * 0.5; // -0.25 to 0.25 radians
            g2d.rotate(angle, x, y);
            g2d.drawString(String.valueOf(captchaText.charAt(i)), x, y);
            g2d.rotate(-angle, x, y); // reset rotation
        }

        g2d.dispose();
        
        // Convert to JavaFX Image
        this.captchaImage = SwingFXUtils.toFXImage(bufferedImage, null);
    }

    public String getCaptchaText() {
        return captchaText;
    }

    public Image getCaptchaImage() {
        return captchaImage;
    }

    public boolean validate(String input) {
        if (input == null || input.isEmpty()) return false;
        // Case insensitive comparison
        return captchaText.equalsIgnoreCase(input.trim());
    }
}
