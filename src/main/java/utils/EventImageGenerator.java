package utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;

/**
 * Génère automatiquement des images PNG pour chaque type d'événement santé.
 * Les images sont sauvegardées dans le dossier "events_images/" à la racine.
 */
public class EventImageGenerator {

    public static final String OUTPUT_DIR = "events_images";
    private static final int W = 400;
    private static final int H = 180;

    // [type, couleur foncée, couleur claire, symbole, label]
    private static final Object[][] TYPES = {
        {"Don de sang",         new Color(127,29,29),  new Color(220,38,38),   "+",  "DON DE SANG"},
        {"Vaccination",         new Color(20,83,45),   new Color(22,163,74),   "+",  "VACCINATION"},
        {"Conférence médicale", new Color(30,58,95),   new Color(37,99,235),   "M",  "CONFERENCE"},
        {"Consultation",        new Color(49,46,129),  new Color(124,58,237),  "C",  "CONSULTATION"},
        {"Sensibilisation",     new Color(146,64,14),  new Color(217,119,6),   "S",  "SENSIBILISATION"},
        {"Atelier santé",       new Color(19,78,74),   new Color(13,148,136),  "A",  "ATELIER SANTE"},
        {"Dépistage",           new Color(30,58,95),   new Color(2,132,199),   "D",  "DEPISTAGE"},
        {"Premiers secours",    new Color(124,45,18),  new Color(234,88,12),   "+",  "PREMIERS SECOURS"},
        {"Conférence",          new Color(30,58,95),   new Color(37,99,235),   "C",  "CONFERENCE"},
        {"Séminaire",           new Color(20,83,45),   new Color(22,163,74),   "S",  "SEMINAIRE"},
        {"Atelier",             new Color(120,53,15),  new Color(217,119,6),   "A",  "ATELIER"},
    };

    // ── Générer toutes les images au démarrage ────────────────────────────────
    public static void generateAll() {
        try {
            Files.createDirectories(Paths.get(OUTPUT_DIR));
            int count = 0;
            for (Object[] cfg : TYPES) {
                String path = OUTPUT_DIR + "/" + sanitize((String) cfg[0]) + ".png";
                if (!new File(path).exists()) {
                    generate((String)cfg[0], (Color)cfg[1], (Color)cfg[2], (String)cfg[3], (String)cfg[4]);
                    count++;
                }
            }
            if (count > 0)
                System.out.println("✅ [Images] " + count + " image(s) d'événements générée(s) → " + OUTPUT_DIR + "/");
        } catch (Exception e) {
            System.err.println("⚠️ [Images] Génération : " + e.getMessage());
        }
    }

    // ── Retourne le chemin de l'image pour un type donné ─────────────────────
    public static String getImagePath(String eventType) {
        if (eventType == null) return null;
        String path = OUTPUT_DIR + "/" + sanitize(eventType) + ".png";
        return new File(path).exists() ? new File(path).getAbsolutePath() : null;
    }

    // ── Génération d'une image PNG ────────────────────────────────────────────
    private static void generate(String type, Color dark, Color light,
                                  String symbol, String label) throws IOException {
        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // ── Fond dégradé ─────────────────────────────────────────────────────
        GradientPaint gp = new GradientPaint(0, 0, dark, W, H, light);
        g.setPaint(gp);
        g.fillRect(0, 0, W, H);

        // ── Cercles décoratifs ────────────────────────────────────────────────
        g.setColor(new Color(255, 255, 255, 20));
        g.fillOval(W - 110, -30, 140, 140);
        g.setColor(new Color(255, 255, 255, 12));
        g.fillOval(W - 70,  H - 70,  100, 100);
        g.setColor(new Color(255, 255, 255, 8));
        g.fillOval(-20, H - 60, 80, 80);

        // ── Croix médicale centrale ───────────────────────────────────────────
        g.setColor(new Color(255, 255, 255, 140));
        int cx = W / 2, cy = H / 2;
        int barW = 14, barH = 54;
        // Croix blanche (pour Don de sang, Vaccination, Premiers secours)
        if ("+".equals(symbol)) {
            g.fillRoundRect(cx - barW/2, cy - barH/2, barW, barH, 4, 4); // vertical
            g.fillRoundRect(cx - barH/2, cy - barW/2, barH, barW, 4, 4); // horizontal
        } else {
            // Cercle avec initiale pour les autres
            g.setColor(new Color(255, 255, 255, 50));
            g.fillOval(cx - 42, cy - 42, 84, 84);
            g.setColor(new Color(255, 255, 255, 200));
            g.setFont(new Font("Arial", Font.BOLD, 52));
            FontMetrics fm = g.getFontMetrics();
            int sw = fm.stringWidth(symbol);
            g.drawString(symbol, cx - sw / 2, cy + 18);
        }

        // ── Label en bas à gauche ─────────────────────────────────────────────
        g.setColor(new Color(255, 255, 255, 230));
        g.setFont(new Font("Arial", Font.BOLD, 15));
        g.drawString(label, 16, H - 12);

        // ── Ligne décorative en bas ───────────────────────────────────────────
        g.setColor(new Color(255, 255, 255, 60));
        g.fillRect(0, H - 4, W, 4);

        g.dispose();

        File out = new File(OUTPUT_DIR + "/" + sanitize(type) + ".png");
        ImageIO.write(img, "PNG", out);
    }

    // ── Sanitize pour nom de fichier ──────────────────────────────────────────
    public static String sanitize(String s) {
        if (s == null) return "default";
        return s.toLowerCase()
            .replace("é","e").replace("è","e").replace("ê","e")
            .replace("à","a").replace("â","a").replace("î","i")
            .replace("ô","o").replace("ù","u").replace("û","u")
            .replace("ç","c").replace(" ","_")
            .replaceAll("[^a-z0-9_]","");
    }
}
