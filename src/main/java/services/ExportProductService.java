package services;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import entities.Product;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ExportProductService {

    public void exportToExcel(List<Product> products, File file) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Liste des Produits");

        // Header style
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        String[] headers = {
            "ID", "Nom", "Description", "Dosage", "Forme", "Laboratoire", 
            "Classe Thérap.", "Prix (DT)", "Catégorie", "Quantité", "Statut", 
            "Mise à jour", "Pharmacie ID", "Image Path"
        };

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowIdx = 1;
        for (Product p : products) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(p.getProductId());
            row.createCell(1).setCellValue(p.getProductName());
            row.createCell(2).setCellValue(p.getDescription());
            row.createCell(3).setCellValue(p.getDosage());
            row.createCell(4).setCellValue(p.getForm());
            row.createCell(5).setCellValue(p.getLaboratory());
            row.createCell(6).setCellValue(p.getTherapeuticClass());
            row.createCell(7).setCellValue(p.getPrice());
            row.createCell(8).setCellValue(p.getCategory());
            row.createCell(9).setCellValue(p.getAvailableQuantity());
            row.createCell(10).setCellValue(p.getAvailabilityStatus());
            row.createCell(11).setCellValue(p.getLastUpdateDate() != null ? p.getLastUpdateDate().toString() : "-");
            row.createCell(12).setCellValue(p.getPharmacyId());
            row.createCell(13).setCellValue(p.getImage());
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        try (FileOutputStream fileOut = new FileOutputStream(file)) {
            workbook.write(fileOut);
        }
        workbook.close();
    }

    public void exportToPdf(List<Product> products, File file) throws Exception {
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        // Title
        com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
        Paragraph title = new Paragraph("Liste des Produits SHIFAA", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        document.add(new Paragraph("Date d'exportation : " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date())));
        document.add(new Paragraph(" ")); // Spacer

        PdfPTable table = new PdfPTable(7); // Show only essential columns for visibility
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.5f, 2.5f, 1.5f, 1.5f, 1f, 1f, 2f});

        String[] headers = {"Image", "Nom / Description", "Dosage", "Laboratoire", "Prix", "Stock", "Catégorie"};
        
        for (String h : headers) {
            PdfPCell header = new PdfPCell(new Paragraph(h, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
            header.setBackgroundColor(BaseColor.LIGHT_GRAY);
            header.setHorizontalAlignment(Element.ALIGN_CENTER);
            header.setPadding(5);
            table.addCell(header);
        }

        for (Product p : products) {
            // Image Cell
            PdfPCell imgCell = new PdfPCell();
            try {
                if (p.getImage() != null && !p.getImage().isEmpty() && new File(p.getImage()).exists()) {
                    Image img = Image.getInstance(p.getImage());
                    img.scaleToFit(50, 50);
                    imgCell.addElement(img);
                } else {
                    imgCell.addElement(new Paragraph("Pas d'image", FontFactory.getFont(FontFactory.HELVETICA, 8)));
                }
            } catch (Exception e) {
                imgCell.addElement(new Paragraph("Erreur image", FontFactory.getFont(FontFactory.HELVETICA, 8)));
            }
            imgCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            imgCell.setPadding(5);
            table.addCell(imgCell);

            // Details Cell
            String details = p.getProductName() + "\n" + (p.getDescription() != null ? p.getDescription() : "");
            table.addCell(new PdfPCell(new Paragraph(details, FontFactory.getFont(FontFactory.HELVETICA, 9))));
            
            table.addCell(new PdfPCell(new Paragraph(p.getDosage(), FontFactory.getFont(FontFactory.HELVETICA, 9))));
            table.addCell(new PdfPCell(new Paragraph(p.getLaboratory(), FontFactory.getFont(FontFactory.HELVETICA, 9))));
            table.addCell(new PdfPCell(new Paragraph(String.format("%.3f DT", p.getPrice()), FontFactory.getFont(FontFactory.HELVETICA, 9))));
            table.addCell(new PdfPCell(new Paragraph(String.valueOf(p.getAvailableQuantity()), FontFactory.getFont(FontFactory.HELVETICA, 9))));
            table.addCell(new PdfPCell(new Paragraph(p.getCategory(), FontFactory.getFont(FontFactory.HELVETICA, 9))));
        }

        document.add(table);
        document.close();
    }
}
