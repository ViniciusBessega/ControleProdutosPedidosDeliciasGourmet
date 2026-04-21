package com.gerenciador.sistema_loja.ui;

import com.gerenciador.sistema_loja.model.Produto;
import com.gerenciador.sistema_loja.model.tiposproduto.ProdutoSimples;
import com.gerenciador.sistema_loja.model.tiposproduto.Torta;
import com.gerenciador.sistema_loja.service.ProdutoService;
import com.gerenciador.sistema_loja.ui.util.BotaoFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.math.BigDecimal;

public class TelaProdutoForm {

    private StackPane rootPrincipal;
    private ProdutoService service;
    private Produto produto; // null = novo

    public TelaProdutoForm(StackPane rootPrincipal, ProdutoService service, Produto produto) {
        this.rootPrincipal = rootPrincipal;
        this.service = service;
        this.produto = produto;
    }

    public Parent criarTela() {

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #ffe4ec;");
        root.setAlignment(Pos.TOP_CENTER);
        root.setMaxWidth(400);

        // 🔙 VOLTAR
        Button btnVoltar = BotaoFactory.secundario("← Voltar");

        btnVoltar.setOnAction(e -> {
            TelaGerenciarProdutos tela = new TelaGerenciarProdutos(rootPrincipal, service);
            rootPrincipal.getChildren().setAll(tela.criarTela());
        });

        // 🔹 CAMPOS
        TextField nome = new TextField();
        nome.setPromptText("Nome");

        ComboBox<String> tipo = new ComboBox<>();
        tipo.getItems().addAll("Doce/Salgado", "Torta");
        tipo.setValue("Doce/Salgado");
        tipo.setStyle("""
            -fx-background-radius: 10;
            -fx-padding: 5;
        """);

        TextField preco = new TextField();
        preco.setPromptText("Preço");

        TextField precoKg = new TextField();
        precoKg.setPromptText("Preço por Kg");

        TextField recheio = new TextField();
        recheio.setPromptText("Recheio");

        nome.setMaxWidth(300);
        tipo.setMaxWidth(300);
        preco.setMaxWidth(300);
        precoKg.setMaxWidth(300);
        recheio.setMaxWidth(300);

        // 🔹 VISIBILIDADE INICIAL
        precoKg.setVisible(false);
        recheio.setVisible(false);

        // 🔹 TROCA DE TIPO
        tipo.setOnAction(e -> {
            if (tipo.getValue().equals("Doce/Salgado")) {
                preco.setVisible(true);
                precoKg.setVisible(false);
                recheio.setVisible(false);
            } else {
                preco.setVisible(false);
                precoKg.setVisible(true);
                recheio.setVisible(true);
            }
        });

        // 🔥 SE FOR EDIÇÃO
        if (produto != null) {

            nome.setText(produto.getNome());

            if (produto instanceof ProdutoSimples ps) {
                tipo.setValue("Doce/Salgado");
                preco.setText(ps.getPreco().toString());

                preco.setVisible(true);
                precoKg.setVisible(false);
                recheio.setVisible(false);
            }

            if (produto instanceof Torta t) {
                tipo.setValue("Torta");
                precoKg.setText(t.getPrecoPorKg().toString());
                recheio.setText(t.getRecheio());

                preco.setVisible(false);
                precoKg.setVisible(true);
                recheio.setVisible(true);
            }
        }

        // 🔹 BOTÕES
        Button salvar = BotaoFactory.primario("Salvar");
        Button cancelar = BotaoFactory.primario("Cancelar");

        salvar.setOnAction(e -> {

            Produto p;

            if (produto == null) {
                // 🔹 NOVO
                if (tipo.getValue().equals("Doce/Salgado")) {
                    ProdutoSimples ps = new ProdutoSimples();
                    ps.setNome(nome.getText());
                    ps.setPreco(new BigDecimal(preco.getText()));
                    p = ps;

                } else {
                    Torta t = new Torta();
                    t.setNome(nome.getText());
                    t.setPrecoPorKg(new BigDecimal(precoKg.getText()));
                    t.setRecheio(recheio.getText());
                    p = t;
                }

            } else {
                // 🔹 EDIÇÃO
                p = produto;

                p.setNome(nome.getText());

                if (p instanceof ProdutoSimples ps) {
                    ps.setPreco(new BigDecimal(preco.getText()));
                }

                if (p instanceof Torta t) {
                    t.setPrecoPorKg(new BigDecimal(precoKg.getText()));
                    t.setRecheio(recheio.getText());
                }
            }

            service.salvar(p);
            voltar();
        });

        cancelar.setOnAction(e -> voltar());

        HBox botoes = new HBox(10, salvar, cancelar);
        botoes.setAlignment(Pos.CENTER);

        root.getChildren().addAll(
                btnVoltar,
                new Label("Produto"),
                nome,
                tipo,
                preco,
                precoKg,
                recheio,
                botoes
        );

        return root;
    }

    private void voltar() {
        TelaGerenciarProdutos tela = new TelaGerenciarProdutos(rootPrincipal, service);
        rootPrincipal.getChildren().setAll(tela.criarTela());
    }
}