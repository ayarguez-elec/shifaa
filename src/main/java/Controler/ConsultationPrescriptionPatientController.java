package Controler;

import entities.Prescription;
import entities.userDS;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import services.userservDS;
import services.SmsService;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

public class ConsultationPrescriptionPatientController {

    @FXML private Label lblPatientName;
    @FXML private Label lblDate;
    @FXML private Label lblDuration;
    @FXML private Label lblDetails;
    @FXML private Label lblPrescriptionId;

    private int currentPatientId;
    private Prescription currentPrescription;
    private userDS currentPatient;
    private userDS currentDoctor;
    private userservDS userService = new userservDS();

    public void setPrescriptionData(Prescription p) {
        this.currentPrescription = p;
        this.currentPatientId = p.getPatientId();
        this.currentPatient = userService.getById(currentPatientId);
        this.currentDoctor = userService.getById(p.getDoctorId());
        if (currentPatient != null) {
            lblPatientName.setText(currentPatient.getFullName());
        }
        
        lblDate.setText(p.getPrescriptionDate().toString());
        lblDuration.setText(p.getTreatmentDuration());
        lblDetails.setText(p.getPrescription());
        lblPrescriptionId.setText("Ordonnance #" + p.getPrescriptionId());
    }

    @FXML
    public void exporterPDF(ActionEvent event) {
        if (currentPrescription == null || currentPatient == null) {
            showAlert("Erreur", "DonnÃ©es manquantes pour exporter le PDF.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter l'ordonnance numÃ©rique");
        fileChooser.setInitialFileName("Ordonnance_" + currentPrescription.getPrescriptionId() + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        File file = fileChooser.showSaveDialog(((Node) event.getSource()).getScene().getWindow());
        if (file == null) return;

        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA, 22, Font.BOLD, new BaseColor(45, 55, 72));
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA, 14, Font.BOLD, new BaseColor(255, 92, 92));
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA, 11, Font.BOLD, new BaseColor(74, 85, 104));
            Font textFont = FontFactory.getFont(FontFactory.HELVETICA, 11, Font.NORMAL, BaseColor.DARK_GRAY);

            Paragraph title = new Paragraph("ORDONNANCE NUMERIQUE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            document.add(new Paragraph("Informations du medecin", sectionFont));
            PdfPTable doctorTable = new PdfPTable(2);
            doctorTable.setWidthPercentage(100);
            doctorTable.setSpacingBefore(8);
            doctorTable.setSpacingAfter(18);
            doctorTable.setWidths(new float[]{1.2f, 3f});

            addInfoRow(doctorTable, "Nom et prenom", currentDoctor != null ? currentDoctor.getFullName() : "N/A", labelFont, textFont);
            addInfoRow(doctorTable, "Numero", valueOrDefault(currentDoctor != null ? currentDoctor.getPhoneNumber() : null), labelFont, textFont);
            addInfoRow(doctorTable, "Specialite", valueOrDefault(currentDoctor != null ? currentDoctor.getSpeciality() : null), labelFont, textFont);
            addInfoRow(doctorTable, "Email", valueOrDefault(currentDoctor != null ? currentDoctor.getEmail() : null), labelFont, textFont);
            document.add(doctorTable);

            document.add(new Paragraph("Prescription", sectionFont));
            PdfPTable prescriptionTable = new PdfPTable(2);
            prescriptionTable.setWidthPercentage(100);
            prescriptionTable.setSpacingBefore(8);
            prescriptionTable.setSpacingAfter(20);
            prescriptionTable.setWidths(new float[]{1.2f, 3f});

            addInfoRow(prescriptionTable, "Patient", currentPatient.getFullName(), labelFont, textFont);
            addInfoRow(prescriptionTable, "Date", currentPrescription.getPrescriptionDate().toString(), labelFont, textFont);
            addInfoRow(prescriptionTable, "Duree", currentPrescription.getTreatmentDuration(), labelFont, textFont);
            addInfoRow(prescriptionTable, "Ordonnance", currentPrescription.getPrescription(), labelFont, textFont);
            document.add(prescriptionTable);

            Paragraph signatureTitle = new Paragraph("Signature du medecin", sectionFont);
            signatureTitle.setSpacingBefore(20);
            signatureTitle.setAlignment(Element.ALIGN_RIGHT);
            document.add(signatureTitle);
            addSignature(document);

            document.close();
            showAlert("Succes", "Ordonnance PDF exportee avec succes.");
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de l'export PDF : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addInfoRow(PdfPTable table, String label, String value, Font labelFont, Font textFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBackgroundColor(new BaseColor(247, 250, 252));
        labelCell.setPadding(8);
        labelCell.setBorderColor(new BaseColor(226, 232, 240));
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(valueOrDefault(value), textFont));
        valueCell.setPadding(8);
        valueCell.setBorderColor(new BaseColor(226, 232, 240));
        table.addCell(valueCell);
    }

    private String valueOrDefault(String value) {
        return value == null || value.trim().isEmpty() ? "N/A" : value;
    }

    private void addSignature(Document document) throws Exception {
        if (currentDoctor == null || currentDoctor.getImageSignature() == null || currentDoctor.getImageSignature().trim().isEmpty()) {
            Paragraph missingSignature = new Paragraph("Signature non disponible");
            missingSignature.setAlignment(Element.ALIGN_RIGHT);
            document.add(missingSignature);
            return;
        }

        String signaturePath = currentDoctor.getImageSignature();
        com.itextpdf.text.Image signatureImage = null;
        File signatureFile = new File(signaturePath);
        if (signatureFile.exists()) {
            signatureImage = com.itextpdf.text.Image.getInstance(signatureFile.getAbsolutePath());
        } else if (getClass().getResource("/" + signaturePath) != null) {
            signatureImage = com.itextpdf.text.Image.getInstance(getClass().getResource("/" + signaturePath));
        }

        if (signatureImage != null) {
            signatureImage.scaleToFit(160, 80);
            signatureImage.setAlignment(Element.ALIGN_RIGHT);
            document.add(signatureImage);
        } else {
            Paragraph missingSignature = new Paragraph("Signature non disponible");
            missingSignature.setAlignment(Element.ALIGN_RIGHT);
            document.add(missingSignature);
        }
    }

    @FXML
    public void ajouterAGoogleCalendar(ActionEvent event) {
        if (currentPrescription == null || currentPatient == null) return;

        try {
            String title = URLEncoder.encode("💊 Médicament: " + currentPatient.getFullName(), StandardCharsets.UTF_8);
            String description = URLEncoder.encode("Rappel Shifaa - Traitement: " + currentPrescription.getPrescription(), StandardCharsets.UTF_8);
            
            String dateStr = currentPrescription.getPrescriptionDate().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String dates = dateStr + "T080000Z/" + dateStr + "T083000Z";
            
            // Calcul de la durée pour la récurrence
            String durStr = currentPrescription.getTreatmentDuration().replaceAll("[^0-9]", "");
            int count = durStr.isEmpty() ? 7 : Integer.parseInt(durStr);
            String recur = URLEncoder.encode("RRULE:FREQ=DAILY;COUNT=" + count, StandardCharsets.UTF_8);

            String url = "https://www.google.com/calendar/render?action=TEMPLATE" +
                         "&text=" + title +
                         "&details=" + description +
                         "&dates=" + dates +
                         "&recur=" + recur;

            Desktop.getDesktop().browse(new URI(url));
            
        } catch (Exception e) {
            System.err.println("Erreur ouverture Google Calendar: " + e.getMessage());
        }
    }

    @FXML
    public void envoyerSMS(ActionEvent event) {
        if (currentPatient == null || currentPrescription == null) {
            showAlert("Erreur", "Données manquantes pour l'envoi du SMS.");
            return;
        }

        String messageBody = String.format(
            "Bonjour %s %s (ID: %d), votre traitement: %s. Durée: %s.",
            currentPatient.getFirstName(),
            currentPatient.getLastName(),
            currentPatient.getUserId(),
            currentPrescription.getPrescription(),
            currentPrescription.getTreatmentDuration()
        );

        try {
            SmsService.sendSms("+21651342604", messageBody);
            showAlert("Succès", "SMS envoyé avec succès !");
        } catch (Exception e) {
            showAlert("Erreur Twilio", "Échec de l'envoi : " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void retourner(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MesPrescriptions.fxml"));
            Parent root = loader.load();
            
            MesPrescriptionsController controller = loader.getController();
            controller.chargerPrescriptionsPatient(currentPatientId);
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
