package com.gerenciador.sistema_loja.ui;

import com.gerenciador.sistema_loja.model.Produto;
import com.gerenciador.sistema_loja.model.tiposproduto.ProdutoSimples;
import com.gerenciador.sistema_loja.model.tiposproduto.Torta;
import com.gerenciador.sistema_loja.service.CarrinhoService;
import com.gerenciador.sistema_loja.service.PedidoPdfService;
import com.gerenciador.sistema_loja.service.PedidoService;
import com.gerenciador.sistema_loja.service.ProdutoService;
import com.gerenciador.sistema_loja.ui.util.BotaoFactory;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

public class TelaDoces {

    private StackPane rootPrincipal;
    private ProdutoService service;
    private CarrinhoService carrinhoService;
    private PedidoService pedidoService;
    private PedidoPdfService pedidoPdfService;

    public TelaDoces(StackPane rootPrincipal, ProdutoService service, CarrinhoService carrinhoService, PedidoService pedidoService, PedidoPdfService pedidoPdfService) {
        this.rootPrincipal = rootPrincipal;
        this.service = service;
        this.carrinhoService = carrinhoService;
        this.pedidoService = pedidoService;
        this.pedidoPdfService = pedidoPdfService;
    }

    public Parent criarTela() {

        BorderPane root = new BorderPane();

        root.setStyle("""
            -fx-background-color: #ffe4ec;
            -fx-focus-color: transparent;
            -fx-faint-focus-color: transparent;
        """);

        // 🔹 LOGO
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

        Label titulo = new Label("Doces");
        titulo.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        TextField campoBusca = new TextField();
        campoBusca.setPromptText("Buscar doce...");
        campoBusca.setFocusTraversable(false);
        campoBusca.setStyle("""
            -fx-background-radius: 8;
            -fx-border-radius: 8;
            -fx-border-color: #ffd1dc;
            -fx-padding: 8 12;
            -fx-font-size: 13px;
        """);

        VBox topo = new VBox(10, logoBox, btnVoltar, titulo, campoBusca);
        topo.setPadding(new Insets(15));

        VBox lista = new VBox(10);
        lista.setPadding(new Insets(0, 0, 80, 0));

        Label totalLabel = new Label("Itens: 0   Total: R$ 0.0");

        atualizarLista(lista, "", totalLabel);

        campoBusca.textProperty().addListener((obs, oldVal, newVal) -> {
            atualizarLista(lista, newVal, totalLabel);
        });

        ScrollPane scroll = new ScrollPane(lista);
        scroll.setFitToWidth(true);
        scroll.setFocusTraversable(false);

        scroll.setStyle("""
            -fx-background: #ffe4ec;
            -fx-background-color: #ffe4ec;
            -fx-control-inner-background: #ffe4ec;
        """);

        Button btnCarrinho = BotaoFactory.primario("Adicionar ao carrinho");
        btnCarrinho.setOnAction(e -> mostrarPopupCarrinho());

        HBox footer = new HBox(20, totalLabel, btnCarrinho);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(10));
        footer.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 10 10 0 0;
        """);

        root.setTop(topo);
        root.setCenter(scroll);
        root.setBottom(footer);

        return root;
    }

    private void atualizarLista(VBox lista, String textoBusca, Label totalLabel) {

        lista.getChildren().clear();

        List<ProdutoSimples> doces = service.listarDoces();

        if (textoBusca != null && !textoBusca.isBlank()) {
            String busca = textoBusca.toLowerCase();

            doces = doces.stream()
                    .filter(d -> d.getNome().toLowerCase().contains(busca))
                    .toList();
        }

        for (ProdutoSimples doce : doces) {

            Label nome = new Label(doce.getNome());
            nome.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

            Label preco = new Label("R$ " + doce.getPreco());

            int qAtual = carrinhoService.getItens().getOrDefault(doce.getId(), 0);

            Label quantidade = new Label(String.valueOf(qAtual));

            Button btnMenos = BotaoFactory.secundario("-");
            Button btnMais = BotaoFactory.primario("+");

            HBox controle = new HBox(5, btnMenos, quantidade, btnMais);
            controle.setAlignment(Pos.CENTER);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox linha = new HBox(10,
                    new VBox(nome, preco),
                    spacer,
                    controle
            );

            linha.setPadding(new Insets(10));

            atualizarCorLinha(linha, qAtual);

            btnMais.setOnAction(e -> {
                carrinhoService.adicionar(doce);

                int q = carrinhoService.getItens().getOrDefault(doce.getId(), 0);
                quantidade.setText(String.valueOf(q));

                atualizarCorLinha(linha, q);
                atualizarTotal(totalLabel);
            });

            btnMenos.setOnAction(e -> {
                carrinhoService.remover(doce);

                int q = carrinhoService.getItens().getOrDefault(doce.getId(), 0);
                quantidade.setText(String.valueOf(q));

                atualizarCorLinha(linha, q);
                atualizarTotal(totalLabel);
            });

            lista.getChildren().add(linha);
        }

        atualizarTotal(totalLabel);
    }

    private void atualizarCorLinha(HBox linha, int quantidade) {
        if (quantidade > 0) {
            linha.setStyle("""
                -fx-background-color: #ffe0e6;
                -fx-background-radius: 10;
                -fx-border-color: #ff4d6d;
                -fx-border-radius: 10;
            """);
        } else {
            linha.setStyle("""
                -fx-background-color: white;
                -fx-background-radius: 10;
                -fx-border-color: #ffd1dc;
                -fx-border-radius: 10;
            """);
        }
    }

    private void atualizarTotal(Label totalLabel) {

        BigDecimal total = carrinhoService.getItens().entrySet().stream()
                .reduce(BigDecimal.ZERO, (acc, e) -> {
                    Produto p = carrinhoService.getProduto(e.getKey());
                    BigDecimal q = BigDecimal.valueOf(e.getValue());

                    if (p instanceof ProdutoSimples ps) {
                        return acc.add(ps.getPreco().multiply(q));
                    }
                    if (p instanceof Torta t) {
                        return acc.add(t.getPrecoPorKg().multiply(q));
                    }
                    return acc;
                }, BigDecimal::add);

        int quantidadeTotal = carrinhoService.getItens().values().stream().mapToInt(i -> i).sum();
        totalLabel.setText("Itens: " + quantidadeTotal + "   Total: R$ " + total.setScale(2, RoundingMode.HALF_UP));
    }

    private void mostrarPopupCarrinho() {

        VBox popup = new VBox(10);
        popup.setPadding(new Insets(25));
        popup.setMaxWidth(350);
        popup.setStyle("-fx-background-color: white;-fx-background-radius: 15;");
        popup.setMaxHeight(Region.USE_PREF_SIZE);
        popup.setPrefHeight(Region.USE_COMPUTED_SIZE);

        Label titulo = new Label("Carrinho");
        titulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        VBox lista = new VBox(5);

        BigDecimal total = BigDecimal.ZERO;

        for (var entry : carrinhoService.getItens().entrySet()) {
            Long id = entry.getKey();
            BigDecimal q = BigDecimal.valueOf(entry.getValue());

            Produto p = carrinhoService.getProduto(id);

            BigDecimal subtotal = BigDecimal.ZERO;
            if (p instanceof ProdutoSimples ps) {
                subtotal = ps.getPreco().multiply(q);
            } else if (p instanceof Torta t) {
                subtotal = t.getPrecoPorKg().multiply(q);
            }

            total = total.add(subtotal);

            lista.getChildren().add(new Label(
                    p.getNome() + " x" + q + " - R$ " + subtotal.setScale(2, RoundingMode.HALF_UP)
            ));
        }

        Label totalLabel = new Label("Total: R$ " + total.setScale(2, RoundingMode.HALF_UP));
        totalLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Button confirmar = BotaoFactory.primario("Confirmar");
        Button cancelar = BotaoFactory.secundario("Cancelar");

        HBox botoes = new HBox(10, confirmar, cancelar);
        botoes.setAlignment(Pos.CENTER);

        popup.getChildren().addAll(titulo, lista, totalLabel, botoes);

        StackPane overlay = new StackPane();
        overlay.getChildren().add(popup);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.4);");
        overlay.setAlignment(Pos.CENTER);
        overlay.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);

        rootPrincipal.getChildren().add(overlay);

        cancelar.setOnAction(e -> rootPrincipal.getChildren().remove(overlay));

        confirmar.setOnAction(e -> {
            rootPrincipal.getChildren().remove(overlay);

            TelaPrincipal tela = new TelaPrincipal(rootPrincipal, service, carrinhoService, pedidoService, pedidoPdfService);
            rootPrincipal.getChildren().setAll(tela.criarTela());
        });

        popup.setScaleX(0);
        popup.setScaleY(0);

        ScaleTransition anim = new ScaleTransition(Duration.millis(200), popup);
        anim.setToX(1);
        anim.setToY(1);
        anim.play();
    }
}