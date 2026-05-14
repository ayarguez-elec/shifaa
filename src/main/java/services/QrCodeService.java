package services;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║  QrCodeService — API 2 : Génération de QR Code                 ║
 * ╠══════════════════════════════════════════════════════════════════╣
 * ║  Generates a unique PNG QR code per confirmed reservation.      ║
 * ║  QR content: structured JSON payload with reservation details.  ║
 * ║  Output: saved to configurable folder, path stored in DB.       ║
 * ╚══════════════════════════════════════════════════════════════════╝
 */
public class QrCodeService {

    private static final String CONFIG_FILE = "/email.properties";
    private final String outputDir;
    private final int    size;

    public QrCodeService() {
        Properties props = loadConfig();
        this.outputDir = props.getProperty("qrcode.output.dir", "qrcodes");
        this.size      = Integer.parseInt(props.getProperty("qrcode.size.px", "300"));
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Generates a QR code PNG for a reservation and returns the absolute file path.
     *
     * @param reservationId  DB id of the reservation
     * @param eventId        event id
     * @param eventTitre     event title
     * @param participantNom participant full name
     * @param participantEmail participant email
     * @param nombrePlaces   number of booked seats
     * @param dateDemande    ISO date-time string
     * @return absolute path to the generated PNG file
     */
    public String generateQrCode(int reservationId, int eventId, String eventTitre,
                                  String participantNom, String participantEmail,
                                  int nombrePlaces, String dateDemande) throws WriterException, IOException {

        // ── QR payload: structured text readable by any QR scanner ────────
        String payload = buildPayload(reservationId, eventId, eventTitre,
                                      participantNom, participantEmail,
                                      nombrePlaces, dateDemande);

        // ── Output path ───────────────────────────────────────────────────
        Path dir  = Paths.get(outputDir);
        Files.createDirectories(dir);
        String filename = "reservation_" + reservationId + ".png";
        Path   outPath  = dir.resolve(filename);

        // ── Generate with ZXing ───────────────────────────────────────────
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN, 2);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(payload, BarcodeFormat.QR_CODE, size, size, hints);

        // Dark = dark blue (#1e3a5f), light = white for brand consistency
        MatrixToImageConfig config = new MatrixToImageConfig(0xFF1e3a5f, 0xFFFFFFFF);
        MatrixToImageWriter.writeToPath(matrix, "PNG", outPath, config);

        System.out.println("🔳 [QrCode] Généré → " + outPath.toAbsolutePath());
        return outPath.toAbsolutePath().toString();
    }

    // ── Payload builder ───────────────────────────────────────────────────────
    private String buildPayload(int resId, int evId, String evTitre,
                                 String partNom, String partEmail,
                                 int places, String date) {
        return "SHIFAA-RESERVATION\n" +
               "ID: " + resId + "\n" +
               "EVENEMENT: " + evTitre + " (#" + evId + ")\n" +
               "PARTICIPANT: " + partNom + "\n" +
               "EMAIL: " + partEmail + "\n" +
               "PLACES: " + places + "\n" +
               "DATE: " + date + "\n" +
               "VERIFY: http://localhost:8080/reservation/" + resId;
    }

    // ── Get path for an existing QR code (without regenerating) ──────────────
    public String getQrCodePath(int reservationId) {
        Path p = Paths.get(outputDir, "reservation_" + reservationId + ".png");
        return Files.exists(p) ? p.toAbsolutePath().toString() : null;
    }

    // ── Config loader ─────────────────────────────────────────────────────────
    private Properties loadConfig() {
        Properties props = new Properties();
        try (InputStream is = getClass().getResourceAsStream(CONFIG_FILE)) {
            if (is != null) props.load(is);
        } catch (IOException e) {
            System.err.println("⚠️ [QrCode] email.properties non trouvé, valeurs par défaut utilisées.");
        }
        return props;
    }
}
