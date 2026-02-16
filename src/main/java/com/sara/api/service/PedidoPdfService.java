package com.sara.api.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.sara.api.model.Pedido;
import com.sara.api.model.PedidoProduto;
import com.sara.api.model.Usuario;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

@Service
public class PedidoPdfService {

    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    private final Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
    private final Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
    private final Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
    private final Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);

    private static final String LOGO_PATH = "/images/logo-sara.png";

    public byte[] generatePedidoPdf(Pedido pedido) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, out);

        document.open();

        addHeader(document);
        addOrderInfo(document, pedido);
        addUserInfo(document, pedido.getUsuario());
        addProductTable(document, pedido);
        addObservations(document, pedido);
        addFinancialSummary(document, pedido);

        document.close();
        return out.toByteArray();
    }

    private void addHeader(Document document) {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        try {
            table.setWidths(new float[] { 1, 2 });
        } catch (Exception e) {
        }

        // Logo Image
        try {
            java.net.URL logoUrl = getClass().getResource(LOGO_PATH);
            if (logoUrl != null) {
                Image img = Image.getInstance(logoUrl);
                img.scaleToFit(100, 100);
                PdfPCell logoCell = new PdfPCell(img);
                logoCell.setBorder(Rectangle.NO_BORDER);
                logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                table.addCell(logoCell);
            } else {
                PdfPCell logoCell = new PdfPCell(new Phrase("SARA", headerFont));
                logoCell.setBorder(Rectangle.NO_BORDER);
                logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                table.addCell(logoCell);
            }
        } catch (Exception e) {
            PdfPCell logoCell = new PdfPCell(new Phrase("SARA", headerFont));
            logoCell.setBorder(Rectangle.NO_BORDER);
            logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(logoCell);
        }

        PdfPCell contactCell = new PdfPCell();
        contactCell.setBorder(Rectangle.NO_BORDER);
        contactCell.addElement(new Paragraph("Sara - Artigos Religiosos", titleFont));
        contactCell.addElement(new Paragraph("Contato: (00) 0000-0000", normalFont));
        contactCell.addElement(new Paragraph("E-mail: contato@sara.com.br", normalFont));
        table.addCell(contactCell);

        document.add(table);
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("\n"));
    }

    private void addOrderInfo(Document document, Pedido pedido) {
        Paragraph p = new Paragraph("PEDIDO #" + pedido.getId(), headerFont);
        p.setAlignment(Element.ALIGN_CENTER);
        document.add(p);
        document.add(new Paragraph("\n"));
    }

    private void addUserInfo(Document document, Usuario usuario) {
        document.add(new Paragraph("DADOS DO CLIENTE", titleFont));
        document.add(new Paragraph("Nome: " + usuario.getNome(), normalFont));

        String enderecoFormatado = String.format("%s, %s - %s",
                usuario.getEndereco(),
                usuario.getNumero(),
                usuario.getBairro());
        document.add(new Paragraph("Endereço: " + enderecoFormatado, normalFont));
        document.add(new Paragraph(String.format("Cidade/UF: %s/%s - CEP: %s",
                usuario.getCidade(),
                usuario.getUf(),
                usuario.getCep()), normalFont));
        document.add(new Paragraph("\n"));
    }

    private void addProductTable(Document document, Pedido pedido) {
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        try {
            table.setWidths(new float[] { 3, 1, 1, 2, 2 });
        } catch (Exception e) {
        }

        addCell(table, "Produto", boldFont, Color.LIGHT_GRAY);
        addCell(table, "Tam.", boldFont, Color.LIGHT_GRAY);
        addCell(table, "Qtd.", boldFont, Color.LIGHT_GRAY);
        addCell(table, "V. Unitário", boldFont, Color.LIGHT_GRAY);
        addCell(table, "Total", boldFont, Color.LIGHT_GRAY);

        for (PedidoProduto item : pedido.getProdutos()) {
            addCell(table, item.getProduto().getNome(), normalFont, null);
            addCell(table, String.valueOf(item.getProduto().getTamanho()), normalFont, null);
            addCell(table, String.valueOf(item.getQuantidade()), normalFont, null);
            addCell(table, currencyFormatter.format(item.getValor()), normalFont, null);

            BigDecimal bTotal = item.getValor().multiply(item.getQuantidade());
            addCell(table, currencyFormatter.format(bTotal), normalFont, null);
        }

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    private void addObservations(Document document, Pedido pedido) {
        if (pedido.getObservacao() != null && !pedido.getObservacao().isEmpty()) {
            document.add(new Paragraph("OBSERVAÇÕES", titleFont));
            document.add(new Paragraph(pedido.getObservacao(), normalFont));
            document.add(new Paragraph("\n"));
        }
    }

    private void addFinancialSummary(Document document, Pedido pedido) {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(40);
        table.setHorizontalAlignment(Element.ALIGN_RIGHT);

        addSummaryRow(table, "Frete:", pedido.getFrete());
        addSummaryRow(table, "Desconto:", pedido.getDesconto());
        addSummaryRow(table, "VALOR TOTAL:", pedido.getValorTotal(), boldFont);

        document.add(table);
    }

    private void addCell(PdfPTable table, String text, Font font, Color bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        if (bgColor != null) {
            cell.setBackgroundColor(bgColor);
        }
        table.addCell(cell);
    }

    private void addSummaryRow(PdfPTable table, String label, BigDecimal value) {
        addSummaryRow(table, label, value, normalFont);
    }

    private void addSummaryRow(PdfPTable table, String label, BigDecimal value, Font font) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(labelCell);

        String textValue = (value != null) ? currencyFormatter.format(value) : currencyFormatter.format(0);
        PdfPCell valueCell = new PdfPCell(new Phrase(textValue, font));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(valueCell);
    }
}
