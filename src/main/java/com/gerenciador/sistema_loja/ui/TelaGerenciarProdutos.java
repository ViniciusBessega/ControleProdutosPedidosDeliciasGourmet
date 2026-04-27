package com.gerenciador.sistema_loja.ui;

import com.gerenciador.sistema_loja.model.Produto;
import com.gerenciador.sistema_loja.model.tiposproduto.ProdutoSimples;
import com.gerenciador.sistema_loja.model.tiposproduto.Torta;
import com.gerenciador.sistema_loja.service.CarrinhoService;
import com.gerenciador.sistema_loja.service.PedidoPdfService;
import com.gerenciador.sistema_loja.service.PedidoService;
import com.gerenciador.sistema_loja.service.ProdutoService;
import com.gerenciador.sistema_loja.ui.util.BotaoFactory;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.util.List;

public class TelaGerenciarProdutos {

    private StackPane rootPrincipal;
    private ProdutoService service;
    private CarrinhoService carrinhoService;
    private PedidoService pedidoService;
    private PedidoPdfService pedidoPdfService;

    private String filtroAtual = "TODOS";
    private String ordenacaoAtual = "Mais recente";

    public TelaGerenciarProdutos(StackPane rootPrincipal, ProdutoService service, CarrinhoService carrinhoService, PedidoService pedidoService, PedidoPdfService pedidoPdfService) {
        this.rootPrincipal = rootPrincipal;
        this.service = service;
        this.carrinhoService = carrinhoService;
        this.pedidoService = pedidoService;
        this.pedidoPdfService = pedidoPdfService;
    }

    public Parent criarTela() {

        VBox root = new VBox(15);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #ffe4ec;");

        // 🔹 LOGO (COM ÁREA CLICÁVEL IGUAL TELA PRINCIPAL)

        ImageView logo = new ImageView(new Image("/logo.png"));
        logo.setFitHeight(150);
        logo.setPreserveRatio(true);

        HBox logoBox = new HBox(logo);
        logoBox.setAlignment(Pos.CENTER_LEFT);
        logoBox.setStyle("-fx-cursor: hand;");
        logoBox.setOnMouseClicked(e -> rootPrincipal.getChildren().setAll(this.criarTela()));

        // 🔙 VOLTAR
        Button btnVoltar = BotaoFactory.secundario("← Voltar");
        btnVoltar.setOnAction(e -> {
            TelaPrincipal tela = new TelaPrincipal(rootPrincipal, service, carrinhoService, pedidoService, pedidoPdfService);
            rootPrincipal.getChildren().setAll(tela.criarTela());
        });

        // 📋 TABELA (CRIADA ANTES DE TUDO PRA NÃO DAR ERRO)
        TableView<Produto> tabela = new TableView<>();
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabela.setPrefHeight(500);

        // 🔍 BUSCA (AGORA FUNCIONA)
        TextField campoBusca = new TextField();
        campoBusca.setPromptText("Buscar produto pelo nome");
        campoBusca.setStyle("""
            -fx-background-radius: 8;
            -fx-border-radius: 8;
            -fx-border-color: #ffd1dc;
            -fx-padding: 8 12;
            -fx-font-size: 13px;
        """);

        campoBusca.textProperty().addListener((obs, oldVal, newVal) -> {
            atualizarTabela(tabela, newVal);
        });

        // ➕ ADICIONAR
        Button btnAdicionar = BotaoFactory.primario("Adicionar Produto");
        btnAdicionar.setOnAction(e -> {
            TelaProdutoForm tela = new TelaProdutoForm(rootPrincipal, service, null, carrinhoService, pedidoService, pedidoPdfService);
            rootPrincipal.getChildren().setAll(tela.criarTela());
        });

        // 🔎 FILTROS
        ComboBox<String> filtroCategoria = new ComboBox<>();
        filtroCategoria.getItems().addAll("TODOS", "DOCE", "SALGADO", "TORTA");
        filtroCategoria.setValue(filtroAtual);

        ComboBox<String> ordenacao = new ComboBox<>();
        ordenacao.getItems().addAll("Mais recente", "Mais antigo");
        ordenacao.setValue(ordenacaoAtual);

        HBox filtros = new HBox(10, filtroCategoria, ordenacao);

        // 🔥 AQUI ESTAVA O ERRO: agora atualiza sem recriar tela
        filtroCategoria.setOnAction(e -> {
            filtroAtual = filtroCategoria.getValue();
            atualizarTabela(tabela, campoBusca.getText());
        });

        ordenacao.setOnAction(e -> {
            ordenacaoAtual = ordenacao.getValue();
            atualizarTabela(tabela, campoBusca.getText());
        });

        // 🎨 ESTILO FILTROS
        String estiloFiltro = """
            -fx-background-color: #ffccd5;
            -fx-background-radius: 20;
            -fx-padding: 5 15;
            -fx-font-weight: bold;
        """;

        filtroCategoria.setStyle(estiloFiltro);
        ordenacao.setStyle(estiloFiltro);

        // ========================
        // 📋 COLUNAS DA TABELA
        // ========================

        // NOME
        TableColumn<Produto, String> colNome = new TableColumn<>("Nome");
        colNome.setReorderable(false);
        colNome.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(cell.getValue().getNome())
        );

        // CATEGORIA
        TableColumn<Produto, String> colCategoria = new TableColumn<>("Categoria");
        colCategoria.setReorderable(false);
        colCategoria.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(
                        cell.getValue().getCategoria().name()
                )
        );

        // PREÇO
        TableColumn<Produto, String> colPreco = new TableColumn<>("Preço");
        colPreco.setReorderable(false);
        colPreco.setCellValueFactory(cell -> {

            Produto p = cell.getValue();

            String texto;

            if (p instanceof ProdutoSimples ps) {
                texto = "R$ " + ps.getPreco();
            } else if (p instanceof Torta t) {
                texto = "R$ " + t.getPrecoPorKg() + "/kg";
            } else {
                texto = "-";
            }

            return new javafx.beans.property.SimpleStringProperty(texto);
        });

        // AÇÕES
        TableColumn<Produto, Void> colAcoes = new TableColumn<>("Ações");
        colAcoes.setReorderable(false);

        colAcoes.setCellFactory(param -> new TableCell<>() {

            private final Button btnEditar = BotaoFactory.primario("Editar");
            private final Button btnExcluir = BotaoFactory.primario("Excluir");
            private final HBox box = new HBox(10, btnEditar, btnExcluir);

            {
                box.setAlignment(Pos.CENTER);

                // 🔥 IMPORTANTE: NÃO usar getIndex() aqui
                btnEditar.setOnAction(e -> {
                    Produto p = getTableRow().getItem();

                    if (p != null) {
                        TelaProdutoForm tela = new TelaProdutoForm(rootPrincipal, service, p, carrinhoService, pedidoService, pedidoPdfService);
                        rootPrincipal.getChildren().setAll(tela.criarTela());
                    }
                });

                btnExcluir.setOnAction(e -> {
                    Produto p = getTableRow().getItem();

                    if (p != null) {
                        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                        confirm.setHeaderText("Excluir produto?");

                        confirm.showAndWait().ifPresent(resp -> {
                            if (resp == ButtonType.OK) {
                                service.deletar(p.getId());
                                atualizarTabela(getTableView(), "");
                            }
                        });
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    setGraphic(box);
                }
            }
        });

        tabela.getColumns().addAll(colNome, colCategoria, colPreco, colAcoes);

        javafx.application.Platform.runLater(() -> {

            // 🔹 fundo geral do header
            Pane header = (Pane) tabela.lookup("TableHeaderRow");

            if (header != null) {
                header.setStyle("""
            -fx-background-color: #ffccd5;
            -fx-border-color: transparent;
        """);
            }

            // 🔹 remove linhas verticais do header
            for (javafx.scene.Node n : tabela.lookupAll(".column-header-background")) {
                n.setStyle("""
            -fx-background-color: #ffccd5;
            -fx-border-color: transparent;
        """);
            }

            // 🔹 estiliza cada coluna (header individual)
            for (javafx.scene.Node n : tabela.lookupAll(".column-header")) {

                n.setStyle("""
            -fx-background-color: #ffccd5;
            -fx-border-width: 0;
            -fx-font-weight: bold;
            -fx-alignment: CENTER;
        """);

                // 🔥 HOVER
                n.setOnMouseEntered(e ->
                        n.setStyle("""
                    -fx-background-color: #ffb3c1;
                    -fx-border-width: 0;
                    -fx-font-weight: bold;
                    -fx-alignment: CENTER;
                """)
                );

                n.setOnMouseExited(e ->
                        n.setStyle("""
                    -fx-background-color: #ffccd5;
                    -fx-border-width: 0;
                    -fx-font-weight: bold;
                    -fx-alignment: CENTER;
                """)
                );
            }

            // 🔹 remove aquela linha cinza feia padrão
            for (javafx.scene.Node n : tabela.lookupAll(".filler")) {
                n.setStyle("-fx-background-color: #ffccd5;");
            }

        });

        // 🔄 PRIMEIRO LOAD
        atualizarTabela(tabela, "");

        // 🔹 ADD NA TELA
        root.getChildren().addAll(
                logoBox,
                btnVoltar,
                campoBusca,
                filtros,
                btnAdicionar,
                tabela
        );

        return root;
    }

    // ========================
    // 🔥 MÉTODO CENTRAL (FILTRO + BUSCA + ORDEM)
    // ========================
    private void atualizarTabela(TableView<Produto> tabela, String textoBusca) {

        List<Produto> produtos;

        // 🔹 FILTRO
        if (filtroAtual.equals("DOCE")) {
            produtos = new java.util.ArrayList<>(service.listarDoces());
        } else if (filtroAtual.equals("SALGADO")) {
            produtos = new java.util.ArrayList<>(service.listarSalgados());
        } else if (filtroAtual.equals("TORTA")) {
            produtos = new java.util.ArrayList<>(service.listarTortas());
        } else {
            produtos = service.listar();
        }

        // 🔍 BUSCA
        if (textoBusca != null && !textoBusca.isBlank()) {
            String busca = textoBusca.toLowerCase();

            produtos.removeIf(p ->
                    !p.getNome().toLowerCase().contains(busca)
            );
        }

        // 🔽 ORDENAÇÃO
        if (ordenacaoAtual.equals("Mais recente")) {
            produtos.sort((a, b) -> b.getId().compareTo(a.getId()));
        } else {
            produtos.sort((a, b) -> a.getId().compareTo(b.getId()));
        }

        tabela.setItems(FXCollections.observableArrayList(produtos));
    }
}