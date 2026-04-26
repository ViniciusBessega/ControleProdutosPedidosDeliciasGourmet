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
        Button btnEditarPedido = BotaoFactory.secundario("Editar Pedido");

        btnFinalizar.setOnAction(e -> {
            if (carrinhoService.getItens().isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Carrinho vazio").show();
                return;
            }
            System.out.println("Pedido finalizado!");
            carrinhoService.limpar();
            atualizarCarrinho(listaCarrinho, totalLabel);
        });

        btnEditarPedido.setOnAction(e ->
                new Alert(Alert.AlertType.INFORMATION, "Em breve: edição de pedido!").show()
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
}