package com.sara.api.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import com.sara.api.model.Produto;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class ProdutoPdfService {

    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    private final Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
    private final Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
    private final Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
    private final Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);
    private final Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY);

    private static final String LOGO_PATH = "/images/logo_sara_menor.jpeg";

    public byte[] generateCatalogoPdf(List<Produto> produtos) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, out);

        document.open();
        addHeader(document);
        addTitle(document);
        addProductTable(document, produtos);
        addFooterInfo(document);

        document.close();
        return out.toByteArray();
    }

    private void addHeader(Document document) {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        try {
            table.setWidths(new float[] { 1, 2 });
        } catch (Exception e) {}

        // Logo ou Texto SARA
        try {
            java.net.URL logoUrl = getClass().getResource(LOGO_PATH);
            if (logoUrl != null) {
                Image img = Image.getInstance(logoUrl);
                img.scaleToFit(80, 80);
                PdfPCell logoCell = new PdfPCell(img);
                logoCell.setBorder(Rectangle.NO_BORDER);
                table.addCell(logoCell);
            } else {
                PdfPCell logoCell = new PdfPCell(new Phrase("SARA", headerFont));
                logoCell.setBorder(Rectangle.NO_BORDER);
                table.addCell(logoCell);
            }
        } catch (Exception e) {
            PdfPCell logoCell = new PdfPCell(new Phrase("SARA", headerFont));
            logoCell.setBorder(Rectangle.NO_BORDER);
            table.addCell(logoCell);
        }

        PdfPCell infoCell = new PdfPCell();
        infoCell.setBorder(Rectangle.NO_BORDER);
        infoCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        infoCell.addElement(new Paragraph("CATÁLOGO DE PRODUTOS", titleFont));
        String dataH = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        infoCell.addElement(new Paragraph("Gerado em: " + dataH, smallFont));
        table.addCell(infoCell);

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    private void addTitle(Document document) {
        Paragraph p = new Paragraph("LISTA PARA RASCUNHO DE PEDIDO", boldFont);
        p.setAlignment(Element.ALIGN_CENTER);
        document.add(p);
        Paragraph p2 = new Paragraph("Utilize esta lista para anotar as quantidades desejadas antes de lançar no sistema.", smallFont);
        p2.setAlignment(Element.ALIGN_CENTER);
        document.add(p2);
        document.add(new Paragraph("\n"));
    }

    private void addProductTable(Document document, List<Produto> produtos) {
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        try {
            table.setWidths(new float[] { 2, 5, 2, 2, 3 });
        } catch (Exception e) {}

        addCell(table, "CÓDIGO", boldFont, Color.LIGHT_GRAY, Element.ALIGN_CENTER);
        addCell(table, "PRODUTO", boldFont, Color.LIGHT_GRAY, Element.ALIGN_LEFT);
        addCell(table, "TAM.", boldFont, Color.LIGHT_GRAY, Element.ALIGN_CENTER);
        addCell(table, "QTD", boldFont, Color.YELLOW, Element.ALIGN_CENTER); // Destaque para preenchimento
        addCell(table, "Valor", boldFont, Color.LIGHT_GRAY, Element.ALIGN_CENTER);

        for (Produto p : produtos) {
            addCell(table, String.valueOf(p.getCodigo()), normalFont, null, Element.ALIGN_CENTER);
            addCell(table, p.getNome(), normalFont, null, Element.ALIGN_LEFT);
            addCell(table, p.getTamanho() + "cm", normalFont, null, Element.ALIGN_CENTER);
            addCell(table, " ", normalFont, null, Element.ALIGN_CENTER); // Espaço vazio para preenchimento manual
            addCell(table, currencyFormatter.format(p.getPreco()), normalFont, null, Element.ALIGN_RIGHT);
        }

        document.add(table);
    }

    private void addFooterInfo(Document document) {
        document.add(new Paragraph("\n"));
        Paragraph p = new Paragraph("Este documento é apenas para fins de conferência e rascunho.", smallFont);
        p.setAlignment(Element.ALIGN_CENTER);
        document.add(p);
    }

    private void addCell(PdfPTable table, String text, Font font, Color bgColor, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        if (bgColor != null) {
            cell.setBackgroundColor(bgColor);
        }
        table.addCell(cell);
    }
}
