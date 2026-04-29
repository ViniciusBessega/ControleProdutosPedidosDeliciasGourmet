package com.gerenciador.sistema_loja.service;

import com.gerenciador.sistema_loja.model.ItemPedido;
import com.gerenciador.sistema_loja.model.Pedido;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.DottedLine;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;

@Service
public class PedidoPdfService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ── PDF A4 ────────────────────────────────────────────
    public void gerarPedidoA4(Pedido pedido, File destino) throws Exception {

        PdfWriter writer = new PdfWriter(destino);
        PdfDocument pdf  = new PdfDocument(writer);
        pdf.addEventHandler(
                com.itextpdf.kernel.events.PdfDocumentEvent.END_PAGE,
                new RodapeEventHandler()
        );
        Document doc     = new Document(pdf, PageSize.A4);
        doc.setMargins(40, 40, 40, 40);

        // ── CABEÇALHO: logo à direita + nome do cliente à esquerda ──
        float[] colsCabecalho = {1, 1};
        Table cabecalho = new Table(UnitValue.createPercentArray(colsCabecalho))
                .useAllAvailableWidth();

        // lado esquerdo: nome e data
        Cell celulaEsq = new Cell()
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                .setVerticalAlignment(com.itextpdf.layout.properties.VerticalAlignment.MIDDLE);

        celulaEsq.add(new Paragraph(pedido.getNomeCliente().toUpperCase())
                .setBold().setFontSize(18));

        if (pedido.getDataEntrega() != null) {
            celulaEsq.add(new Paragraph("Data de entrega: " +
                    pedido.getDataEntrega().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    .setFontSize(10).setFontColor(ColorConstants.GRAY));
        }

        cabecalho.addCell(celulaEsq);

        // lado direito: logo
        Cell celulaDir = new Cell()
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT)
                .setVerticalAlignment(com.itextpdf.layout.properties.VerticalAlignment.MIDDLE);

        try {
            var logoStream = getClass().getResourceAsStream("/logo.png");
            if (logoStream != null) {
                com.itextpdf.io.image.ImageData imageData =
                        com.itextpdf.io.image.ImageDataFactory.create(logoStream.readAllBytes());
                com.itextpdf.layout.element.Image logo =
                        new com.itextpdf.layout.element.Image(imageData);
                logo.setWidth(120);
                logo.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.RIGHT);
                celulaDir.add(logo);
            }
        } catch (Exception ignored) {}

        cabecalho.addCell(celulaDir);
        doc.add(cabecalho);

        doc.add(new LineSeparator(new com.itextpdf.kernel.pdf.canvas.draw.DottedLine())
                .setMarginTop(10).setMarginBottom(10));

        // ── TABELA DE ITENS CENTRALIZADA ──────────────────────
        // usando margens laterais para centralizar visualmente
        float larguraTabela = PageSize.A4.getWidth() - 80 - 60; // margens - recuo extra
        float margemLateral = 30;

        Table tabela = new Table(UnitValue.createPercentArray(new float[]{4, 1, 2, 2}))
                .setWidth(larguraTabela)
                .setMarginLeft(margemLateral)
                .setMarginRight(margemLateral);

        // header
        com.itextpdf.kernel.colors.Color corHeader =
                new com.itextpdf.kernel.colors.DeviceRgb(255, 204, 213);

        for (String h : new String[]{"PRODUTO", "QTD", "UNIT.", "TOTAL"}) {
            tabela.addHeaderCell(
                    new Cell().add(new Paragraph(h).setBold().setFontSize(9))
                            .setBackgroundColor(corHeader)
                            .setTextAlignment(TextAlignment.CENTER)
                            .setPadding(5)
            );
        }

        // linhas
        java.math.BigDecimal subtotalBruto = java.math.BigDecimal.ZERO;

        if (pedido.getItens() != null) {
            for (ItemPedido item : pedido.getItens()) {
                String nome = item.getProduto().getNome();
                int    qtd  = item.getQuantidade().intValue();
                java.math.BigDecimal unit = item.getPrecoUnitario();
                java.math.BigDecimal sub  = unit.multiply(item.getQuantidade())
                        .setScale(2, RoundingMode.HALF_UP);
                subtotalBruto = subtotalBruto.add(sub);

                tabela.addCell(celulaItem(nome, TextAlignment.LEFT));
                tabela.addCell(celulaItem("x" + qtd, TextAlignment.CENTER));
                tabela.addCell(celulaItem("R$ " + unit.setScale(2, RoundingMode.HALF_UP), TextAlignment.CENTER));
                tabela.addCell(celulaItem("R$ " + sub, TextAlignment.CENTER));
            }
        }

        doc.add(tabela);

        // ── OBSERVAÇÃO ────────────────────────────────────────
        if (pedido.getObservacao() != null && !pedido.getObservacao().isBlank()) {
            doc.add(new LineSeparator(new com.itextpdf.kernel.pdf.canvas.draw.DottedLine())
                    .setMarginTop(12).setMarginBottom(6));
            doc.add(new Paragraph("Obs: " + pedido.getObservacao())
                    .setFontSize(10).setFontColor(ColorConstants.DARK_GRAY));
        }

        doc.add(new LineSeparator(new com.itextpdf.kernel.pdf.canvas.draw.DottedLine())
                .setMarginTop(12).setMarginBottom(8));

        // ── TOTAIS ────────────────────────────────────────────
        boolean temDesconto = pedido.getDesconto() != null &&
                pedido.getDesconto().compareTo(BigDecimal.ZERO) > 0;

        if (temDesconto) {
            com.itextpdf.layout.element.Text textoRiscado =
                    new com.itextpdf.layout.element.Text(
                            "Subtotal: R$ " + subtotalBruto.setScale(2, RoundingMode.HALF_UP))
                            .setLineThrough()
                            .setFontColor(ColorConstants.GRAY);
            doc.add(new Paragraph(textoRiscado)
                    .setFontSize(11)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginBottom(2));

            doc.add(new Paragraph("Desconto: - R$ " +
                    pedido.getDesconto().setScale(2, RoundingMode.HALF_UP))
                    .setFontSize(11)
                    .setFontColor(new DeviceRgb(255, 77, 109))
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginBottom(6));
        }

        if (pedido.getTotal() != null) {
            doc.add(new Paragraph("TOTAL: R$ " +
                    pedido.getTotal().setScale(2, RoundingMode.HALF_UP))
                    .setBold()
                    .setFontSize(15)
                    .setFontColor(ColorConstants.BLACK)
                    .setTextAlignment(TextAlignment.RIGHT));
        }

        doc.close();
    }

    // ── COMANDA (1/4 A4 = A6 aprox) ──────────────────────
    public void gerarComanda(Pedido pedido, File destino) throws Exception {

        PdfWriter writer   = new PdfWriter(destino);
        PdfDocument pdf    = new PdfDocument(writer);
        pdf.addNewPage(PageSize.A4);

        float larguraA4 = PageSize.A4.getWidth();
        float alturaA4  = PageSize.A4.getHeight();

        float xInicio = larguraA4 / 2f;
        float largura = larguraA4 / 2f;
        float altura  = alturaA4  / 2f;
        float padding = 15f;

        float[] cursor = { alturaA4 - padding };

        com.itextpdf.kernel.pdf.canvas.PdfCanvas canvas =
                new com.itextpdf.kernel.pdf.canvas.PdfCanvas(pdf.getFirstPage());

        com.itextpdf.kernel.font.PdfFont fontBold =
                com.itextpdf.kernel.font.PdfFontFactory.createFont(
                        com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD);

        com.itextpdf.kernel.font.PdfFont fontNormal =
                com.itextpdf.kernel.font.PdfFontFactory.createFont(
                        com.itextpdf.io.font.constants.StandardFonts.HELVETICA);

        // ── borda pontilhada ao redor do quadrante ──────────
        canvas.setLineDash(3, 3)
                .setLineWidth(0.5f)
                .setStrokeColor(com.itextpdf.kernel.colors.ColorConstants.GRAY)
                .rectangle(xInicio, alturaA4 / 2f, largura, altura)
                .stroke();

        // ── helpers de escrita ───────────────────────────────
        java.util.function.BiFunction<String, Float, Float> escreverLinha = (texto, fontSize) -> {
            float y = cursor[0] - fontSize;
            canvas.beginText()
                    .setFontAndSize(fontNormal, fontSize)
                    .moveText(xInicio + padding, y)
                    .showText(texto)
                    .endText();
            cursor[0] = y - 4f;
            return cursor[0];
        };

        java.util.function.BiFunction<String, Float, Float> escreverLinhaBold = (texto, fontSize) -> {
            float y = cursor[0] - fontSize;
            canvas.beginText()
                    .setFontAndSize(fontBold, fontSize)
                    .moveText(xInicio + padding, y)
                    .showText(texto)
                    .endText();
            cursor[0] = y - 4f;
            return cursor[0];
        };

        // ── NOME DO CLIENTE ─────────────────────────────────
        escreverLinhaBold.apply(pedido.getNomeCliente().toUpperCase(), 11f);

        // ── DATA ────────────────────────────────────────────
        if (pedido.getData() != null) {
            escreverLinha.apply(pedido.getData().format(FMT), 8f);
        }

        // ── DATA DE ENTREGA ─────────────────────────────────
        if (pedido.getDataEntrega() != null) {
            escreverLinha.apply("Entrega: " + pedido.getDataEntrega()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), 8f);
        }

        // ── LINHA TRACEJADA ─────────────────────────────────
        cursor[0] -= 4f;
        canvas.setLineDash(2, 2)
                .setLineWidth(0.4f)
                .setStrokeColor(com.itextpdf.kernel.colors.ColorConstants.GRAY)
                .moveTo(xInicio + padding, cursor[0])
                .lineTo(xInicio + largura - padding, cursor[0])
                .stroke();
        cursor[0] -= 6f;

        // ── ITENS ───────────────────────────────────────────
        if (pedido.getItens() != null) {
            for (ItemPedido item : pedido.getItens()) {
                String linha = item.getProduto().getNome()
                        + "  x" + item.getQuantidade().intValue();
                escreverLinha.apply(linha, 9f);
            }
        }

        // ── OBSERVAÇÃO ──────────────────────────────────────
        if (pedido.getObservacao() != null && !pedido.getObservacao().isBlank()) {
            cursor[0] -= 4f;
            canvas.setLineDash(2, 2)
                    .setLineWidth(0.4f)
                    .setStrokeColor(com.itextpdf.kernel.colors.ColorConstants.GRAY)
                    .moveTo(xInicio + padding, cursor[0])
                    .lineTo(xInicio + largura - padding, cursor[0])
                    .stroke();
            cursor[0] -= 6f;

            escreverLinha.apply("Obs: " + pedido.getObservacao(), 8f);
        }

        // ── fecha só o PdfDocument, sem Document wrapper ────
        canvas.release();
        pdf.close();
    }

    private Cell celulaItem(String texto, TextAlignment align) {
        return new Cell()
                .add(new Paragraph(texto).setFontSize(10))
                .setTextAlignment(align)
                .setPadding(4);
    }

    private static class RodapeEventHandler
            implements com.itextpdf.kernel.events.IEventHandler {

        private final String texto = "Desenvolvido por Vinicius Bessega \u00A9";

        @Override
        public void handleEvent(com.itextpdf.kernel.events.Event event) {
            com.itextpdf.kernel.events.PdfDocumentEvent docEvent =
                    (com.itextpdf.kernel.events.PdfDocumentEvent) event;

            com.itextpdf.kernel.pdf.PdfDocument pdfDoc = docEvent.getDocument();
            com.itextpdf.kernel.pdf.PdfPage page = docEvent.getPage();

            try {
                com.itextpdf.kernel.font.PdfFont font =
                        com.itextpdf.kernel.font.PdfFontFactory.createFont(
                                com.itextpdf.io.font.constants.StandardFonts.HELVETICA);

                com.itextpdf.kernel.pdf.canvas.PdfCanvas canvas =
                        new com.itextpdf.kernel.pdf.canvas.PdfCanvas(page);

                float largura = page.getPageSize().getWidth();
                float texto_largura = font.getWidth(texto, 7);

                canvas.beginText()
                        .setFontAndSize(font, 7)
                        .setColor(new com.itextpdf.kernel.colors.DeviceGray(0.6f), true)
                        .moveText(largura - 40 - texto_largura, 20)
                        .showText(texto)
                        .endText()
                        .release();

            } catch (Exception ignored) {}
        }
    }

    private static final java.util.prefs.Preferences PREFS =
            java.util.prefs.Preferences.userNodeForPackage(PedidoPdfService.class);
    private static final String PREF_ULTIMO_DIR = "ultimo_diretorio_pdf";
}