package com.gerenciador.sistema_loja.ui;

import com.gerenciador.sistema_loja.model.Pedido;
import com.gerenciador.sistema_loja.service.CarrinhoService;
import com.gerenciador.sistema_loja.service.PedidoPdfService;
import com.gerenciador.sistema_loja.service.PedidoService;
import com.gerenciador.sistema_loja.service.ProdutoService;
import com.gerenciador.sistema_loja.ui.util.BotaoFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;

public class TelaEditarPedido {

    private StackPane rootPrincipal;
    private ProdutoService produtoService;
    private CarrinhoService carrinhoService;
    private PedidoService pedidoService;
    private PedidoPdfService pedidoPdfService;
    private Pedido pedido;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public TelaEditarPedido(StackPane rootPrincipal, ProdutoService produtoService, CarrinhoService carrinhoService, PedidoService pedidoService, Pedido pedido, PedidoPdfService pedidoPdfService) {
        this.rootPrincipal  = rootPrincipal;
        this.produtoService = produtoService;
        this.carrinhoService = carrinhoService;
        this.pedidoService  = pedidoService;
        this.pedido = pedido;
        this.pedidoPdfService = pedidoPdfService;
    }

    public Parent criarTela() {

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #ffe4ec;");

        // ── TOPO ──────────────────────────────────────────
        ImageView logo = new ImageView(new Image("/logo.png"));
        logo.setFitHeight(150);
        logo.setPreserveRatio(true);

        HBox logoBox = new HBox(logo);
        logoBox.setStyle("-fx-cursor: hand;");
        logoBox.setOnMouseClicked(e -> voltar());

        Button btnVoltar = BotaoFactory.secundario("← Voltar");
        btnVoltar.setOnAction(e -> voltar());

        VBox topo = new VBox(10, logoBox, btnVoltar);
        topo.setPadding(new Insets(15, 15, 0, 15));
        root.setTop(topo);

        // ── CARD CENTRAL ──────────────────────────────────
        VBox card = new VBox(14);
        card.setPadding(new Insets(25));
        card.setMaxWidth(500);
        card.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 15;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 12, 0, 0, 4);
        """);

        // ── CAMPOS SOMENTE LEITURA ─────────────────────────
        String estiloLabel = "-fx-font-size: 11px; -fx-text-fill: #aaa; -fx-font-weight: bold;";
        String estiloValor = "-fx-font-size: 13px; -fx-text-fill: #555;";

        // data
        Label lblDataTitulo = new Label("DATA");
        lblDataTitulo.setStyle(estiloLabel);
        Label lblDataValor = new Label(pedido.getData() != null ? pedido.getData().format(FMT) : "-");
        lblDataValor.setStyle(estiloValor);

        // itens
        Label lblItensTitulo = new Label("ITENS");
        lblItensTitulo.setStyle(estiloLabel);

        VBox listaItens = new VBox(3);
        if (pedido.getItens() != null) {
            for (var item : pedido.getItens()) {
                String linha = item.getProduto().getNome()
                        + " x" + item.getQuantidade().intValue()
                        + " — R$ " + item.getPrecoUnitario()
                        .multiply(item.getQuantidade())
                        .setScale(2, RoundingMode.HALF_UP);
                Label lItem = new Label(linha);
                lItem.setStyle(estiloValor);
                listaItens.getChildren().add(lItem);
            }
        }

        // separador
        Separator sep = new Separator();

        // ── CAMPOS EDITÁVEIS ──────────────────────────────
        // nome
        Label lblNomeTitulo = new Label("NOME DO CLIENTE");
        lblNomeTitulo.setStyle(estiloLabel);
        TextField campoNome = new TextField(pedido.getNomeCliente());
        campoNome.setStyle("""
            -fx-background-radius: 8; -fx-border-radius: 8;
            -fx-border-color: #ffd1dc; -fx-padding: 8 12; -fx-font-size: 13px;
        """);

        // desconto
        Label lblDescontoTitulo = new Label("DESCONTO (R$)");
        lblDescontoTitulo.setStyle(estiloLabel);
        TextField campoDesconto = new TextField(
                pedido.getDesconto() != null
                        ? pedido.getDesconto().setScale(2, RoundingMode.HALF_UP).toString()
                        : "0.00");
        campoDesconto.setStyle(campoNome.getStyle());

        // observação
        Label lblObsTitulo = new Label("OBSERVAÇÃO");
        lblObsTitulo.setStyle(estiloLabel);
        TextArea campoObs = new TextArea(pedido.getObservacao() != null ? pedido.getObservacao() : "");
        campoObs.setWrapText(true);
        campoObs.setPrefRowCount(3);
        campoObs.setStyle("""
            -fx-background-radius: 8; -fx-border-radius: 8;
            -fx-border-color: #ffd1dc; -fx-padding: 8 12; -fx-font-size: 13px;
        """);

        // total
        Label lblTotalTitulo = new Label("TOTAL (R$)");
        lblTotalTitulo.setStyle(estiloLabel);
        TextField campoTotal = new TextField(
                pedido.getTotal() != null
                        ? pedido.getTotal().setScale(2, RoundingMode.HALF_UP).toString()
                        : "0.00");
        campoTotal.setStyle(campoNome.getStyle());

        card.getChildren().addAll(
                lblDataTitulo, lblDataValor,
                lblItensTitulo, listaItens,
                sep,
                lblNomeTitulo, campoNome,
                lblDescontoTitulo, campoDesconto,
                lblObsTitulo, campoObs,
                lblTotalTitulo, campoTotal
        );

        ScrollPane scrollCard = new ScrollPane(card);
        scrollCard.setFitToWidth(true);
        scrollCard.setFocusTraversable(false);
        scrollCard.setStyle("""
            -fx-background: #ffe4ec;
            -fx-background-color: #ffe4ec;
            -fx-focus-color: transparent;
            -fx-faint-focus-color: transparent;
        """);

        StackPane centro = new StackPane(scrollCard);
        centro.setPadding(new Insets(15));
        centro.setAlignment(Pos.TOP_CENTER);
        root.setCenter(centro);

        // ── FOOTER FIXO ───────────────────────────────────
        Button btnConfirmar = BotaoFactory.primario("Confirmar");
        Button btnCancelar  = BotaoFactory.secundario("Cancelar");

        btnConfirmar.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Salvar alterações");
            confirm.setHeaderText("Deseja salvar as alterações no pedido de \""
                    + pedido.getNomeCliente() + "\"?");
            confirm.showAndWait().ifPresent(resp -> {
                if (resp == ButtonType.OK) {
                    try {
                        pedido.setNomeCliente(campoNome.getText().trim());
                        pedido.setDesconto(new BigDecimal(
                                campoDesconto.getText().replace(",", ".")));
                        pedido.setObservacao(campoObs.getText().trim());
                        pedido.setTotal(new BigDecimal(
                                campoTotal.getText().replace(",", ".")));

                        pedidoService.salvarSemRecalcular(pedido);

                        Alert sucesso = new Alert(Alert.AlertType.INFORMATION);
                        sucesso.setTitle("Salvo");
                        sucesso.setHeaderText(null);
                        sucesso.setContentText("✅ Pedido atualizado com sucesso!");
                        sucesso.showAndWait(); // 👈 aguarda fechar e volta depois

                        voltar(); // 👈 fora do ifPresent, sempre executa após o alerta

                    } catch (Exception ex) {
                        Alert erro = new Alert(Alert.AlertType.ERROR);
                        erro.setTitle("Erro");
                        erro.setHeaderText("Não foi possível salvar.");
                        erro.setContentText(ex.getMessage());
                        erro.showAndWait();
                    }
                }
            });
        });

        btnCancelar.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Cancelar edição");
            confirm.setHeaderText("Deseja descartar as alterações?");
            confirm.setContentText("As mudanças não serão salvas.");
            confirm.showAndWait().ifPresent(resp -> {
                if (resp == ButtonType.OK) voltar();
            });
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox footerLinha = new HBox(10, spacer, btnCancelar, btnConfirmar);
        footerLinha.setAlignment(Pos.CENTER_RIGHT);
        footerLinha.setPadding(new Insets(12, 15, 12, 15));

        VBox footer = new VBox(footerLinha);
        footer.setStyle("""
            -fx-background-color: white;
            -fx-border-color: #e0e0e0 transparent transparent transparent;
            -fx-border-width: 1 0 0 0;
        """);

        root.setBottom(footer);

        return root;
    }

    private void voltar() {
        TelaPedidos tela = new TelaPedidos(rootPrincipal, produtoService, carrinhoService, pedidoService, pedidoPdfService);
        rootPrincipal.getChildren().setAll(tela.criarTela());
    }
}