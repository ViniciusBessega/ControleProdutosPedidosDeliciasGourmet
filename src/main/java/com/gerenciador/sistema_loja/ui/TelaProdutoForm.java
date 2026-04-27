package com.gerenciador.sistema_loja.ui;

import com.gerenciador.sistema_loja.model.Produto;
import com.gerenciador.sistema_loja.model.tiposproduto.ProdutoSimples;
import com.gerenciador.sistema_loja.model.tiposproduto.Torta;
import com.gerenciador.sistema_loja.model.tiposproduto.Categoria;
import com.gerenciador.sistema_loja.service.CarrinhoService;
import com.gerenciador.sistema_loja.service.PedidoPdfService;
import com.gerenciador.sistema_loja.service.PedidoService;
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
    private CarrinhoService carrinhoService;
    private PedidoService pedidoService;
    private PedidoPdfService pedidoPdfService;

    public TelaProdutoForm(StackPane rootPrincipal, ProdutoService service, Produto produto, CarrinhoService carrinhoService, PedidoService pedidoService, PedidoPdfService pedidoPdfService) {
        this.rootPrincipal = rootPrincipal;
        this.service = service;
        this.produto = produto;
        this.carrinhoService = carrinhoService;
        this.pedidoService = pedidoService;
        this.pedidoPdfService = pedidoPdfService;
    }

    public Parent criarTela() {

        // 🔹 CONTAINER (TELA TODA)
        StackPane container = new StackPane();
        container.setStyle("-fx-background-color: #ffe4ec;");
        container.setAlignment(Pos.CENTER);

        // 🔹 CARD CENTRAL
        VBox card = new VBox(15);
        card.setPadding(new Insets(25));
        card.setMaxWidth(450);
        card.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 15;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 15, 0, 0, 5);
        """);
        card.setMaxHeight(Region.USE_PREF_SIZE);

        // 🔙 VOLTAR
        Button btnVoltar = BotaoFactory.secundario("← Voltar");
        btnVoltar.setOnAction(e -> voltar());

        // 🔹 GRID (FORM PROFISSIONAL)
        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setAlignment(Pos.CENTER);


        // 🔹 CAMPOS

        ComboBox<String> tipo = new ComboBox<>();
        tipo.getItems().addAll("DOCE", "SALGADO", "TORTA");
        tipo.setValue("DOCE");

        TextArea nome = new TextArea();
        nome.setPromptText("Nome");
        nome.setWrapText(true);
        nome.setPrefRowCount(1);
        nome.setMaxHeight(60);

        TextField preco = new TextField();
        preco.setPromptText("Preço");

        TextField precoKg = new TextField();
        precoKg.setPromptText("Preço por Kg");

        TextArea recheio = new TextArea();
        recheio.setPromptText("Recheio");
        recheio.setWrapText(true);
        recheio.setPrefRowCount(3);

        // 🔹 LABELS
        Label lblPreco = new Label("Preço");
        Label lblPrecoKg = new Label("Preço por KG");
        Label lblRecheio = new Label("Recheio");

        // 🔥 CONTROLE DE LAYOUT (ESSENCIAL)
        lblPreco.managedProperty().bind(lblPreco.visibleProperty());
        preco.managedProperty().bind(preco.visibleProperty());

        lblPrecoKg.managedProperty().bind(lblPrecoKg.visibleProperty());
        precoKg.managedProperty().bind(precoKg.visibleProperty());

        lblRecheio.managedProperty().bind(lblRecheio.visibleProperty());
        recheio.managedProperty().bind(recheio.visibleProperty());

        // 🔹 VISIBILIDADE INICIAL
        precoKg.setVisible(false);
        lblPrecoKg.setVisible(false);
        recheio.setVisible(false);
        lblRecheio.setVisible(false);

        // 🔹 TROCA DE TIPO
        tipo.setOnAction(e -> {

            Categoria cat = Categoria.valueOf(tipo.getValue());

            if (cat == Categoria.TORTA) {
                preco.setVisible(false);
                lblPreco.setVisible(false);

                precoKg.setVisible(true);
                lblPrecoKg.setVisible(true);
                recheio.setVisible(true);
                lblRecheio.setVisible(true);

            } else {
                preco.setVisible(true);
                lblPreco.setVisible(true);

                precoKg.setVisible(false);
                lblPrecoKg.setVisible(false);
                recheio.setVisible(false);
                lblRecheio.setVisible(false);
            }
        });

        // 🔹 TAMANHO PADRÃO
        tipo.setMaxWidth(Double.MAX_VALUE);
        nome.setMaxWidth(Double.MAX_VALUE);
        preco.setMaxWidth(Double.MAX_VALUE);
        precoKg.setMaxWidth(Double.MAX_VALUE);
        recheio.setMaxWidth(Double.MAX_VALUE);

        // 🔥 GRID POSIÇÕES
        int row = 0;

        // 🔹 CATEGORIA
        Label lblCategoria = new Label("Categoria");
        lblCategoria.setStyle("-fx-font-weight: bold;");
        grid.add(lblCategoria, 0, row++);
        grid.add(tipo, 0, row++);

        // 🔹 NOME
        Label lblNome = new Label("Nome");
        lblNome.setStyle("-fx-font-weight: bold;");
        grid.add(lblNome, 0, row++);
        grid.add(nome, 0, row++);

        // 🔹 PREÇO
        lblPreco.setStyle("-fx-font-weight: bold;");
        grid.add(lblPreco, 0, row++);
        grid.add(preco, 0, row++);

        // 🔹 PREÇO KG
        lblPrecoKg.setStyle("-fx-font-weight: bold;");
        grid.add(lblPrecoKg, 0, row++);
        grid.add(precoKg, 0, row++);

        // 🔹 RECHEIO
        lblRecheio.setStyle("-fx-font-weight: bold;");
        grid.add(lblRecheio, 0, row++);
        grid.add(recheio, 0, row++);

        grid.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(tipo, Priority.ALWAYS);
        GridPane.setHgrow(nome, Priority.ALWAYS);
        GridPane.setHgrow(preco, Priority.ALWAYS);
        GridPane.setHgrow(precoKg, Priority.ALWAYS);
        GridPane.setHgrow(recheio, Priority.ALWAYS);

        // 🔥 EDIÇÃO
        if (produto != null) {

            nome.setText(produto.getNome());
            tipo.setValue(produto.getCategoria().name());

            if (produto instanceof ProdutoSimples ps) {
                preco.setText(ps.getPreco().toString());
            }

            if (produto instanceof Torta t) {
                precoKg.setText(t.getPrecoPorKg().toString());
                recheio.setText(t.getRecheio());

                tipo.setValue("TORTA");
                tipo.getOnAction().handle(null); // 🔥 força atualização visual
            }
        }

        // 🔹 BOTÕES
        Button salvar = BotaoFactory.primario("Salvar");
        Button cancelar = BotaoFactory.secundario("Cancelar");

        salvar.setOnAction(e -> {

            try {
                Categoria categoriaSelecionada = Categoria.valueOf(tipo.getValue());

                Produto p;

                if (produto == null) {

                    if (categoriaSelecionada == Categoria.TORTA) {
                        Torta t = new Torta();
                        t.setNome(nome.getText());
                        t.setPrecoPorKg(new BigDecimal(precoKg.getText().replace(",", ".")));
                        t.setRecheio(recheio.getText());
                        t.setCategoria(Categoria.TORTA);
                        p = t;

                    } else {
                        ProdutoSimples ps = new ProdutoSimples();
                        ps.setNome(nome.getText());
                        ps.setPreco(new BigDecimal(preco.getText().replace(",", ".")));
                        ps.setCategoria(categoriaSelecionada);
                        p = ps;
                    }

                } else {

                    p = produto;
                    p.setNome(nome.getText());

                    if (p instanceof ProdutoSimples ps) {
                        ps.setPreco(new BigDecimal(preco.getText().replace(",", ".")));
                        ps.setCategoria(categoriaSelecionada);
                    }

                    if (p instanceof Torta t) {
                        t.setPrecoPorKg(new BigDecimal(precoKg.getText().replace(",", ".")));
                        t.setRecheio(recheio.getText());
                        t.setCategoria(Categoria.TORTA);
                    }
                }

                service.salvar(p);
                voltar();

            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Erro ao salvar");
                alert.setContentText(ex.getMessage());
                alert.showAndWait();
            }
        });

        cancelar.setOnAction(e -> voltar());

        HBox botoes = new HBox(10, salvar, cancelar);
        botoes.setAlignment(Pos.CENTER);

        // 🔹 MONTA CARD
        card.getChildren().addAll(
                btnVoltar,
                grid,
                botoes
        );

        container.getChildren().add(card);
        return container;
    }

    private void voltar() {
        TelaGerenciarProdutos tela = new TelaGerenciarProdutos(rootPrincipal, service, carrinhoService, pedidoService, pedidoPdfService);
        rootPrincipal.getChildren().setAll(tela.criarTela());
    }
}