package com.sara.api.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import com.sara.api.model.Pedido;
import com.sara.api.model.PedidoProduto;
import com.sara.api.model.Usuario;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class PedidoPdfService {

    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    private final Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
    private final Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
    private final Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
    private final Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);

    private static final String LOGO_PATH = "/images/logo_sara_menor.jpeg";

    public byte[] generatePedidoPdf(Pedido pedido) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, out);

        document.open();

        if (Boolean.TRUE.equals(pedido.getCancelado())) {
            addWatermark(writer);
        }

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
        contactCell.addElement(new Paragraph("Sara Imagens", titleFont));
        contactCell.addElement(new Paragraph("E-mail: atendimento@saraimagens.com.br", normalFont));
        table.addCell(contactCell);

        document.add(table);
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("\n"));
    }

    private void addOrderInfo(Document document, Pedido pedido) {
        Paragraph p = new Paragraph("PEDIDO #" + pedido.getNumero(), headerFont);
        p.setAlignment(Element.ALIGN_CENTER);
        document.add(p);

        if (pedido.getDataPedido() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            Paragraph pData = new Paragraph("Data: " + pedido.getDataPedido().format(formatter), boldFont);
            pData.setAlignment(Element.ALIGN_CENTER);
            document.add(pData);
        }

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
        
        document.add(new Paragraph("CPF/CNPJ: " + formatCpfCnpj(usuario.getCpfCnpj()), normalFont));
        document.add(new Paragraph("E-mail: " + usuario.getEmail(), normalFont));

        document.add(new Paragraph("\n"));
    }

    private void addProductTable(Document document, Pedido pedido) {
        // Tabela externa (container) de 2 colunas para colocar a tabela principal e a tabela de resumo lado a lado
        PdfPTable containerTable = new PdfPTable(2);
        containerTable.setWidthPercentage(100);
        try {
            // Ocupamos 72.0% para a tabela de produtos e 28.0% para a de categorias
            containerTable.setWidths(new float[] { 72.0f, 28.0f });
        } catch (Exception e) {
        }

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        try {
            // New widths: Cód(0.8), Qtd(0.5), Produto(2.2), T(0.4), Unt(1), Total(1)
            table.setWidths(new float[] { 0.8f, 0.5f, 2.2f, 0.4f, 1f, 1f });
        } catch (Exception e) {
        }

        addCell(table, "Cód.", boldFont, Color.LIGHT_GRAY);
        addCell(table, "Qtd", boldFont, Color.LIGHT_GRAY, Element.ALIGN_CENTER);
        addCell(table, "Produto", boldFont, Color.LIGHT_GRAY);
        addCell(table, "T", boldFont, Color.LIGHT_GRAY);
        addCell(table, "Unt.", boldFont, Color.LIGHT_GRAY, Element.ALIGN_RIGHT);
        addCell(table, "Total", boldFont, Color.LIGHT_GRAY, Element.ALIGN_RIGHT);

        java.util.List<PedidoProduto> produtos = new java.util.ArrayList<>(pedido.getProdutos());
        produtos.sort(java.util.Comparator
                .comparing((PedidoProduto p) -> p.getProduto().getCodigo(), java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()))
                .thenComparing(p -> p.getProduto().getNome().toLowerCase())
                .thenComparing(p -> p.getProduto().getTamanho())
                .thenComparing(p -> p.getValor()));

        // Pre-calculate max integer parts length for alignment
        int maxIntPartsUnit = 0;
        int maxIntPartsTotal = 0;
        for (PedidoProduto item : produtos) {
            maxIntPartsUnit = Math.max(maxIntPartsUnit, getIntegerPartLength(item.getValor()));
            BigDecimal bTotal = item.getValor().multiply(item.getQuantidade());
            maxIntPartsTotal = Math.max(maxIntPartsTotal, getIntegerPartLength(bTotal));
        }

        for (PedidoProduto item : produtos) {
            addCell(table, item.getProduto().getCodigo(), normalFont, null);
            addCell(table, String.format("%02d", item.getQuantidade().intValue()), normalFont, null, Element.ALIGN_CENTER);
            addCell(table, item.getProduto().getNome(), normalFont, null);
            addCell(table, String.format("%02d", item.getProduto().getTamanho()), normalFont, null);
            
            addCell(table, formatCurrencyWithSpaces(item.getValor(), maxIntPartsUnit), normalFont, null, Element.ALIGN_RIGHT);

            BigDecimal bTotal = item.getValor().multiply(item.getQuantidade());
            addCell(table, formatCurrencyWithSpaces(bTotal, maxIntPartsTotal), normalFont, null, Element.ALIGN_RIGHT);
        }

        // Célula para a tabela principal
        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(Rectangle.NO_BORDER);
        leftCell.setPaddingRight(10f); // Afasta a tabela esquerda da direita
        leftCell.setVerticalAlignment(Element.ALIGN_TOP);
        leftCell.addElement(table);
        containerTable.addCell(leftCell);

        // Tabela de resumo por categoria (coluna da direita)
        PdfPTable summaryTable = createCategorySummaryTable(pedido);
        PdfPTable summaryUnitTable = createCategoryUnitValueTable(pedido);

        // Célula para a tabela de resumo
        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setPaddingLeft(10f); // Afasta a tabela direita da esquerda
        rightCell.setVerticalAlignment(Element.ALIGN_TOP);
        rightCell.addElement(summaryTable);
        rightCell.addElement(new Paragraph("\n")); // Espaçamento vertical entre tabelas
        rightCell.addElement(summaryUnitTable);
        containerTable.addCell(rightCell);

        document.add(containerTable);
        document.add(new Paragraph("\n"));
    }

    private PdfPTable createCategoryUnitValueTable(Pedido pedido) {
        PdfPTable unitTable = new PdfPTable(2);
        unitTable.setWidthPercentage(100);
        try {
            // Cat. (0.5f) e Vl. Un. (1.625f) para alinhar com a tabela superior (0.5f + 1.125f)
            unitTable.setWidths(new float[] { 0.5f, 1.625f });
        } catch (Exception e) {
        }

        // Título principal do cabeçalho
        PdfPCell titleCell = new PdfPCell(new Phrase("Valor Un. Categoria", boldFont));
        titleCell.setColspan(2);
        titleCell.setPadding(5);
        titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        titleCell.setBackgroundColor(Color.LIGHT_GRAY);
        unitTable.addCell(titleCell);

        // Cabeçalhos de coluna
        addCell(unitTable, "Cat.", boldFont, Color.LIGHT_GRAY, Element.ALIGN_LEFT);
        addCell(unitTable, "Vl. Un.", boldFont, Color.LIGHT_GRAY, Element.ALIGN_RIGHT);

        java.util.Map<String, CategorySummary> map = new java.util.TreeMap<>();
        for (PedidoProduto item : pedido.getProdutos()) {
            String codigo = item.getProduto().getCodigo();
            String categoria = "";
            if (codigo != null && codigo.length() >= 3) {
                categoria = codigo.substring(0, 3);
            } else if (codigo != null) {
                categoria = codigo;
            } else {
                categoria = "N/A";
            }

            CategorySummary summary = map.computeIfAbsent(categoria, k -> new CategorySummary());
            summary.quantidade = summary.quantidade.add(item.getQuantidade());
            summary.valorTotal = summary.valorTotal.add(item.getValor().multiply(item.getQuantidade()));
        }

        for (java.util.Map.Entry<String, CategorySummary> entry : map.entrySet()) {
            addCell(unitTable, entry.getKey(), normalFont, null, Element.ALIGN_LEFT);
            
            BigDecimal media = BigDecimal.ZERO;
            if (entry.getValue().quantidade.compareTo(BigDecimal.ZERO) > 0) {
                media = entry.getValue().valorTotal.divide(entry.getValue().quantidade, 2, java.math.RoundingMode.HALF_UP);
            }
            
            addCell(unitTable, currencyFormatter.format(media), normalFont, null, Element.ALIGN_RIGHT);
        }

        return unitTable;
    }

    private PdfPTable createCategorySummaryTable(Pedido pedido) {
        PdfPTable summaryTable = new PdfPTable(3);
        summaryTable.setWidthPercentage(100);
        try {
            summaryTable.setWidths(new float[] { 0.5f, 0.5f, 1.125f });
        } catch (Exception e) {
        }

        // Título principal do cabeçalho
        PdfPCell titleCell = new PdfPCell(new Phrase("Itens por Categoria", boldFont));
        titleCell.setColspan(3);
        titleCell.setPadding(5);
        titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        titleCell.setBackgroundColor(Color.LIGHT_GRAY);
        summaryTable.addCell(titleCell);

        // Cabeçalhos de coluna
        addCell(summaryTable, "Cat.", boldFont, Color.LIGHT_GRAY, Element.ALIGN_LEFT);
        addCell(summaryTable, "Qt", boldFont, Color.LIGHT_GRAY, Element.ALIGN_CENTER);
        addCell(summaryTable, "Valor", boldFont, Color.LIGHT_GRAY, Element.ALIGN_RIGHT);

        java.util.Map<String, CategorySummary> map = new java.util.TreeMap<>();
        for (PedidoProduto item : pedido.getProdutos()) {
            String codigo = item.getProduto().getCodigo();
            String categoria = "";
            if (codigo != null && codigo.length() >= 3) {
                categoria = codigo.substring(0, 3);
            } else if (codigo != null) {
                categoria = codigo;
            } else {
                categoria = "N/A";
            }

            CategorySummary summary = map.computeIfAbsent(categoria, k -> new CategorySummary());
            summary.quantidade = summary.quantidade.add(item.getQuantidade());
            summary.valorTotal = summary.valorTotal.add(item.getValor().multiply(item.getQuantidade()));
        }

        for (java.util.Map.Entry<String, CategorySummary> entry : map.entrySet()) {
            addCell(summaryTable, entry.getKey(), normalFont, null, Element.ALIGN_LEFT);
            String qtStr = String.format("%02d", entry.getValue().quantidade.intValue());
            addCell(summaryTable, qtStr, normalFont, null, Element.ALIGN_CENTER);
            addCell(summaryTable, currencyFormatter.format(entry.getValue().valorTotal), normalFont, null, Element.ALIGN_RIGHT);
        }

        return summaryTable;
    }

    private static class CategorySummary {
        BigDecimal quantidade = BigDecimal.ZERO;
        BigDecimal valorTotal = BigDecimal.ZERO;
    }

    private int getIntegerPartLength(BigDecimal value) {
        if (value == null) return 1;
        String formatted = currencyFormatter.format(value);
        int firstDigitIdx = -1;
        for (int i = 0; i < formatted.length(); i++) {
            if (Character.isDigit(formatted.charAt(i))) {
                firstDigitIdx = i;
                break;
            }
        }
        if (firstDigitIdx == -1) return 1;
        String numericPart = formatted.substring(firstDigitIdx);
        int commaIdx = numericPart.indexOf(',');
        return (commaIdx == -1) ? numericPart.length() : commaIdx;
    }

    private String formatCurrencyWithSpaces(BigDecimal value, int maxIntPartsLength) {
        if (value == null) value = BigDecimal.ZERO;
        String formatted = currencyFormatter.format(value);
        
        int firstDigitIdx = -1;
        for (int i = 0; i < formatted.length(); i++) {
            if (Character.isDigit(formatted.charAt(i))) {
                firstDigitIdx = i;
                break;
            }
        }
        
        if (firstDigitIdx == -1) return formatted;
        
        String prefix = formatted.substring(0, firstDigitIdx);
        String numericPart = formatted.substring(firstDigitIdx);
        
        int commaIdx = numericPart.indexOf(',');
        int currentIntPartsLength = (commaIdx == -1) ? numericPart.length() : commaIdx;
        
        int spacesToAdd = maxIntPartsLength - currentIntPartsLength;
        
        StringBuilder sb = new StringBuilder(prefix);
        for (int i = 0; i < spacesToAdd; i++) {
            sb.append(" ");
        }
        sb.append(numericPart);
        return sb.toString();
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

        // Calculate total of products (without discount/freight)
        BigDecimal totalProdutos = pedido.getProdutos().stream()
                .map(item -> item.getValor().multiply(item.getQuantidade()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        addSummaryRow(table, "Total dos Produtos:", totalProdutos);
        addSummaryRow(table, "Frete:", pedido.getFrete());

        // Custom row for discount percentage (String)
        PdfPCell labelCell = new PdfPCell(new Phrase("Desconto:", normalFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(labelCell);

        String descontoStr = pedido.getDesconto() != null ? pedido.getDesconto().toString() + "%" : "0%";
        PdfPCell valueCell = new PdfPCell(new Phrase(descontoStr, normalFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(valueCell);

        addSummaryRow(table, "VALOR TOTAL:", pedido.getValorTotal(), boldFont);

        document.add(table);
    }

    private void addCell(PdfPTable table, String text, Font font, Color bgColor) {
        addCell(table, text, font, bgColor, Element.ALIGN_LEFT);
    }

    private void addCell(PdfPTable table, String text, Font font, Color bgColor, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        cell.setHorizontalAlignment(alignment);
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

    private void addWatermark(PdfWriter writer) {
        try {
            PdfContentByte cb = writer.getDirectContentUnder();
            BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
            cb.beginText();
            cb.setFontAndSize(bf, 100);
            cb.setRGBColorFill(220, 220, 220); // Cinza claro
            cb.showTextAligned(Element.ALIGN_CENTER, "CANCELADO", 297, 421, 45); // 45 graus, centro da página A4
            cb.endText();
        } catch (Exception e) {
            // Se falhar ao adicionar marca d'água, continua gerando o PDF normalmente
        }
    }

    private String formatCpfCnpj(String cpfCnpj) {
        if (cpfCnpj == null) return "";
        String clean = cpfCnpj.replaceAll("\\D", "");
        if (clean.length() == 11) {
            return clean.replaceAll("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
        } else if (clean.length() == 14) {
            return clean.replaceAll("(\\d{2})(\\d{3})(\\d{3})(\\d{4})(\\d{2})", "$1.$2.$3/$4-$5");
        }
        return cpfCnpj;
    }
}
