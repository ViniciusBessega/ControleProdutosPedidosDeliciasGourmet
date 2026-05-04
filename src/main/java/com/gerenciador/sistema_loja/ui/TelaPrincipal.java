package com.gerenciador.sistema_loja.ui;

import com.gerenciador.sistema_loja.model.ItemPedido;
import com.gerenciador.sistema_loja.model.Pedido;
import com.gerenciador.sistema_loja.model.Produto;
import com.gerenciador.sistema_loja.model.tiposproduto.ProdutoSimples;
import com.gerenciador.sistema_loja.model.tiposproduto.Torta;
import com.gerenciador.sistema_loja.service.CarrinhoService;
import com.gerenciador.sistema_loja.service.PedidoPdfService;
import com.gerenciador.sistema_loja.service.PedidoService;
import com.gerenciador.sistema_loja.service.ProdutoService;
import com.gerenciador.sistema_loja.ui.util.BotaoFactory;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;

import java.math.BigDecimal;
import java.math.RoundingMode;
import javafx.scene.control.ButtonBar;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TelaPrincipal {

    private ProdutoService produtoService;
    private StackPane rootPrincipal;
    private CarrinhoService carrinhoService;
    private PedidoService pedidoService;
    private PedidoPdfService pedidoPdfService;

    public TelaPrincipal(StackPane rootPrincipal, ProdutoService produtoService,
                         CarrinhoService carrinhoService, PedidoService pedidoService,
                         PedidoPdfService pedidoPdfService) {
        this.rootPrincipal  = rootPrincipal;
        this.produtoService = produtoService;
        this.carrinhoService = carrinhoService;
        this.pedidoService  = pedidoService;
        this.pedidoPdfService = pedidoPdfService;
    }

    public Parent criarTela() {

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #ffe4ec;");

        // ── TOPO ──────────────────────────────────────────
        BorderPane topoContainer = new BorderPane();
        topoContainer.setPadding(new Insets(15, 15, 0, 15));

        ImageView logo = new ImageView(new Image("/logo.png"));
        logo.setFitHeight(150);
        logo.setPreserveRatio(true);

        HBox logoBox = new HBox(logo);
        logoBox.setAlignment(Pos.CENTER_LEFT);
        logoBox.setStyle("-fx-cursor: hand;");
        logoBox.setOnMouseClicked(e -> rootPrincipal.getChildren().setAll(this.criarTela()));

        HBox centro = new HBox(15);
        centro.setAlignment(Pos.CENTER);

        Button btnDoces = BotaoFactory.primario("Doces");
        btnDoces.setOnAction(e -> {
            TelaDoces tela = new TelaDoces(rootPrincipal, produtoService, carrinhoService, pedidoService, pedidoPdfService);
            rootPrincipal.getChildren().setAll(tela.criarTela());
        });

        Button btnSalgados = BotaoFactory.primario("Salgados");
        btnSalgados.setOnAction(e -> {
            TelaSalgados tela = new TelaSalgados(rootPrincipal, produtoService, carrinhoService, pedidoService, pedidoPdfService);
            rootPrincipal.getChildren().setAll(tela.criarTela());
        });

        Button btnTortas = BotaoFactory.primario("Tortas");
        btnTortas.setOnAction(e -> {
            TelaTortas tela = new TelaTortas(rootPrincipal, produtoService, carrinhoService, pedidoService, pedidoPdfService);
            rootPrincipal.getChildren().setAll(tela.criarTela());
        });

        centro.getChildren().addAll(btnDoces, btnSalgados, btnTortas);

        Button btnEditarProdutos = BotaoFactory.secundario("Editar/Adicionar Produtos");
        btnEditarProdutos.setOnAction(e -> {
            TelaGerenciarProdutos tela = new TelaGerenciarProdutos(rootPrincipal, produtoService, carrinhoService, pedidoService, pedidoPdfService);
            rootPrincipal.getChildren().setAll(tela.criarTela());
        });

        Button btnPedidos = BotaoFactory.secundario("Pedidos");
        btnPedidos.setOnAction(e -> {
            TelaPedidos tela = new TelaPedidos(rootPrincipal, produtoService, carrinhoService, pedidoService, pedidoPdfService);
            rootPrincipal.getChildren().setAll(tela.criarTela());
        });

        btnEditarProdutos.setMaxWidth(Double.MAX_VALUE);
        btnPedidos.setMaxWidth(Double.MAX_VALUE);

        VBox botoesDir = new VBox(8, btnEditarProdutos, btnPedidos);
        botoesDir.setAlignment(Pos.CENTER);
        botoesDir.setMinWidth(200);

        topoContainer.setLeft(logoBox);
        topoContainer.setCenter(centro);
        topoContainer.setRight(botoesDir);

        BorderPane.setAlignment(logoBox,   Pos.CENTER);
        BorderPane.setAlignment(centro,    Pos.CENTER);
        BorderPane.setAlignment(botoesDir, Pos.CENTER);

        // ── NOME DO CLIENTE ────────────────────────────────
        Label lblNomeCliente = new Label("Nome do cliente");
        lblNomeCliente.setStyle("-fx-font-size: 11px; -fx-text-fill: #888; -fx-font-weight: bold;");

        TextField nomeCliente = new TextField(carrinhoService.getNomeCliente());
        nomeCliente.setPromptText("Nome do cliente");
        nomeCliente.setStyle("""
            -fx-background-radius: 8; -fx-border-radius: 8;
            -fx-border-color: #ffd1dc; -fx-padding: 8 12; -fx-font-size: 13px;
        """);
        nomeCliente.textProperty().addListener((obs, o, n) -> carrinhoService.setNomeCliente(n));

        VBox campoNomeBox = new VBox(4, lblNomeCliente, nomeCliente);
        HBox.setHgrow(campoNomeBox, Priority.ALWAYS);

        // ── DATA ENTREGA ──────────────────────────────────
        Label lblDataEntrega = new Label("Data de entrega");
        lblDataEntrega.setStyle("-fx-font-size: 11px; -fx-text-fill: #888; -fx-font-weight: bold;");

        java.time.format.DateTimeFormatter fmtData = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");

        String dataInicial = carrinhoService.getDataEntrega() != null
                ? carrinhoService.getDataEntrega().format(fmtData) : "";
        TextField campoDataEntregaTexto = new TextField(dataInicial);
        campoDataEntregaTexto.setPromptText("dd/MM/yyyy");
        campoDataEntregaTexto.setStyle("""
            -fx-background-radius: 8; -fx-border-radius: 8;
            -fx-border-color: #ffd1dc; -fx-padding: 8 12; -fx-font-size: 13px;
        """);
        campoDataEntregaTexto.setPrefWidth(140);

        Button btnCalendario = new Button("📅");
        btnCalendario.setStyle("""
            -fx-background-color: #ff4d6d; -fx-text-fill: white;
            -fx-background-radius: 8; -fx-padding: 8 10;
            -fx-font-size: 13px; -fx-cursor: hand;
        """);

        java.time.LocalDate[] dataEntregaSelecionada = { carrinhoService.getDataEntrega() };

        campoDataEntregaTexto.textProperty().addListener((obs, o, n) -> {
            try {
                dataEntregaSelecionada[0] = java.time.LocalDate.parse(n, fmtData);
                carrinhoService.setDataEntrega(dataEntregaSelecionada[0]);
                campoDataEntregaTexto.setStyle("""
                    -fx-background-radius: 8; -fx-border-radius: 8;
                    -fx-border-color: #ff4d6d; -fx-padding: 8 12; -fx-font-size: 13px;
                """);
            } catch (Exception ignored) {
                dataEntregaSelecionada[0] = null;
                carrinhoService.setDataEntrega(null);
                campoDataEntregaTexto.setStyle("""
                    -fx-background-radius: 8; -fx-border-radius: 8;
                    -fx-border-color: #ffd1dc; -fx-padding: 8 12; -fx-font-size: 13px;
                """);
            }
        });

        btnCalendario.setOnAction(e ->
                mostrarPopupCalendario(dataEntregaSelecionada, campoDataEntregaTexto, fmtData));

        HBox campoDataBox = new HBox(6, campoDataEntregaTexto, btnCalendario);
        campoDataBox.setAlignment(Pos.CENTER_LEFT);

        VBox dataEntregaBox = new VBox(4, lblDataEntrega, campoDataBox);
        dataEntregaBox.setMinWidth(190);

        HBox linhaCliente = new HBox(12, campoNomeBox, dataEntregaBox);
        linhaCliente.setAlignment(Pos.BOTTOM_LEFT);

        VBox topoComNome = new VBox(10, topoContainer, linhaCliente);
        topoComNome.setPadding(new Insets(15, 15, 10, 15));
        root.setTop(topoComNome);

        // ── CARRINHO ──────────────────────────────────────
        VBox carrinhoBox = new VBox(0);
        carrinhoBox.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 10;
            -fx-border-color: #e0e0e0;
            -fx-border-radius: 10;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);
        """);
        VBox.setMargin(carrinhoBox, new Insets(0, 15, 0, 15));

        GridPane headerGrid = criarHeaderGrid();
        headerGrid.setPadding(new Insets(6, 15, 6, 15));
        headerGrid.setStyle("-fx-border-color: transparent transparent #ddd transparent; -fx-border-width: 0 0 1 0;");

        VBox listaCarrinho = new VBox(0);

        Label totalLabel = new Label("TOTAL: R$ 0,00");
        totalLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #222;");

        atualizarCarrinho(listaCarrinho, totalLabel);

        carrinhoBox.getChildren().addAll(headerGrid, listaCarrinho);

        ScrollPane scroll = new ScrollPane(carrinhoBox);
        scroll.setFitToWidth(true);
        scroll.setFocusTraversable(false);
        scroll.setStyle("-fx-background: #ffe4ec; -fx-background-color: #ffe4ec;");
        root.setCenter(scroll);

        // ── FOOTER ────────────────────────────────────────
        Label tracejado = new Label(
                "- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -"
        );
        tracejado.setMaxWidth(Double.MAX_VALUE);
        tracejado.setStyle("-fx-text-fill: #ccc; -fx-font-size: 10px;");
        tracejado.setPadding(new Insets(6, 15, 2, 15));

        Label footerSubtotalBruto = new Label("");
        footerSubtotalBruto.setStyle("-fx-font-size: 12px; -fx-text-fill: #aaa; -fx-strikethrough: true;");
        footerSubtotalBruto.managedProperty().bind(footerSubtotalBruto.visibleProperty());
        footerSubtotalBruto.setVisible(false);

        Label footerDesconto = new Label("");
        footerDesconto.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");
        footerDesconto.managedProperty().bind(footerDesconto.visibleProperty());
        footerDesconto.setVisible(false);

        TextArea campoObservacao = new TextArea();
        campoObservacao.setPromptText("Observações do pedido...");
        campoObservacao.setWrapText(true);
        campoObservacao.setPrefRowCount(2);
        campoObservacao.setMaxHeight(60);
        campoObservacao.setStyle("""
            -fx-background-radius: 8; -fx-border-radius: 8;
            -fx-border-color: #ffd1dc; -fx-padding: 6 10;
            -fx-font-size: 11px; -fx-control-inner-background: white;
        """);
        campoObservacao.setFocusTraversable(false);
        VBox.setMargin(campoObservacao, new Insets(0, 15, 0, 15));

        Button btnFinalizar = BotaoFactory.primario("Finalizar Pedido");
        btnFinalizar.setOnAction(e -> {
            if (carrinhoService.getItens().isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Carrinho vazio").show();
                return;
            }
            if (nomeCliente.getText().isBlank()) {
                new Alert(Alert.AlertType.WARNING, "Informe o nome do cliente antes de finalizar.").show();
                return;
            }
            mostrarPopupFinalizarPedido(nomeCliente.getText().trim(), listaCarrinho, totalLabel,
                    footerSubtotalBruto, footerDesconto, nomeCliente, campoObservacao,
                    dataEntregaSelecionada, campoDataEntregaTexto);
        });

        Button btnEditarPedido = BotaoFactory.secundario("Editar Pedido");
        btnEditarPedido.setOnAction(e ->
                mostrarPopupEditarPedido(listaCarrinho, totalLabel, footerSubtotalBruto, footerDesconto));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox infoTotal = new VBox(2, footerSubtotalBruto, footerDesconto, totalLabel);

        HBox footerLinha = new HBox(10, infoTotal, spacer, btnEditarPedido, btnFinalizar);
        footerLinha.setAlignment(Pos.CENTER_LEFT);
        footerLinha.setPadding(new Insets(8, 15, 12, 15));

        Label lblCredito = new Label("Desenvolvido por Vinicius Bessega ©");
        lblCredito.setStyle("-fx-font-size: 11px; -fx-text-fill: #737171;");
        lblCredito.setPadding(new Insets(0, 15, 4, 0));

        HBox creditoBox = new HBox(lblCredito);
        creditoBox.setAlignment(Pos.BOTTOM_RIGHT);

        VBox footer = new VBox(2, campoObservacao, tracejado, footerLinha, creditoBox);
        footer.setStyle("""
            -fx-background-color: white;
            -fx-border-color: #e0e0e0 transparent transparent transparent;
            -fx-border-width: 1 0 0 0;
        """);
        root.setBottom(footer);

        return root;
    }

    // ── HEADER DA TABELA DO CARRINHO ──────────────────────
    private GridPane criarHeaderGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(0);

        ColumnConstraints cProduto = new ColumnConstraints();
        cProduto.setHgrow(Priority.ALWAYS);
        cProduto.setFillWidth(true);

        ColumnConstraints cQtd = new ColumnConstraints(80);
        cQtd.setHalignment(HPos.CENTER);

        ColumnConstraints cUnit = new ColumnConstraints(100);
        cUnit.setHalignment(HPos.CENTER);

        ColumnConstraints cSub = new ColumnConstraints(110);
        cSub.setHalignment(HPos.CENTER);

        grid.getColumnConstraints().addAll(cProduto, cQtd, cUnit, cSub);

        String estiloHeader = "-fx-font-size: 11px; -fx-text-fill: #888; -fx-font-weight: bold;";
        Label hNome = new Label("PRODUTO");  hNome.setStyle(estiloHeader);
        Label hQtd  = new Label("QTD");      hQtd.setStyle(estiloHeader);
        Label hUnit = new Label("UNIT.");     hUnit.setStyle(estiloHeader);
        Label hSub  = new Label("SUBTOTAL"); hSub.setStyle(estiloHeader);

        grid.add(hNome, 0, 0);
        grid.add(hQtd,  1, 0);
        grid.add(hUnit, 2, 0);
        grid.add(hSub,  3, 0);

        return grid;
    }

    // ── LINHA DE ITEM — usa BigDecimal para qtd ───────────
    private GridPane criarLinhaItem(String nome, BigDecimal qtd, BigDecimal precoUnit,
                                    BigDecimal subtotal, boolean isTorta) {
        GridPane grid = new GridPane();
        grid.setHgap(0);
        grid.setPadding(new Insets(7, 15, 7, 15));
        grid.setStyle("-fx-border-color: transparent transparent #f5f5f5 transparent; -fx-border-width: 0 0 1 0;");

        ColumnConstraints cProduto = new ColumnConstraints();
        cProduto.setHgrow(Priority.ALWAYS);
        cProduto.setFillWidth(true);

        ColumnConstraints cQtd = new ColumnConstraints(80);
        cQtd.setHalignment(HPos.CENTER);

        ColumnConstraints cUnit = new ColumnConstraints(100);
        cUnit.setHalignment(HPos.CENTER);

        ColumnConstraints cSub = new ColumnConstraints(110);
        cSub.setHalignment(HPos.CENTER);

        grid.getColumnConstraints().addAll(cProduto, cQtd, cUnit, cSub);

        Label lNome = new Label(nome);
        lNome.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");

        String qtdTexto = isTorta
                ? qtd.stripTrailingZeros().toPlainString() + " kg"
                : "x" + qtd.toBigIntegerExact();

        Label lQtd = new Label(qtdTexto);
        lQtd.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");

        Label lUnit = new Label("R$ " + precoUnit.setScale(2, RoundingMode.HALF_UP));
        lUnit.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");

        Label lSub = new Label("R$ " + subtotal.setScale(2, RoundingMode.HALF_UP));
        lSub.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #333;");

        grid.add(lNome, 0, 0);
        grid.add(lQtd,  1, 0);
        grid.add(lUnit, 2, 0);
        grid.add(lSub,  3, 0);

        return grid;
    }

    // ── ATUALIZAR CARRINHO ────────────────────────────────
    private void atualizarCarrinho(VBox listaCarrinho, Label totalLabel) {
        listaCarrinho.getChildren().clear();

        if (carrinhoService.getItens().isEmpty()) {
            Label vazio = new Label("Nenhum item adicionado.");
            vazio.setStyle("-fx-text-fill: #aaa; -fx-font-size: 12px;");
            vazio.setPadding(new Insets(14, 15, 14, 15));
            listaCarrinho.getChildren().add(vazio);
            totalLabel.setText("TOTAL: R$ 0,00");
            return;
        }

        BigDecimal total = BigDecimal.ZERO;

        for (var entry : carrinhoService.getItens().entrySet()) {
            Produto    p = carrinhoService.getProduto(entry.getKey());
            BigDecimal q = entry.getValue();

            if (p == null || q == null || q.compareTo(BigDecimal.ZERO) <= 0) continue;

            BigDecimal precoUnit = BigDecimal.ZERO;
            if (p instanceof ProdutoSimples ps) precoUnit = ps.getPreco();
            else if (p instanceof Torta t)      precoUnit = t.getPrecoPorKg();

            boolean isTorta = p instanceof Torta;

            BigDecimal subtotal = precoUnit.multiply(q);
            total = total.add(subtotal);

            listaCarrinho.getChildren().add(
                    criarLinhaItem(p.getNome(), q, precoUnit, subtotal, isTorta)
            );
        }

        totalLabel.setText("TOTAL: R$ " + total.setScale(2, RoundingMode.HALF_UP));
    }

    // ── POPUP EDITAR PEDIDO ───────────────────────────────
    private void mostrarPopupEditarPedido(VBox listaCarrinho, Label totalLabel,
                                          Label footerSubtotalBruto, Label footerDesconto) {

        BorderPane popup = new BorderPane();
        popup.setMaxWidth(540);
        popup.setMaxHeight(500);
        popup.setPrefHeight(Region.USE_COMPUTED_SIZE);
        popup.setStyle("""
        -fx-background-color: white;
        -fx-background-radius: 15;
        -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 20, 0, 0, 6);
    """);
        popup.setFocusTraversable(false);

        Map<Long, SimpleObjectProperty<BigDecimal>> qtdMap   = new java.util.HashMap<>();
        Map<Long, SimpleObjectProperty<BigDecimal>> precoMap = new java.util.HashMap<>();

        for (var entry : carrinhoService.getItens().entrySet()) {
            Long id = entry.getKey();
            Produto p = carrinhoService.getProduto(id);

            BigDecimal precoUnit = BigDecimal.ZERO;
            if (p instanceof ProdutoSimples ps) precoUnit = ps.getPreco();
            else if (p instanceof Torta t)      precoUnit = t.getPrecoPorKg();

            qtdMap.put(id,   new SimpleObjectProperty<>(entry.getValue()));
            precoMap.put(id, new SimpleObjectProperty<>(precoUnit));
        }

        // ── Labels rodapé ─────────────────────────────────────
        Label lblSubtotalBruto = new Label("");
        lblSubtotalBruto.setStyle("-fx-font-size: 12px; -fx-text-fill: #aaa; -fx-strikethrough: true;");
        lblSubtotalBruto.managedProperty().bind(lblSubtotalBruto.visibleProperty());
        lblSubtotalBruto.setVisible(false);

        Label lblDescontoValor = new Label("");
        lblDescontoValor.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");
        lblDescontoValor.managedProperty().bind(lblDescontoValor.visibleProperty());
        lblDescontoValor.setVisible(false);

        Label totalPopupLabel = new Label();
        totalPopupLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px;");

        // ── Campos de desconto ─────────────────────────────────
        Label lblDesconto = new Label("Desconto");
        lblDesconto.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");

        Label lblRS  = new Label("R$");
        lblRS.setStyle("-fx-font-size: 11px; -fx-text-fill: #aaa;");
        Label lblPct = new Label("%");
        lblPct.setStyle("-fx-font-size: 11px; -fx-text-fill: #aaa;");

        TextField campoDescontoRS = new TextField("0.00");
        campoDescontoRS.setEditable(false);
        campoDescontoRS.setPrefWidth(95);
        campoDescontoRS.setStyle("""
        -fx-background-radius: 8; -fx-border-radius: 8;
        -fx-border-color: #ffd1dc; -fx-padding: 8 12; -fx-font-size: 13px;
    """);

        TextField campoDescontoPct = new TextField("0.00");
        campoDescontoPct.setEditable(false);
        campoDescontoPct.setPrefWidth(75);
        campoDescontoPct.setStyle(campoDescontoRS.getStyle());

        HBox camposDesconto = new HBox(8, lblRS, campoDescontoRS, lblPct, campoDescontoPct);
        camposDesconto.setAlignment(Pos.CENTER_LEFT);

        boolean[] atualizandoDesconto = { false };

        // ── Recalcular ─────────────────────────────────────────
        Runnable recalcular = () -> {
            BigDecimal subtotalGeral = BigDecimal.ZERO;
            for (Long id : qtdMap.keySet())
                subtotalGeral = subtotalGeral.add(
                        precoMap.get(id).get().multiply(qtdMap.get(id).get()));

            BigDecimal valorDesc = BigDecimal.ZERO;
            try { valorDesc = new BigDecimal(campoDescontoRS.getText().replace(",", ".")); }
            catch (Exception ignored) {}

            BigDecimal totalFinal = subtotalGeral.subtract(valorDesc)
                    .max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);

            if (valorDesc.compareTo(BigDecimal.ZERO) > 0) {
                lblSubtotalBruto.setText("R$ " + subtotalGeral.setScale(2, RoundingMode.HALF_UP));
                lblSubtotalBruto.setVisible(true);
                lblDescontoValor.setText("Desconto: R$ " + valorDesc.setScale(2, RoundingMode.HALF_UP));
                lblDescontoValor.setVisible(true);
                footerSubtotalBruto.setText("R$ " + subtotalGeral.setScale(2, RoundingMode.HALF_UP));
                footerSubtotalBruto.setVisible(true);
                footerDesconto.setText("Desconto: R$ " + valorDesc.setScale(2, RoundingMode.HALF_UP));
                footerDesconto.setVisible(true);
            } else {
                lblSubtotalBruto.setVisible(false);
                lblDescontoValor.setVisible(false);
                footerSubtotalBruto.setVisible(false);
                footerDesconto.setVisible(false);
            }
            totalPopupLabel.setText("TOTAL: R$ " + totalFinal);
            totalLabel.setText("TOTAL: R$ " + totalFinal);
        };

        // Listener R$ → sincroniza %
        campoDescontoRS.textProperty().addListener((obs, o, n) -> {
            if (atualizandoDesconto[0]) return;
            atualizandoDesconto[0] = true;
            try {
                BigDecimal sub = BigDecimal.ZERO;
                for (Long id : qtdMap.keySet())
                    sub = sub.add(precoMap.get(id).get().multiply(qtdMap.get(id).get()));
                BigDecimal rs  = new BigDecimal(n.replace(",", "."));
                if (sub.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal pct = rs.multiply(new BigDecimal("100"))
                            .divide(sub, 2, RoundingMode.HALF_UP);
                    campoDescontoPct.setText(pct.toPlainString());
                }
            } catch (Exception ignored) {}
            atualizandoDesconto[0] = false;
            recalcular.run();
        });

        // Listener % → sincroniza R$
        campoDescontoPct.textProperty().addListener((obs, o, n) -> {
            if (atualizandoDesconto[0]) return;
            atualizandoDesconto[0] = true;
            try {
                BigDecimal sub = BigDecimal.ZERO;
                for (Long id : qtdMap.keySet())
                    sub = sub.add(precoMap.get(id).get().multiply(qtdMap.get(id).get()));
                BigDecimal pct = new BigDecimal(n.replace(",", "."));
                BigDecimal rs  = sub.multiply(pct)
                        .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                campoDescontoRS.setText(rs.toPlainString());
            } catch (Exception ignored) {}
            atualizandoDesconto[0] = false;
            recalcular.run();
        });

        Button btnEditar = BotaoFactory.secundario("Editar Valores");
        Button btnOk     = BotaoFactory.primario("OK");
        boolean[] modoEdicao = { false };

        VBox listaPopup = new VBox(0);
        listaPopup.setFocusTraversable(false);

        Runnable[] construirLinhasRef = new Runnable[1];
        construirLinhasRef[0] = () -> {
            listaPopup.getChildren().clear();

            for (Long id : new java.util.ArrayList<>(qtdMap.keySet())) {
                Produto p         = carrinhoService.getProduto(id);
                var     qtdProp   = qtdMap.get(id);
                var     precoProp = precoMap.get(id);
                boolean isTorta   = p instanceof Torta;

                GridPane grid = new GridPane();
                grid.setPadding(new Insets(7, 12, 7, 12));
                grid.setHgap(8);
                grid.setStyle("""
                -fx-border-color: transparent transparent #f5f5f5 transparent;
                -fx-border-width: 0 0 1 0;
            """);
                grid.setFocusTraversable(false);

                ColumnConstraints cNome = new ColumnConstraints();
                cNome.setHgrow(Priority.ALWAYS); cNome.setFillWidth(true); cNome.setMinWidth(100);
                ColumnConstraints cControle = new ColumnConstraints(140);
                cControle.setHalignment(javafx.geometry.HPos.CENTER);
                ColumnConstraints cPreco = new ColumnConstraints(80);
                cPreco.setHalignment(javafx.geometry.HPos.CENTER);
                ColumnConstraints cSub = new ColumnConstraints(85);
                cSub.setHalignment(javafx.geometry.HPos.CENTER);
                grid.getColumnConstraints().addAll(cNome, cControle, cPreco, cSub);

                Label lblNome = new Label(p.getNome());
                lblNome.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");
                lblNome.setWrapText(true);
                lblNome.setMinHeight(Region.USE_PREF_SIZE);
                GridPane.setValignment(lblNome, javafx.geometry.VPos.CENTER);

                Button btnMenos = new Button("-");
                btnMenos.setStyle("""
                -fx-background-color: #6c757d; -fx-text-fill: white;
                -fx-background-radius: 15; -fx-padding: 4 12;
                -fx-font-weight: bold; -fx-cursor: hand;
            """);
                Button btnMais = new Button("+");
                btnMais.setStyle("""
                -fx-background-color: #ff4d6d; -fx-text-fill: white;
                -fx-background-radius: 15; -fx-padding: 4 12;
                -fx-font-weight: bold; -fx-cursor: hand;
            """);

                TextField lblQtd = new TextField(qtdProp.get().stripTrailingZeros().toPlainString());
                lblQtd.setPrefWidth(isTorta ? 55 : 45);
                lblQtd.setMaxWidth(isTorta ? 55 : 45);
                lblQtd.setStyle("""
                -fx-font-size: 12px; -fx-font-weight: bold; -fx-alignment: center;
                -fx-background-radius: 6; -fx-border-radius: 6;
                -fx-border-color: #ffd1dc; -fx-padding: 2 4;
            """);
                lblQtd.textProperty().addListener((obs, o, n) -> {
                    String regex = isTorta ? "\\d{0,3}([.,]\\d?)?" : "\\d{0,3}";
                    if (!n.matches(regex)) { lblQtd.setText(o); return; }
                    try {
                        BigDecimal val = new BigDecimal(n.replace(",", "."));
                        qtdProp.set(val);
                        carrinhoService.getItens().put(id, val);
                        recalcular.run();
                        atualizarCarrinho(listaCarrinho, totalLabel);
                    } catch (Exception ignored) {}
                });

                Label lblSufixo = new Label(isTorta ? "kg" : "");
                lblSufixo.setStyle("-fx-font-size: 11px; -fx-text-fill: #aaa;");

                HBox controle = new HBox(6, btnMenos, lblQtd, lblSufixo, btnMais);
                controle.setAlignment(Pos.CENTER);

                TextField campoPreco = new TextField(
                        precoProp.get().setScale(2, RoundingMode.HALF_UP).toString());
                campoPreco.setEditable(false);
                campoPreco.setPrefWidth(72); campoPreco.setMinWidth(72); campoPreco.setMaxWidth(72);
                campoPreco.setStyle("""
                -fx-background-radius: 6; -fx-border-radius: 6;
                -fx-border-color: transparent; -fx-padding: 4 6; -fx-font-size: 12px;
            """);

                BigDecimal subInicial = precoProp.get().multiply(qtdProp.get())
                        .setScale(2, RoundingMode.HALF_UP);
                Label lblSub = new Label("R$ " + subInicial);
                lblSub.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #333;");

                campoPreco.textProperty().addListener((obs, o, n) -> {
                    try {
                        BigDecimal novoPreco = new BigDecimal(n.replace(",", "."));
                        precoProp.set(novoPreco);
                        BigDecimal sub = novoPreco.multiply(qtdProp.get()).setScale(2, RoundingMode.HALF_UP);
                        lblSub.setText("R$ " + sub);
                        recalcular.run();
                    } catch (Exception ignored) {}
                });

                BigDecimal passo = isTorta ? new BigDecimal("0.5") : BigDecimal.ONE;

                btnMais.setOnAction(ev -> {
                    BigDecimal novaQtd = qtdProp.get().add(passo);
                    qtdProp.set(novaQtd);
                    carrinhoService.getItens().put(id, novaQtd);
                    lblQtd.setText(novaQtd.stripTrailingZeros().toPlainString());
                    lblSub.setText("R$ " + precoProp.get().multiply(novaQtd).setScale(2, RoundingMode.HALF_UP));
                    recalcular.run();
                    atualizarCarrinho(listaCarrinho, totalLabel);
                });

                btnMenos.setOnAction(ev -> {
                    BigDecimal atual = qtdProp.get();
                    if (atual.compareTo(passo) <= 0) {
                        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                        confirm.setHeaderText("Remover \"" + p.getNome() + "\"?");
                        confirm.setContentText("Deseja retirar este produto do pedido?");
                        confirm.showAndWait().ifPresent(resp -> {
                            if (resp == ButtonType.OK) {
                                carrinhoService.getItens().remove(id);
                                carrinhoService.getProdutos().remove(id);
                                qtdMap.remove(id);
                                precoMap.remove(id);
                                construirLinhasRef[0].run();
                                recalcular.run();
                                atualizarCarrinho(listaCarrinho, totalLabel);
                            }
                        });
                    } else {
                        BigDecimal novaQtd = atual.subtract(passo);
                        qtdProp.set(novaQtd);
                        carrinhoService.getItens().put(id, novaQtd);
                        lblQtd.setText(novaQtd.stripTrailingZeros().toPlainString());
                        lblSub.setText("R$ " + precoProp.get().multiply(novaQtd).setScale(2, RoundingMode.HALF_UP));
                        recalcular.run();
                        atualizarCarrinho(listaCarrinho, totalLabel);
                    }
                });

                grid.setUserData(campoPreco);
                GridPane.setValignment(controle,   javafx.geometry.VPos.CENTER);
                GridPane.setValignment(campoPreco, javafx.geometry.VPos.CENTER);
                GridPane.setValignment(lblSub,     javafx.geometry.VPos.CENTER);

                grid.add(lblNome,    0, 0);
                grid.add(controle,   1, 0);
                grid.add(campoPreco, 2, 0);
                grid.add(lblSub,     3, 0);

                listaPopup.getChildren().add(grid);
            }

            recalcular.run();
        };

        construirLinhasRef[0].run();

        ScrollPane scrollPopup = new ScrollPane(listaPopup);
        scrollPopup.setFitToWidth(true);
        scrollPopup.setFocusTraversable(false);
        scrollPopup.setPrefHeight(280);
        scrollPopup.setStyle("""
        -fx-background: white; -fx-background-color: white;
        -fx-focus-color: transparent; -fx-faint-focus-color: transparent;
    """);
        popup.setCenter(scrollPopup);

        btnEditar.setOnAction(ev -> {
            modoEdicao[0] = !modoEdicao[0];

            String estiloAtivo = """
            -fx-background-radius: 8; -fx-border-radius: 8;
            -fx-border-color: #ff4d6d; -fx-padding: 8 12; -fx-font-size: 13px;
        """;
            String estiloInativo = """
            -fx-background-radius: 8; -fx-border-radius: 8;
            -fx-border-color: #ffd1dc; -fx-padding: 8 12; -fx-font-size: 13px;
        """;

            campoDescontoRS.setEditable(modoEdicao[0]);
            campoDescontoRS.setStyle(modoEdicao[0] ? estiloAtivo : estiloInativo);
            campoDescontoPct.setEditable(modoEdicao[0]);
            campoDescontoPct.setStyle(modoEdicao[0] ? estiloAtivo : estiloInativo);

            for (var node : listaPopup.getChildren()) {
                if (node instanceof GridPane gp && gp.getUserData() instanceof TextField tf) {
                    tf.setEditable(modoEdicao[0]);
                    tf.setStyle(modoEdicao[0] ? """
                    -fx-background-radius: 6; -fx-border-radius: 6;
                    -fx-border-color: #ff4d6d; -fx-padding: 4 6; -fx-font-size: 12px;
                """ : """
                    -fx-background-radius: 6; -fx-border-radius: 6;
                    -fx-border-color: transparent; -fx-padding: 4 6; -fx-font-size: 12px;
                """);
                }
            }
            btnEditar.setText(modoEdicao[0] ? "Concluir Edição" : "Editar Valores");
        });

        HBox botoes = new HBox(10, btnEditar, btnOk);
        botoes.setAlignment(Pos.CENTER_RIGHT);

        VBox rodape = new VBox(6,
                new VBox(4, lblDesconto, camposDesconto),
                lblSubtotalBruto,
                lblDescontoValor,
                totalPopupLabel,
                botoes
        );
        rodape.setPadding(new Insets(12, 12, 14, 12));
        rodape.setStyle("""
        -fx-background-color: white;
        -fx-border-color: #f0f0f0 transparent transparent transparent;
        -fx-border-width: 1 0 0 0; -fx-background-radius: 0 0 15 15;
    """);
        popup.setBottom(rodape);

        StackPane overlay = new StackPane(popup);
        overlay.setStyle("""
        -fx-background-color: rgba(0,0,0,0.4);
        -fx-focus-color: transparent; -fx-faint-focus-color: transparent;
    """);
        overlay.setAlignment(Pos.CENTER);
        overlay.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
        overlay.setFocusTraversable(false);

        rootPrincipal.getChildren().add(overlay);
        popup.setScaleX(0); popup.setScaleY(0);
        ScaleTransition anim = new ScaleTransition(Duration.millis(200), popup);
        anim.setToX(1); anim.setToY(1); anim.play();

        btnOk.setOnAction(ev -> rootPrincipal.getChildren().remove(overlay));
    }

    // ── POPUP FINALIZAR PEDIDO ────────────────────────────
    private void mostrarPopupFinalizarPedido(
            String nomeClienteTexto, VBox listaCarrinho, Label totalLabel,
            Label footerSubtotalBruto, Label footerDesconto,
            TextField campoNomeCliente, TextArea campoObservacao,
            java.time.LocalDate[] dataEntregaSelecionada, TextField campoDataEntregaTexto) {

        BorderPane popup = new BorderPane();
        popup.setMaxWidth(520);
        popup.setMaxHeight(520);
        popup.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 15;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 20, 0, 0, 6);
        """);
        popup.setFocusTraversable(false);

        Label lblCliente = new Label(nomeClienteTexto.toUpperCase());
        lblCliente.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #333;");

        Label lblData = new Label(
                java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        lblData.setStyle("-fx-font-size: 11px; -fx-text-fill: #aaa;");

        Label lblDataEntregaPopup = new Label(
                dataEntregaSelecionada[0] != null
                        ? "Entrega: " + dataEntregaSelecionada[0]
                                        .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        : "");
        lblDataEntregaPopup.setStyle("-fx-font-size: 11px; -fx-text-fill: #ff4d6d; -fx-font-weight: bold;");
        lblDataEntregaPopup.managedProperty().bind(lblDataEntregaPopup.visibleProperty());
        lblDataEntregaPopup.setVisible(dataEntregaSelecionada[0] != null);

        Label tracejadoTopo = new Label(
                "- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
        tracejadoTopo.setStyle("-fx-text-fill: #ccc; -fx-font-size: 10px;");

        // Header colunas
        GridPane headerGrid = new GridPane();
        headerGrid.setHgap(8);
        headerGrid.setPadding(new Insets(4, 0, 4, 0));

        ColumnConstraints hcNome = new ColumnConstraints(); hcNome.setHgrow(Priority.ALWAYS); hcNome.setFillWidth(true);
        ColumnConstraints hcQtd  = new ColumnConstraints(60); hcQtd.setHalignment(HPos.CENTER);
        ColumnConstraints hcUnit = new ColumnConstraints(85); hcUnit.setHalignment(HPos.CENTER);
        ColumnConstraints hcSub  = new ColumnConstraints(90); hcSub.setHalignment(HPos.CENTER);
        headerGrid.getColumnConstraints().addAll(hcNome, hcQtd, hcUnit, hcSub);

        String estiloH = "-fx-font-size: 10px; -fx-text-fill: #888; -fx-font-weight: bold;";
        Label hN = new Label("PRODUTO"); hN.setStyle(estiloH);
        Label hQ = new Label("QTD");     hQ.setStyle(estiloH);
        Label hU = new Label("UNIT.");   hU.setStyle(estiloH);
        Label hS = new Label("TOTAL");   hS.setStyle(estiloH);
        headerGrid.add(hN, 0, 0); headerGrid.add(hQ, 1, 0);
        headerGrid.add(hU, 2, 0); headerGrid.add(hS, 3, 0);

        VBox topo = new VBox(6, lblCliente, lblData, lblDataEntregaPopup, tracejadoTopo, headerGrid);
        topo.setPadding(new Insets(16, 16, 6, 16));
        topo.setStyle("""
            -fx-background-color: white;
            -fx-border-color: transparent transparent #f0f0f0 transparent;
            -fx-border-width: 0 0 1 0; -fx-background-radius: 15 15 0 0;
        """);
        popup.setTop(topo);

        // Lista de itens
        VBox listaRelatorio = new VBox(0);
        listaRelatorio.setFocusTraversable(false);
        BigDecimal subtotalGeral = BigDecimal.ZERO;

        for (var entry : carrinhoService.getItens().entrySet()) {
            Produto   p = carrinhoService.getProduto(entry.getKey());
            BigDecimal q = entry.getValue();

            BigDecimal precoUnit = BigDecimal.ZERO;
            if (p instanceof ProdutoSimples ps) precoUnit = ps.getPreco();
            else if (p instanceof Torta t)      precoUnit = t.getPrecoPorKg();

            BigDecimal sub = precoUnit.multiply(q).setScale(2, RoundingMode.HALF_UP);
            subtotalGeral = subtotalGeral.add(sub);

            boolean isTorta = p instanceof Torta;
            String qtdTexto = q.stripTrailingZeros().scale() > 0
                    ? q.stripTrailingZeros().toPlainString() + " kg"
                    : "x" + q.toBigIntegerExact();

            GridPane linha = new GridPane();
            linha.setHgap(8);
            linha.setPadding(new Insets(7, 16, 7, 16));
            linha.setStyle("-fx-border-color: transparent transparent #f8f8f8 transparent; -fx-border-width: 0 0 1 0;");
            linha.setFocusTraversable(false);

            ColumnConstraints lcNome = new ColumnConstraints(); lcNome.setHgrow(Priority.ALWAYS); lcNome.setFillWidth(true);
            ColumnConstraints lcQtd  = new ColumnConstraints(60); lcQtd.setHalignment(HPos.CENTER);
            ColumnConstraints lcUnit = new ColumnConstraints(85); lcUnit.setHalignment(HPos.CENTER);
            ColumnConstraints lcSub  = new ColumnConstraints(90); lcSub.setHalignment(HPos.CENTER);
            linha.getColumnConstraints().addAll(lcNome, lcQtd, lcUnit, lcSub);

            Label lNome = new Label(p.getNome()); lNome.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;"); lNome.setWrapText(true);
            Label lQtd  = new Label(qtdTexto);    lQtd.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");
            Label lUnit = new Label("R$ " + precoUnit.setScale(2, RoundingMode.HALF_UP)); lUnit.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");
            Label lSub  = new Label("R$ " + sub); lSub.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #333;");

            GridPane.setValignment(lNome, VPos.CENTER); GridPane.setValignment(lQtd, VPos.CENTER);
            GridPane.setValignment(lUnit, VPos.CENTER); GridPane.setValignment(lSub, VPos.CENTER);

            linha.add(lNome, 0, 0); linha.add(lQtd, 1, 0);
            linha.add(lUnit, 2, 0); linha.add(lSub, 3, 0);

            listaRelatorio.getChildren().add(linha);
        }

        ScrollPane scrollPopup = new ScrollPane(listaRelatorio);
        scrollPopup.setFitToWidth(true);
        scrollPopup.setFocusTraversable(false);
        scrollPopup.setPrefHeight(230);
        scrollPopup.setStyle("""
            -fx-background: white; -fx-background-color: white;
            -fx-focus-color: transparent; -fx-faint-focus-color: transparent;
        """);
        popup.setCenter(scrollPopup);

        // Rodapé
        String totalFooter = totalLabel.getText().replace("TOTAL: R$ ", "").trim();
        BigDecimal totalFinalCalculado;
        try { totalFinalCalculado = new BigDecimal(totalFooter.replace(",", ".")); }
        catch (Exception ex) { totalFinalCalculado = subtotalGeral; }
        final BigDecimal totalFinal = totalFinalCalculado;
        final BigDecimal subtotalFinal = subtotalGeral;

        BigDecimal valorDesc = subtotalGeral.subtract(totalFinal).setScale(2, RoundingMode.HALF_UP);

        Label tracejadoRodape = new Label(
                "- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
        tracejadoRodape.setStyle("-fx-text-fill: #ccc; -fx-font-size: 10px;");

        Label lblSubBruto = new Label("R$ " + subtotalGeral.setScale(2, RoundingMode.HALF_UP));
        lblSubBruto.setStyle("-fx-font-size: 12px; -fx-text-fill: #aaa; -fx-strikethrough: true;");
        lblSubBruto.managedProperty().bind(lblSubBruto.visibleProperty());
        lblSubBruto.setVisible(valorDesc.compareTo(BigDecimal.ZERO) > 0);

        Label lblDescValor = new Label("Desconto: R$ " + valorDesc.setScale(2, RoundingMode.HALF_UP));
        lblDescValor.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");
        lblDescValor.managedProperty().bind(lblDescValor.visibleProperty());
        lblDescValor.setVisible(valorDesc.compareTo(BigDecimal.ZERO) > 0);

        Label lblTotal = new Label("TOTAL: R$ " + totalFinal.setScale(2, RoundingMode.HALF_UP));
        lblTotal.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #222;");

        Button btnCancelar  = BotaoFactory.secundario("Cancelar");
        Button btnConfirmar = BotaoFactory.primario("Confirmar Pedido");

        Label lblObsTexto = new Label(campoObservacao.getText().trim());
        lblObsTexto.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
        lblObsTexto.setWrapText(true);
        lblObsTexto.managedProperty().bind(lblObsTexto.visibleProperty());
        lblObsTexto.setVisible(!campoObservacao.getText().isBlank());

        Label lblObsTitulo = new Label("Obs:");
        lblObsTitulo.setStyle("-fx-font-size: 11px; -fx-text-fill: #aaa; -fx-font-weight: bold;");
        lblObsTitulo.managedProperty().bind(lblObsTitulo.visibleProperty());
        lblObsTitulo.setVisible(!campoObservacao.getText().isBlank());

        VBox blocoObs = new VBox(2, lblObsTitulo, lblObsTexto);
        blocoObs.managedProperty().bind(blocoObs.visibleProperty());
        blocoObs.setVisible(!campoObservacao.getText().isBlank());

        HBox botoes = new HBox(10, btnCancelar, btnConfirmar);
        botoes.setAlignment(Pos.CENTER_RIGHT);

        VBox rodape = new VBox(6, blocoObs, tracejadoRodape, lblSubBruto, lblDescValor, lblTotal, botoes);
        rodape.setPadding(new Insets(10, 16, 14, 16));
        rodape.setStyle("""
            -fx-background-color: white;
            -fx-border-color: #f0f0f0 transparent transparent transparent;
            -fx-border-width: 1 0 0 0; -fx-background-radius: 0 0 15 15;
        """);
        popup.setBottom(rodape);

        StackPane overlay = new StackPane(popup);
        overlay.setStyle("""
            -fx-background-color: rgba(0,0,0,0.4);
            -fx-focus-color: transparent; -fx-faint-focus-color: transparent;
        """);
        overlay.setAlignment(Pos.CENTER);
        overlay.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
        overlay.setFocusTraversable(false);

        rootPrincipal.getChildren().add(overlay);
        popup.setScaleX(0); popup.setScaleY(0);
        ScaleTransition anim = new ScaleTransition(Duration.millis(200), popup);
        anim.setToX(1); anim.setToY(1); anim.play();

        btnCancelar.setOnAction(ev -> {
            Alert confirm = new Alert(Alert.AlertType.NONE);
            confirm.setTitle("Cancelar pedido?");
            confirm.setHeaderText("O que deseja fazer?");

            ButtonType btnContinuar      = new ButtonType("Continuar Pedido");
            ButtonType btnCancelarPedido = new ButtonType("Cancelar Pedido", ButtonBar.ButtonData.CANCEL_CLOSE);
            confirm.getButtonTypes().setAll(btnContinuar, btnCancelarPedido);

            confirm.showAndWait().ifPresent(resp -> {
                if (resp == btnContinuar) {
                    rootPrincipal.getChildren().remove(overlay);
                }
                if (resp == btnCancelarPedido) {
                    rootPrincipal.getChildren().remove(overlay);
                    carrinhoService.limpar();
                    footerSubtotalBruto.setVisible(false);
                    footerDesconto.setVisible(false);
                    totalLabel.setText("TOTAL: R$ 0,00");
                    campoNomeCliente.clear();
                    campoObservacao.clear();
                    atualizarCarrinho(listaCarrinho, totalLabel);
                }
            });
        });

        btnConfirmar.setOnAction(ev -> {
            Pedido pedido = new Pedido();
            pedido.setNomeCliente(nomeClienteTexto);
            pedido.setData(java.time.LocalDateTime.now());
            pedido.setDesconto(valorDesc);
            pedido.setObservacao(campoObservacao.getText().trim());
            pedido.setDataEntrega(dataEntregaSelecionada[0]);

            List<ItemPedido> itens = new ArrayList<>();

            for (var entry : carrinhoService.getItens().entrySet()) {
                Produto   p = carrinhoService.getProduto(entry.getKey());
                BigDecimal q = entry.getValue();

                BigDecimal precoUnit = BigDecimal.ZERO;
                if (p instanceof ProdutoSimples ps) precoUnit = ps.getPreco();
                else if (p instanceof Torta t)      precoUnit = t.getPrecoPorKg();

                ItemPedido item = new ItemPedido();
                item.setProduto(p);
                item.setQuantidade(q);           // BigDecimal direto — funciona para 2.5 kg
                item.setPrecoUnitario(precoUnit);
                item.setPedido(pedido);
                itens.add(item);
            }

            pedido.setItens(itens);

            try {
                pedido.setTotal(totalFinal);
                pedidoService.salvarSemRecalcular(pedido);

                Pedido pedidoSalvo = pedidoService.buscarComItens(pedido.getId());

                Alert sucesso = new Alert(Alert.AlertType.INFORMATION);
                sucesso.setTitle("Pedido confirmado");
                sucesso.setHeaderText(null);
                sucesso.setContentText("✅ Pedido de " + nomeClienteTexto + " finalizado com sucesso!");
                sucesso.showAndWait();

                rootPrincipal.getChildren().remove(overlay);
                carrinhoService.limpar();
                footerSubtotalBruto.setVisible(false);
                footerDesconto.setVisible(false);
                totalLabel.setText("TOTAL: R$ 0,00");
                campoNomeCliente.clear();
                campoObservacao.clear();
                campoDataEntregaTexto.clear();
                dataEntregaSelecionada[0] = null;
                atualizarCarrinho(listaCarrinho, totalLabel);

                mostrarPopupSalvarPdf(pedidoSalvo);

            } catch (Exception ex) {
                Alert erro = new Alert(Alert.AlertType.ERROR);
                erro.setTitle("Erro ao salvar");
                erro.setHeaderText("Não foi possível finalizar o pedido.");
                erro.setContentText(ex.getMessage());
                erro.showAndWait();
            }
        });
    }

    // ── POPUP SALVAR PDF ──────────────────────────────────
    private void mostrarPopupSalvarPdf(Pedido pedido) {
        VBox popup = new VBox(12);
        popup.setPadding(new Insets(24));
        popup.setMaxWidth(380);
        popup.setMaxHeight(Region.USE_PREF_SIZE);
        popup.setPrefHeight(Region.USE_COMPUTED_SIZE);
        popup.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 15;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 20, 0, 0, 6);
        """);

        Label titulo    = new Label("Salvar PDF");
        titulo.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
        Label subtitulo = new Label("Deseja salvar algum documento?");
        subtitulo.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");

        Button btnPedido  = BotaoFactory.primario("Pedido (A4)");
        Button btnComanda = BotaoFactory.primario("Comanda");
        Button btnAmbos   = BotaoFactory.secundario("Pedido e Comanda");
        Button btnNenhum  = BotaoFactory.secundario("Nenhum");

        for (Button b : List.of(btnPedido, btnComanda, btnAmbos, btnNenhum))
            b.setMaxWidth(Double.MAX_VALUE);

        popup.getChildren().addAll(titulo, subtitulo, btnPedido, btnComanda, btnAmbos, btnNenhum);

        StackPane overlay = new StackPane(popup);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.4); -fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
        overlay.setAlignment(Pos.CENTER);
        overlay.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
        overlay.setFocusTraversable(false);

        rootPrincipal.getChildren().add(overlay);
        popup.setScaleX(0); popup.setScaleY(0);
        ScaleTransition anim = new ScaleTransition(Duration.millis(200), popup);
        anim.setToX(1); anim.setToY(1); anim.play();

        String nomeBase = pedido.getNomeCliente().toUpperCase();

        btnPedido.setOnAction(ev  -> { rootPrincipal.getChildren().remove(overlay); salvarArquivo(pedido, nomeBase + " - PEDIDO",  false); });
        btnComanda.setOnAction(ev -> { rootPrincipal.getChildren().remove(overlay); salvarArquivo(pedido, nomeBase + " - COMANDA", true);  });
        btnAmbos.setOnAction(ev   -> {
            rootPrincipal.getChildren().remove(overlay);
            salvarArquivo(pedido, nomeBase + " - PEDIDO",  false);
            salvarArquivo(pedido, nomeBase + " - COMANDA", true);
        });
        btnNenhum.setOnAction(ev -> rootPrincipal.getChildren().remove(overlay));
    }

    private void salvarArquivo(Pedido pedido, String nomeArquivo, boolean comanda) {
        java.util.prefs.Preferences prefs =
                java.util.prefs.Preferences.userNodeForPackage(PedidoPdfService.class);

        javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
        chooser.setTitle("Salvar " + (comanda ? "Comanda" : "Pedido"));
        chooser.setInitialFileName(nomeArquivo + ".pdf");
        chooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("PDF", "*.pdf"));

        String ultimoDir = prefs.get("ultimo_diretorio_pdf", null);
        if (ultimoDir != null) {
            java.io.File dir = new java.io.File(ultimoDir);
            if (dir.exists() && dir.isDirectory()) chooser.setInitialDirectory(dir);
        }

        java.io.File destino = chooser.showSaveDialog(rootPrincipal.getScene().getWindow());
        if (destino != null) {
            prefs.put("ultimo_diretorio_pdf", destino.getParent());
            try {
                if (comanda) pedidoPdfService.gerarComanda(pedido, destino);
                else         pedidoPdfService.gerarPedidoA4(pedido, destino);

                Alert ok = new Alert(Alert.AlertType.INFORMATION);
                ok.setTitle("Salvo");
                ok.setHeaderText(null);
                ok.setContentText("✅ Arquivo salvo em:\n" + destino.getAbsolutePath());
                ok.showAndWait();
            } catch (Exception ex) {
                Alert erro = new Alert(Alert.AlertType.ERROR);
                erro.setTitle("Erro ao salvar PDF");
                erro.setHeaderText("Não foi possível gerar o arquivo.");
                erro.setContentText(ex.getMessage());
                erro.showAndWait();
            }
        }
    }

    // ── POPUP CALENDÁRIO ─────────────────────────────────
    private void mostrarPopupCalendario(
            java.time.LocalDate[] dataEntregaSelecionada,
            TextField campoDataEntregaTexto,
            java.time.format.DateTimeFormatter fmtData) {

        VBox popup = new VBox(10);
        popup.setPadding(new Insets(16));
        popup.setMaxWidth(300);
        popup.setMaxHeight(Region.USE_PREF_SIZE);
        popup.setPrefHeight(Region.USE_COMPUTED_SIZE);
        popup.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 15;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 20, 0, 0, 6);
        """);

        java.time.LocalDate[] mesAtual = {
                dataEntregaSelecionada[0] != null
                        ? dataEntregaSelecionada[0].withDayOfMonth(1)
                        : java.time.LocalDate.now().withDayOfMonth(1)
        };

        Label lblMesAno = new Label();
        lblMesAno.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

        Button btnAnterior = new Button("‹");
        btnAnterior.setStyle("""
            -fx-background-color: #ffccd5; -fx-background-radius: 20;
            -fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 2 10;
        """);
        Button btnProximo = new Button("›");
        btnProximo.setStyle(btnAnterior.getStyle());

        HBox navMes = new HBox(10, btnAnterior, lblMesAno, btnProximo);
        navMes.setAlignment(Pos.CENTER);

        GridPane gridDias = new GridPane();
        gridDias.setHgap(4); gridDias.setVgap(4);
        gridDias.setAlignment(Pos.CENTER);

        String[] diasSemana = {"Dom","Seg","Ter","Qua","Qui","Sex","Sáb"};
        for (int i = 0; i < 7; i++) {
            Label lblDia = new Label(diasSemana[i]);
            lblDia.setStyle("-fx-font-size: 10px; -fx-text-fill: #aaa; -fx-font-weight: bold;");
            lblDia.setMinWidth(32); lblDia.setAlignment(Pos.CENTER);
            gridDias.add(lblDia, i, 0);
        }

        StackPane overlay = new StackPane(popup);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.4); -fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
        overlay.setAlignment(Pos.CENTER);
        overlay.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
        overlay.setFocusTraversable(false);

        Button btnLimpar = BotaoFactory.secundario("Limpar");
        btnLimpar.setMaxWidth(Double.MAX_VALUE);
        btnLimpar.setOnAction(ev -> {
            dataEntregaSelecionada[0] = null;
            campoDataEntregaTexto.clear();
            campoDataEntregaTexto.setStyle("""
                -fx-background-radius: 8; -fx-border-radius: 8;
                -fx-border-color: #ffd1dc; -fx-padding: 8 12; -fx-font-size: 13px;
            """);
            rootPrincipal.getChildren().remove(overlay);
        });

        popup.getChildren().addAll(navMes, gridDias, btnLimpar);

        Runnable[] renderRef = new Runnable[1];
        renderRef[0] = () -> {
            gridDias.getChildren().removeIf(n ->
                    GridPane.getRowIndex(n) != null && GridPane.getRowIndex(n) > 0);

            java.time.LocalDate primeiro = mesAtual[0];
            int diaSemanaInicio = primeiro.getDayOfWeek().getValue() % 7;

            lblMesAno.setText(
                    primeiro.getMonth().getDisplayName(java.time.format.TextStyle.FULL,
                                    new java.util.Locale("pt","BR"))
                            .substring(0,1).toUpperCase() +
                            primeiro.getMonth().getDisplayName(java.time.format.TextStyle.FULL,
                                    new java.util.Locale("pt","BR")).substring(1) +
                            " " + primeiro.getYear());

            int col = diaSemanaInicio, row = 1;
            for (int dia = 1; dia <= primeiro.lengthOfMonth(); dia++) {
                java.time.LocalDate dataAtual = primeiro.withDayOfMonth(dia);
                Button btnDia = new Button(String.valueOf(dia));
                btnDia.setMinWidth(32); btnDia.setMinHeight(32);

                boolean selecionado = dataEntregaSelecionada[0] != null
                        && dataEntregaSelecionada[0].equals(dataAtual);
                boolean hoje = dataAtual.equals(java.time.LocalDate.now());

                if (selecionado)
                    btnDia.setStyle("-fx-background-color: #ff4d6d; -fx-text-fill: white; -fx-background-radius: 20; -fx-cursor: hand; -fx-font-weight: bold;");
                else if (hoje)
                    btnDia.setStyle("-fx-background-color: #ffccd5; -fx-text-fill: #ff4d6d; -fx-background-radius: 20; -fx-cursor: hand; -fx-font-weight: bold;");
                else
                    btnDia.setStyle("-fx-background-color: transparent; -fx-text-fill: #333; -fx-background-radius: 20; -fx-cursor: hand;");

                btnDia.setOnAction(ev -> {
                    dataEntregaSelecionada[0] = dataAtual;
                    campoDataEntregaTexto.setText(dataAtual.format(fmtData));
                    rootPrincipal.getChildren().remove(overlay);
                });

                gridDias.add(btnDia, col, row);
                col++; if (col == 7) { col = 0; row++; }
            }
        };

        renderRef[0].run();
        btnAnterior.setOnAction(ev -> { mesAtual[0] = mesAtual[0].minusMonths(1); renderRef[0].run(); });
        btnProximo.setOnAction(ev  -> { mesAtual[0] = mesAtual[0].plusMonths(1);  renderRef[0].run(); });

        rootPrincipal.getChildren().add(overlay);
        popup.setScaleX(0); popup.setScaleY(0);
        ScaleTransition anim = new ScaleTransition(Duration.millis(200), popup);
        anim.setToX(1); anim.setToY(1); anim.play();

        overlay.setOnMouseClicked(ev -> {
            if (ev.getTarget() == overlay) rootPrincipal.getChildren().remove(overlay);
        });
    }
}