package com.gerenciador.sistema_loja.ui;

import com.gerenciador.sistema_loja.service.ProdutoService;
import com.gerenciador.sistema_loja.ui.util.BotaoFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;



public class TelaPrincipal {

    private ProdutoService produtoService;
    private StackPane rootPrincipal;

    public TelaPrincipal(StackPane rootPrincipal, ProdutoService produtoService) {
        this.rootPrincipal = rootPrincipal;
        this.produtoService = produtoService;
    }

    public Parent criarTela() {

        VBox root = new VBox(15);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #ffe4ec;");
        root.setSpacing(20);


        BorderPane topoContainer = new BorderPane();

        //ESQUERDA

        ImageView logo = new ImageView(new Image("/logo.png"));
        VBox.setMargin(logo, new Insets(0, 0, 10, 0));


        logo.setFitHeight(170);
        logo.setPreserveRatio(true);
        logo.setCursor(Cursor.HAND);
        logo.setSmooth(true);

        //fazendo hbox pra nao clicar somente na letra do png
        HBox logoBox = new HBox(logo);
        logoBox.setAlignment(Pos.CENTER_LEFT);

        logoBox.setMinWidth(150);
        logoBox.setPrefHeight(150);
        logoBox.setMaxWidth(150);

        logoBox.setStyle("-fx-cursor: hand;");

        logoBox.setOnMouseClicked(e -> {
            rootPrincipal.getChildren().setAll(this.criarTela());
        });


        // 🔹 BOTÕES CENTRAIS
        HBox centro = new HBox(15);
        centro.setAlignment(Pos.CENTER);

        Button btnDoces = BotaoFactory.primario("Doces");
        Button btnSalgados = BotaoFactory.primario("Salgados");
        Button btnTortas = BotaoFactory.primario("Tortas");

        centro.getChildren().addAll(btnDoces, btnSalgados, btnTortas);

        // 🔹 BOTÃO DIREITA
        Button btnEditar = BotaoFactory.secundario("Editar/Adicionar Produtos");

        btnEditar.setOnAction(e -> {
            TelaGerenciarProdutos tela = new TelaGerenciarProdutos(rootPrincipal, produtoService);
            rootPrincipal.getChildren().setAll(tela.criarTela());
        });

        // 🔹 POSICIONAMENTO
        topoContainer.setLeft(logoBox);
        topoContainer.setCenter(centro);
        topoContainer.setRight(btnEditar);

        BorderPane.setAlignment(logo, Pos.CENTER_LEFT);
        BorderPane.setAlignment(btnEditar, Pos.CENTER_RIGHT);


        // 🔹 LISTA PRODUTOS
        ListView<String> listaProdutos = new ListView<>();
        listaProdutos.setPrefHeight(250);

        // 🔹 CARRINHO
        VBox carrinhoBox = new VBox(10);
        carrinhoBox.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-background-radius: 10;");

        Label carrinhoTitulo = new Label("Carrinho vazio");
        ListView<String> listaCarrinho = new ListView<>();

        TextField nomeCliente = new TextField();
        nomeCliente.setPromptText("Nome do cliente");

        Button btnFinalizar = BotaoFactory.primario("Finalizar Pedido");

        carrinhoBox.getChildren().addAll(carrinhoTitulo, listaCarrinho, nomeCliente, btnFinalizar);

        root.getChildren().addAll(topoContainer, listaProdutos, carrinhoBox);

        return root;
    }
}