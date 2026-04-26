package com.gerenciador.sistema_loja.ui;

import com.gerenciador.sistema_loja.model.Produto;
import com.gerenciador.sistema_loja.model.tiposproduto.ProdutoSimples;
import com.gerenciador.sistema_loja.model.tiposproduto.Torta;
import com.gerenciador.sistema_loja.service.CarrinhoService;
import com.gerenciador.sistema_loja.service.ProdutoService;
import com.gerenciador.sistema_loja.ui.util.BotaoFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;
import java.math.BigDecimal;
import java.math.RoundingMode;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class TelaPrincipal {

    private ProdutoService produtoService;
    private StackPane rootPrincipal;
    private CarrinhoService carrinhoService;

    public TelaPrincipal(StackPane rootPrincipal, ProdutoService produtoService, CarrinhoService carrinhoService) {
        this.rootPrincipal = rootPrincipal;
        this.produtoService = produtoService;
        this.carrinhoService = carrinhoService;
    }

    public Parent criarTela() {

        // 🔹 ROOT = BorderPane para fixar footer embaixo
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
            TelaDoces tela = new TelaDoces(rootPrincipal, produtoService, carrinhoService);
            rootPrincipal.getChildren().setAll(tela.criarTela());
        });

        Button btnSalgados = BotaoFactory.primario("Salgados");
        btnSalgados.setOnAction(e -> {
            TelaSalgados tela = new TelaSalgados(rootPrincipal, produtoService, carrinhoService);
            rootPrincipal.getChildren().setAll(tela.criarTela());
        });

        Button btnTortas = BotaoFactory.primario("Tortas");
        btnTortas.setOnAction(e -> {
            TelaTortas tela = new TelaTortas(rootPrincipal, produtoService, carrinhoService);
            rootPrincipal.getChildren().setAll(tela.criarTela());
        });

        centro.getChildren().addAll(btnDoces, btnSalgados, btnTortas);

        // 🔹 BOTÕES DIREITA empilhados, altura igual, centralizados verticalmente
        Button btnEditarProdutos = BotaoFactory.secundario("Editar/Adicionar Produtos");
        btnEditarProdutos.setOnAction(e -> {
            TelaGerenciarProdutos tela = new TelaGerenciarProdutos(rootPrincipal, produtoService, carrinhoService);
            rootPrincipal.getChildren().setAll(tela.criarTela());
        });

        Button btnPedidos = BotaoFactory.secundario("Pedidos");
        btnPedidos.setOnAction(e -> {
            // futura tela de pedidos
            new Alert(Alert.AlertType.INFORMATION, "Em breve: tela de pedidos!").show();
        });

        // largura igual para os dois
        btnEditarProdutos.setMaxWidth(Double.MAX_VALUE);
        btnPedidos.setMaxWidth(Double.MAX_VALUE);

        VBox botoesDir = new VBox(8, btnEditarProdutos, btnPedidos);
        botoesDir.setAlignment(Pos.CENTER);
        botoesDir.setMinWidth(200);

        topoContainer.setLeft(logoBox);
        topoContainer.setCenter(centro);
        topoContainer.setRight(botoesDir);

        // alinhamento vertical de todos os três no BorderPane
        BorderPane.setAlignment(logoBox,   Pos.CENTER);
        BorderPane.setAlignment(centro,    Pos.CENTER);
        BorderPane.setAlignment(botoesDir, Pos.CENTER);

        // ── NOME DO CLIENTE (abaixo do topo, acima do carrinho) ──
        TextField nomeCliente = new TextField();
        nomeCliente.setPromptText("Nome do cliente");
        nomeCliente.setStyle("""
            -fx-background-radius: 8;
            -fx-border-radius: 8;
            -fx-border-color: #ffd1dc;
            -fx-padding: 8 12;
            -fx-font-size: 13px;
        """);

        VBox topoComNome = new VBox(10, topoContainer, nomeCliente);
        topoComNome.setPadding(new Insets(15, 15, 10, 15));

        root.setTop(topoComNome);

        // ── CARRINHO (centro, scrollável) ──────────────────
        VBox carrinhoBox = new VBox(0);
        carrinhoBox.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 10;
            -fx-border-color: #e0e0e0;
            -fx-border-radius: 10;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);
        """);
        VBox.setMargin(carrinhoBox, new Insets(0, 15, 0, 15));

        // header colunas com GridPane
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
        scroll.setStyle("""
            -fx-background: #ffe4ec;
            -fx-background-color: #ffe4ec;
        """);
        BorderPane.setMargin(scroll, new Insets(0, 0, 0, 0));

        root.setCenter(scroll);

        // ── FOOTER FIXO ────────────────────────────────────
        // separador tracejado
        Label tracejado = new Label(
                "- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -"
        );
        tracejado.setMaxWidth(Double.MAX_VALUE);
        tracejado.setStyle("-fx-text-fill: #ccc; -fx-font-size: 10px;");
        tracejado.setPadding(new Insets(6, 15, 2, 15));

        Button btnFinalizar = BotaoFactory.primario("Finalizar Pedido");
        btnFinalizar.setOnAction(e -> {
            if (carrinhoService.getItens().isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Carrinho vazio").show();
                return;
            }
            System.out.println("Pedido finalizado!");
            carrinhoService.limpar();
            atualizarCarrinho(listaCarrinho, totalLabel);
        });

        Button btnEditarPedido = BotaoFactory.secundario("Editar Pedido");
        btnEditarPedido.setOnAction(e ->
                mostrarPopupEditarPedido(listaCarrinho, totalLabel)
        );


        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox footerLinha = new HBox(10, totalLabel, spacer, btnEditarPedido, btnFinalizar);
        footerLinha.setAlignment(Pos.CENTER_LEFT);
        footerLinha.setPadding(new Insets(8, 15, 12, 15));

        VBox footer = new VBox(0, tracejado, footerLinha);
        footer.setStyle("""
            -fx-background-color: white;
            -fx-border-color: #e0e0e0 transparent transparent transparent;
            -fx-border-width: 1 0 0 0;
        """);

        root.setBottom(footer);

        return root;
    }

    // ── GRID DE CABEÇALHO ─────────────────────────────────
    private GridPane criarHeaderGrid() {

        GridPane grid = new GridPane();
        grid.setHgap(0);

        // larguras fixas: Produto ocupa o resto, as outras são fixas
        ColumnConstraints cProduto = new ColumnConstraints();
        cProduto.setHgrow(Priority.ALWAYS);
        cProduto.setFillWidth(true);

        ColumnConstraints cQtd = new ColumnConstraints(70);
        cQtd.setHalignment(javafx.geometry.HPos.CENTER);

        ColumnConstraints cUnit = new ColumnConstraints(100);
        cUnit.setHalignment(javafx.geometry.HPos.CENTER);

        ColumnConstraints cSub = new ColumnConstraints(110);
        cSub.setHalignment(javafx.geometry.HPos.CENTER);

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

    // ── LINHA DE ITEM (mesmo GridPane de colunas) ─────────
    private GridPane criarLinhaItem(String nome, int qtd, BigDecimal precoUnit, BigDecimal subtotal) {

        GridPane grid = new GridPane();
        grid.setHgap(0);
        grid.setPadding(new Insets(7, 15, 7, 15));
        grid.setStyle("-fx-border-color: transparent transparent #f5f5f5 transparent; -fx-border-width: 0 0 1 0;");

        ColumnConstraints cProduto = new ColumnConstraints();
        cProduto.setHgrow(Priority.ALWAYS);
        cProduto.setFillWidth(true);

        ColumnConstraints cQtd = new ColumnConstraints(70);
        cQtd.setHalignment(javafx.geometry.HPos.CENTER);

        ColumnConstraints cUnit = new ColumnConstraints(100);
        cUnit.setHalignment(javafx.geometry.HPos.CENTER);

        ColumnConstraints cSub = new ColumnConstraints(110);
        cSub.setHalignment(javafx.geometry.HPos.CENTER);

        grid.getColumnConstraints().addAll(cProduto, cQtd, cUnit, cSub);

        Label lNome = new Label(nome);
        lNome.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");

        Label lQtd = new Label("x" + qtd);
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

            Produto p = carrinhoService.getProduto(entry.getKey());
            int q = entry.getValue();

            BigDecimal precoUnit = BigDecimal.ZERO;
            if (p instanceof ProdutoSimples ps) precoUnit = ps.getPreco();
            else if (p instanceof Torta t)      precoUnit = t.getPrecoPorKg();

            BigDecimal subtotal = precoUnit.multiply(BigDecimal.valueOf(q));
            total = total.add(subtotal);

            listaCarrinho.getChildren().add(
                    criarLinhaItem(p.getNome(), q, precoUnit, subtotal)
            );
        }

        totalLabel.setText("TOTAL: R$ " + total.setScale(2, RoundingMode.HALF_UP));
    }

    private void mostrarPopupEditarPedido(VBox listaCarrinho, Label totalLabel) {

        // ── CARD ──────────────────────────────────────────────
        VBox popup = new VBox(12);
        popup.setPadding(new Insets(20));
        popup.setMaxWidth(420);
        popup.setStyle("""
        -fx-background-color: white;
        -fx-background-radius: 15;
        -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 20, 0, 0, 6);
    """);
        popup.setMaxHeight(520);
        popup.setPrefHeight(Region.USE_COMPUTED_SIZE);

        // ── LISTA DE ITENS ────────────────────────────────────
        VBox listaPopup = new VBox(6);

        // mapa mutável de quantidades locais para edição
        java.util.Map<Long, javafx.beans.property.SimpleIntegerProperty> qtdMap = new java.util.HashMap<>();
        java.util.Map<Long, javafx.beans.property.SimpleObjectProperty<BigDecimal>> precoMap = new java.util.HashMap<>();

        for (var entry : carrinhoService.getItens().entrySet()) {
            Long id = entry.getKey();
            Produto p = carrinhoService.getProduto(id);

            BigDecimal precoUnit = BigDecimal.ZERO;
            if (p instanceof ProdutoSimples ps) precoUnit = ps.getPreco();
            else if (p instanceof Torta t)      precoUnit = t.getPrecoPorKg();

            qtdMap.put(id, new javafx.beans.property.SimpleIntegerProperty(entry.getValue()));
            precoMap.put(id, new javafx.beans.property.SimpleObjectProperty<>(precoUnit));
        }

        // label total do popup — declarado antes para callbacks atualizarem
        Label totalPopupLabel = new Label();
        totalPopupLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px;");

        // campo desconto
        TextField campoDesconto = new TextField("0");
        campoDesconto.setStyle("""
        -fx-background-radius: 8;
        -fx-border-radius: 8;
        -fx-border-color: #ffd1dc;
        -fx-padding: 8 12;
        -fx-font-size: 13px;
    """);
        campoDesconto.setEditable(false);

        // referência ao botão editar para os callbacks
        Button[] btnEditarRef = new Button[1];

        Label lblSubtotalBruto = new Label("");
        lblSubtotalBruto.setStyle("-fx-font-size: 12px; -fx-text-fill: #aaa; -fx-strikethrough: true;");
        lblSubtotalBruto.managedProperty().bind(lblSubtotalBruto.visibleProperty());
        lblSubtotalBruto.setVisible(false);

        Label lblDescontoValor = new Label("");
        lblDescontoValor.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");
        lblDescontoValor.managedProperty().bind(lblDescontoValor.visibleProperty());
        lblDescontoValor.setVisible(false);

        // ── RECALCULAR TOTAL ──────────────────────────────────
        Runnable recalcular = () -> {
            BigDecimal subtotalGeral = BigDecimal.ZERO;
            for (Long id : qtdMap.keySet()) {
                int q = qtdMap.get(id).get();
                BigDecimal pu = precoMap.get(id).get();
                subtotalGeral = subtotalGeral.add(pu.multiply(BigDecimal.valueOf(q)));
            }

            BigDecimal desconto = BigDecimal.ZERO;
            try {
                desconto = new BigDecimal(campoDesconto.getText().replace(",", "."));
            } catch (Exception ignored) {}

            BigDecimal valorDesc = subtotalGeral
                    .multiply(desconto)
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

            BigDecimal totalFinal = subtotalGeral.subtract(valorDesc).setScale(2, RoundingMode.HALF_UP);

            if (valorDesc.compareTo(BigDecimal.ZERO) > 0) {
                lblSubtotalBruto.setText("Total: R$ " + subtotalGeral.setScale(2, RoundingMode.HALF_UP));
                lblSubtotalBruto.setVisible(true);
                lblDescontoValor.setText("Desconto: R$ " + valorDesc.setScale(2, RoundingMode.HALF_UP));
                lblDescontoValor.setVisible(true);
            } else {
                lblSubtotalBruto.setVisible(false);
                lblDescontoValor.setVisible(false);
            }

            totalPopupLabel.setText("TOTAL: R$ " + totalFinal);
        };

        // ── CONSTRUIR LINHAS ──────────────────────────────────
        Runnable construirLinhas = null; // declarado para referência circular
        // usamos array trick
        Runnable[] construirLinhasRef = new Runnable[1];

        construirLinhasRef[0] = () -> {
            listaPopup.getChildren().clear();

            for (Long id : new java.util.ArrayList<>(qtdMap.keySet())) {
                Produto p = carrinhoService.getProduto(id);

                javafx.beans.property.SimpleIntegerProperty qtdProp = qtdMap.get(id);
                javafx.beans.property.SimpleObjectProperty<BigDecimal> precoProp = precoMap.get(id);

                // quantidade
                Label lblQtd = new Label(String.valueOf(qtdProp.get()));
                lblQtd.setStyle("""
                        -fx-font-size: 12px;
                        -fx-font-weight: bold;
                        -fx-min-width: 18;
                        -fx-alignment: center;
                """);

                Button btnMenos = new Button("-");
                                btnMenos.setStyle("""
                    -fx-background-color: #6c757d;
                    -fx-text-fill: white;
                    -fx-background-radius: 15;
                    -fx-padding: 4 12;
                    -fx-font-weight: bold;
                    -fx-cursor: hand;
                """);

                Button btnMais = new Button("+");
                btnMais.setStyle("""
                    -fx-background-color: #ff4d6d;
                    -fx-text-fill: white;
                    -fx-background-radius: 15;
                    -fx-padding: 4 12;
                    -fx-font-weight: bold;
                    -fx-cursor: hand;
                """);

                // subtotal da linha
                BigDecimal subInicial = precoProp.get().multiply(BigDecimal.valueOf(qtdProp.get()));
                Label lblSub = new Label("R$ " + subInicial.setScale(2, RoundingMode.HALF_UP));
                lblSub.setStyle("""
                    -fx-font-size: 12px;
                    -fx-font-weight: bold;
                    -fx-text-fill: #333;
                """);
                lblSub.setPrefWidth(80);
                lblSub.setMinWidth(80);
                lblSub.setMaxWidth(80);
                lblSub.setAlignment(Pos.CENTER_RIGHT);

                // campo de preço (só editável quando modo edição ativo)
                TextField campoPreco = new TextField(precoProp.get().setScale(2, RoundingMode.HALF_UP).toString());
                campoPreco.setEditable(false);
                campoPreco.setPrefWidth(70);
                campoPreco.setMinWidth(70);
                campoPreco.setMaxWidth(70);
                campoPreco.setStyle("""
                    -fx-background-radius: 6;
                    -fx-border-radius: 6;
                    -fx-border-color: transparent;
                    -fx-padding: 4 6;
                    -fx-font-size: 12px;
                """);
                // ao editar preço manualmente
                campoPreco.textProperty().addListener((obs, o, n) -> {
                    try {
                        BigDecimal novoPreco = new BigDecimal(n.replace(",", "."));
                        precoProp.set(novoPreco);
                        BigDecimal sub = novoPreco.multiply(BigDecimal.valueOf(qtdProp.get()));
                        lblSub.setText("R$ " + sub.setScale(2, RoundingMode.HALF_UP));
                        recalcular.run();
                    } catch (Exception ignored) {}
                });

                btnMais.setOnAction(ev -> {
                    qtdProp.set(qtdProp.get() + 1);
                    carrinhoService.getItens().put(id, qtdProp.get());
                    lblQtd.setText(String.valueOf(qtdProp.get()));
                    BigDecimal sub = precoProp.get().multiply(BigDecimal.valueOf(qtdProp.get()));
                    lblSub.setText("R$ " + sub.setScale(2, RoundingMode.HALF_UP));
                    recalcular.run();
                    atualizarCarrinho(listaCarrinho, totalLabel);
                });

                btnMenos.setOnAction(ev -> {
                    int atual = qtdProp.get();
                    if (atual <= 1) {
                        // pergunta se quer remover
                        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                        confirm.setHeaderText("Remover \"" + p.getNome() + "\"?");
                        confirm.setContentText("Deseja retirar este produto do pedido?");
                        confirm.showAndWait().ifPresent(resp -> {
                            if (resp == ButtonType.OK) {
                                carrinhoService.remover(p);
                                qtdMap.remove(id);
                                precoMap.remove(id);
                                construirLinhasRef[0].run();
                                recalcular.run();
                                atualizarCarrinho(listaCarrinho, totalLabel);
                            }
                        });
                    } else {
                        qtdProp.set(atual - 1);
                        carrinhoService.getItens().put(id, qtdProp.get());
                        lblQtd.setText(String.valueOf(qtdProp.get()));
                        BigDecimal sub = precoProp.get().multiply(BigDecimal.valueOf(qtdProp.get()));
                        lblSub.setText("R$ " + sub.setScale(2, RoundingMode.HALF_UP));
                        recalcular.run();
                        atualizarCarrinho(listaCarrinho, totalLabel);
                    }
                });

                HBox controle = new HBox(6, btnMenos, lblQtd, btnMais);
                controle.setAlignment(Pos.CENTER);

                Label lblNome = new Label(p.getNome());
                lblNome.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");
                HBox.setHgrow(lblNome, Priority.ALWAYS);

                Region sp = new Region();
                HBox.setHgrow(sp, Priority.ALWAYS);

                HBox linha = new HBox(10, lblNome, controle, campoPreco, lblSub);
                linha.setAlignment(Pos.CENTER_LEFT);
                linha.setPadding(new Insets(6, 8, 6, 8));
                linha.setStyle("""
                    -fx-background-color: white;
                    -fx-border-color: transparent transparent #f5f5f5 transparent;
                    -fx-border-width: 0 0 1 0;
                """);
                linha.setUserData(campoPreco);

                // guarda referência ao campoPreco para ativar edição depois
                linha.setUserData(campoPreco);

                listaPopup.getChildren().add(linha);
            }

            recalcular.run();
        };

        construirLinhasRef[0].run();

        // ── DESCONTO ──────────────────────────────────────────
        Label lblDesconto = new Label("Desconto (%)");
        lblDesconto.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");
        campoDesconto.textProperty().addListener((obs, o, n) -> recalcular.run());

        VBox blocoDesconto = new VBox(4, lblDesconto, campoDesconto);

        // dispara cálculo inicial
        recalcular.run();

        // ── BOTÕES ────────────────────────────────────────────
        Button btnEditar = BotaoFactory.secundario("Editar Valores");
        Button btnOk     = BotaoFactory.primario("OK");
        btnEditarRef[0]  = btnEditar;

        // estado de edição
        boolean[] modoEdicao = {false};

        btnEditar.setOnAction(ev -> {
            modoEdicao[0] = !modoEdicao[0];

            // ativa/desativa campos de preço e desconto
            campoDesconto.setEditable(modoEdicao[0]);
            campoDesconto.setStyle(modoEdicao[0] ? """
            -fx-background-radius: 8;
            -fx-border-radius: 8;
            -fx-border-color: #ff4d6d;
            -fx-padding: 8 12;
            -fx-font-size: 13px;
        """ : """
            -fx-background-radius: 8;
            -fx-border-radius: 8;
            -fx-border-color: #ffd1dc;
            -fx-padding: 8 12;
            -fx-font-size: 13px;
        """);

            for (var node : listaPopup.getChildren()) {
                if (node instanceof HBox hb && hb.getUserData() instanceof TextField tf) {
                    tf.setEditable(modoEdicao[0]);
                    tf.setStyle(modoEdicao[0] ? """
                    -fx-background-radius: 6;
                    -fx-border-radius: 6;
                    -fx-border-color: #ff4d6d;
                    -fx-padding: 4 8;
                    -fx-font-size: 13px;
                    -fx-pref-width: 70;
                """ : """
                    -fx-background-radius: 6;
                    -fx-border-radius: 6;
                    -fx-border-color: transparent;
                    -fx-padding: 4 8;
                    -fx-font-size: 13px;
                    -fx-pref-width: 70;
                """);
                }
            }

            btnEditar.setText(modoEdicao[0] ? "Concluir Edição" : "Editar Valores");
        });

        HBox botoes = new HBox(10, btnEditar, btnOk);
        botoes.setAlignment(Pos.CENTER_RIGHT);

        // ── MONTA POPUP ───────────────────────────────────────
        popup.getChildren().addAll(listaPopup, blocoDesconto, lblSubtotalBruto, lblDescontoValor, totalPopupLabel, botoes);

        // ── OVERLAY ───────────────────────────────────────────
        StackPane overlay = new StackPane(popup);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.4);");
        overlay.setAlignment(Pos.CENTER);
        overlay.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);

        rootPrincipal.getChildren().add(overlay);

        // ── ANIMAÇÃO (igual TelaDoces) ────────────────────────
        popup.setScaleX(0);
        popup.setScaleY(0);

        ScaleTransition anim = new ScaleTransition(Duration.millis(200), popup);
        anim.setToX(1);
        anim.setToY(1);
        anim.play();

        btnOk.setOnAction(ev -> rootPrincipal.getChildren().remove(overlay));
    }
}