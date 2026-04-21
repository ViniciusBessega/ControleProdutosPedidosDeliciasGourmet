package com.gerenciador.sistema_loja.ui;

import com.gerenciador.sistema_loja.model.Produto;
import com.gerenciador.sistema_loja.model.tiposproduto.ProdutoSimples;
import com.gerenciador.sistema_loja.model.tiposproduto.Torta;
import com.gerenciador.sistema_loja.service.ProdutoService;
import com.gerenciador.sistema_loja.ui.util.BotaoFactory;
import javafx.collections.FXCollections;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.geometry.*;
import javafx.stage.Stage;

import java.util.List;

public class TelaGerenciarProdutos {

    private StackPane rootPrincipal;
    private ProdutoService service;


    public TelaGerenciarProdutos(StackPane rootPrincipal, ProdutoService service) {
        this.rootPrincipal = rootPrincipal;
        this.service = service;
    }

    public Parent criarTela() {

        VBox root = new VBox(15);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #ffe4ec;");



        ImageView logo = new ImageView(new Image("/logo.png"));
        VBox.setMargin(logo, new Insets(0, 0, 10, 0));

        logo.setStyle("-fx-cursor: hand;");
        logo.setFitHeight(170);
        logo.setPreserveRatio(true);

        logo.setOnMouseClicked(e -> {
            TelaPrincipal tela = new TelaPrincipal(rootPrincipal, service);
            rootPrincipal.getChildren().setAll(tela.criarTela());
        });

        // 🔙 VOLTAR
        Button btnVoltar = BotaoFactory.secundario("← Voltar");

        btnVoltar.setOnAction(e -> {
            TelaPrincipal tela = new TelaPrincipal(rootPrincipal, service);
            rootPrincipal.getChildren().setAll(tela.criarTela());
        });

        // 🔍 BUSCA
        TextField campoBusca = new TextField();
        campoBusca.setPromptText("Buscar produto pelo nome");

        // ➕ ADICIONAR
        Button btnAdicionar = BotaoFactory.primario("Adicionar Produto");

        btnAdicionar.setOnAction(e -> {
            TelaProdutoForm tela = new TelaProdutoForm(rootPrincipal, service, null);
            rootPrincipal.getChildren().setAll(tela.criarTela());
        });

        // 📋 LISTA
        ListView<HBox> lista = new ListView<>();
        lista.setPrefHeight(400);

        List<Produto> produtos = service.listar();

        for (Produto p : produtos) {

            String texto;

            if (p instanceof ProdutoSimples ps) {
                texto = p.getNome() + " - R$ " + ps.getPreco();
            } else if (p instanceof Torta t) {
                texto = p.getNome() + " - R$ " + t.getPrecoPorKg() + "/kg";
            } else {
                texto = p.getNome();
            }

            Label nome = new Label(texto);

            Button btnEditar = BotaoFactory.primario("Editar");
            Button btnExcluir = BotaoFactory.primario("Excluir");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox linha = new HBox(10, nome, spacer, btnEditar, btnExcluir);

            // EDITAR
            btnEditar.setOnAction(e -> {
                TelaProdutoForm tela = new TelaProdutoForm(rootPrincipal, service, p);
                rootPrincipal.getChildren().setAll(tela.criarTela());
            });

            // EXCLUIR
            btnExcluir.setOnAction(e -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);

                confirm.setHeaderText("Excluir produto?");
                confirm.showAndWait().ifPresent(resp -> {
                    if (resp == ButtonType.OK) {
                        service.deletar(p.getId());

                        rootPrincipal.getChildren().setAll(
                                new TelaGerenciarProdutos(rootPrincipal, service).criarTela()
                        );
                    }
                });
            });

            lista.getItems().add(linha);
        }

        root.getChildren().addAll(logo, btnVoltar, campoBusca, btnAdicionar, lista);

        return root;
    }
}