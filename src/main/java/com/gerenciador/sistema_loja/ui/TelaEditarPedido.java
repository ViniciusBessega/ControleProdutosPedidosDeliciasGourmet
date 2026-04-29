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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TelaEditarPedido {

    private StackPane rootPrincipal;
    private ProdutoService produtoService;
    private CarrinhoService carrinhoService;
    private PedidoService pedidoService;
    private PedidoPdfService pedidoPdfService;
    private Pedido pedido;

    private static final DateTimeFormatter FMT     = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FMT_DIA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Map<Long, Integer> itensEditaveis = new LinkedHashMap<>();
    private Map<Long, Produto> produtosMap = new LinkedHashMap<>();
    private Map<Long, BigDecimal> precosMap = new LinkedHashMap<>();

    public TelaEditarPedido(StackPane rootPrincipal, ProdutoService produtoService,
                            CarrinhoService carrinhoService, PedidoService pedidoService,
                            Pedido pedido, PedidoPdfService pedidoPdfService) {
        this.rootPrincipal   = rootPrincipal;
        this.produtoService  = produtoService;
        this.carrinhoService = carrinhoService;
        this.pedidoService   = pedidoService;
        this.pedido          = pedido;
        this.pedidoPdfService = pedidoPdfService;

        if (pedido.getItens() != null) {
            for (ItemPedido item : pedido.getItens()) {
                Long produtoId = item.getProduto().getId();
                itensEditaveis.put(produtoId, item.getQuantidade().intValue());
                produtosMap.put(produtoId, item.getProduto());
                precosMap.put(produtoId, item.getPrecoUnitario());
            }
        }
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

        // ── ESTILOS COMPARTILHADOS ─────────────────────────
        String estiloLabel = "-fx-font-size: 11px; -fx-text-fill: #aaa; -fx-font-weight: bold;";
        String estiloValor = "-fx-font-size: 13px; -fx-text-fill: #555;";
        String estiloField = """
            -fx-background-radius: 8; -fx-border-radius: 8;
            -fx-border-color: #ffd1dc; -fx-padding: 8 12; -fx-font-size: 13px;
        """;
        String estiloCard  = """
            -fx-background-color: white;
            -fx-background-radius: 15;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 12, 0, 0, 4);
        """;

        // ══════════════════════════════════════════════════
        // CARD 1 — INFORMAÇÕES DO PEDIDO
        // ══════════════════════════════════════════════════
        VBox cardInfo = new VBox(14);
        cardInfo.setPadding(new Insets(25));
        cardInfo.setPrefWidth(420);
        cardInfo.setMaxWidth(420);
        cardInfo.setStyle(estiloCard);

        Label lblDataTitulo = new Label("DATA DO PEDIDO");
        lblDataTitulo.setStyle(estiloLabel);
        Label lblDataValor = new Label(pedido.getData() != null ? pedido.getData().format(FMT) : "-");
        lblDataValor.setStyle(estiloValor);

        Label lblNomeTitulo = new Label("NOME DO CLIENTE");
        lblNomeTitulo.setStyle(estiloLabel);
        TextField campoNome = new TextField(pedido.getNomeCliente());
        campoNome.setStyle(estiloField);

        Label lblDataEntregaTitulo = new Label("DATA DE ENTREGA");
        lblDataEntregaTitulo.setStyle(estiloLabel);

        LocalDate[] dataEntregaSelecionada = { pedido.getDataEntrega() };
        TextField campoDataEntrega = new TextField(
                pedido.getDataEntrega() != null ? pedido.getDataEntrega().format(FMT_DIA) : "");
        campoDataEntrega.setPromptText("dd/MM/yyyy");
        campoDataEntrega.setStyle(estiloField);
        HBox.setHgrow(campoDataEntrega, Priority.ALWAYS);

        campoDataEntrega.textProperty().addListener((obs, o, n) -> {
            try {
                dataEntregaSelecionada[0] = LocalDate.parse(n, FMT_DIA);
                campoDataEntrega.setStyle(estiloField.replace("#ffd1dc", "#ff4d6d"));
            } catch (Exception ex) {
                dataEntregaSelecionada[0] = null;
                campoDataEntrega.setStyle(estiloField);
            }
        });

        Button btnCalendario = new Button("📅");
        btnCalendario.setStyle("""
            -fx-background-color: #ff4d6d; -fx-text-fill: white;
            -fx-background-radius: 8; -fx-padding: 8 10;
            -fx-font-size: 13px; -fx-cursor: hand;
        """);
        btnCalendario.setOnAction(e -> mostrarPopupCalendario(dataEntregaSelecionada, campoDataEntrega));

        Button btnLimparData = BotaoFactory.secundario("✕");
        btnLimparData.setOnAction(e -> { dataEntregaSelecionada[0] = null; campoDataEntrega.clear(); });

        HBox campoDataBox = new HBox(6, campoDataEntrega, btnCalendario, btnLimparData);
        campoDataBox.setAlignment(Pos.CENTER_LEFT);

        Label lblObsTitulo = new Label("OBSERVAÇÃO");
        lblObsTitulo.setStyle(estiloLabel);
        TextArea campoObs = new TextArea(pedido.getObservacao() != null ? pedido.getObservacao() : "");
        campoObs.setWrapText(true);
        campoObs.setPrefRowCount(3);
        campoObs.setStyle(estiloField);

        Label lblDescontoTitulo = new Label("DESCONTO (R$)");
        lblDescontoTitulo.setStyle(estiloLabel);
        TextField campoDesconto = new TextField(
                pedido.getDesconto() != null
                        ? pedido.getDesconto().setScale(2, RoundingMode.HALF_UP).toString()
                        : "0.00");
        campoDesconto.setStyle(estiloField);

        Label lblTotalTitulo = new Label("TOTAL (R$)");
        lblTotalTitulo.setStyle(estiloLabel);
        TextField campoTotal = new TextField(
                pedido.getTotal() != null
                        ? pedido.getTotal().setScale(2, RoundingMode.HALF_UP).toString()
                        : "0.00");
        campoTotal.setStyle(estiloField);

        cardInfo.getChildren().addAll(
                lblDataTitulo, lblDataValor,
                new Separator(),
                lblNomeTitulo, campoNome,
                lblDataEntregaTitulo, campoDataBox,
                lblObsTitulo, campoObs,
                lblDescontoTitulo, campoDesconto,
                lblTotalTitulo, campoTotal
        );

        // ══════════════════════════════════════════════════
        // CARD 2 — ITENS DO PEDIDO (LARGURA AUMENTADA)
        // ══════════════════════════════════════════════════
        VBox cardItens = new VBox(14);
        cardItens.setPadding(new Insets(25));
        cardItens.setPrefWidth(700);
        cardItens.setMaxWidth(700);
        cardItens.setStyle(estiloCard);

        Label lblItensTitulo = new Label("ITENS DO PEDIDO");
        lblItensTitulo.setStyle(estiloLabel);

        VBox listaItens = new VBox(6);
        listaItens.setPadding(new Insets(0, 2, 0, 2));

        ScrollPane scrollItens = new ScrollPane(listaItens);
        scrollItens.setFitToWidth(true);
        scrollItens.setFocusTraversable(false);
        scrollItens.setPrefHeight(340);
        scrollItens.setStyle("""
            -fx-background: white; -fx-background-color: white;
            -fx-focus-color: transparent; -fx-faint-focus-color: transparent;
        """);
        VBox.setVgrow(scrollItens, Priority.ALWAYS);

        Runnable[] atualizarItensRef = new Runnable[1];
        atualizarItensRef[0] = () -> construirLinhasItens(listaItens, atualizarItensRef, campoTotal);
        atualizarItensRef[0].run();

        Button btnAdicionarItem = BotaoFactory.secundario("+ Adicionar Item");
        btnAdicionarItem.setMaxWidth(Double.MAX_VALUE);
        btnAdicionarItem.setOnAction(e -> mostrarPopupAdicionarItem(atualizarItensRef, campoTotal));

        cardItens.getChildren().addAll(lblItensTitulo, scrollItens, btnAdicionarItem);

        // ══════════════════════════════════════════════════
        // DOIS CARDS LADO A LADO, CENTRALIZADOS
        // ══════════════════════════════════════════════════
        HBox duosCards = new HBox(20, cardInfo, cardItens);
        duosCards.setAlignment(Pos.TOP_CENTER);
        duosCards.setPadding(new Insets(15));

        ScrollPane scrollExterno = new ScrollPane(duosCards);
        scrollExterno.setFitToWidth(true);
        scrollExterno.setFocusTraversable(false);
        scrollExterno.setStyle("""
            -fx-background: #ffe4ec; -fx-background-color: #ffe4ec;
            -fx-focus-color: transparent; -fx-faint-focus-color: transparent;
        """);

        root.setCenter(scrollExterno);

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
                        pedido.setDataEntrega(dataEntregaSelecionada[0]);
                        pedido.setDesconto(new BigDecimal(
                                campoDesconto.getText().replace(",", ".")));
                        pedido.setObservacao(campoObs.getText().trim());
                        pedido.setTotal(new BigDecimal(
                                campoTotal.getText().replace(",", ".")));

                        List<ItemPedido> novosItens = new ArrayList<>();
                        for (Map.Entry<Long, Integer> entry : new LinkedHashMap<>(itensEditaveis).entrySet()) {
                            Long produtoId = entry.getKey();
                            Produto p   = produtosMap.get(produtoId);
                            int     qtd = entry.getValue();

                            BigDecimal precoUnit = precosMap.getOrDefault(produtoId, BigDecimal.ZERO);

                            ItemPedido itemExistente = pedido.getItens() == null ? null :
                                    pedido.getItens().stream()
                                            .filter(it -> it.getProduto().getId().equals(p.getId()))
                                            .findFirst().orElse(null);

                            if (itemExistente != null) {
                                itemExistente.setQuantidade(BigDecimal.valueOf(qtd));
                                itemExistente.setPrecoUnitario(precoUnit);
                                novosItens.add(itemExistente);
                            } else {
                                ItemPedido novo = new ItemPedido();
                                novo.setProduto(p);
                                novo.setQuantidade(BigDecimal.valueOf(qtd));
                                novo.setPrecoUnitario(precoUnit);
                                novo.setPedido(pedido);
                                novosItens.add(novo);
                            }
                        }
                        pedido.getItens().clear();
                        pedido.getItens().addAll(novosItens);

                        pedidoService.salvarSemRecalcular(pedido);

                        Alert sucesso = new Alert(Alert.AlertType.INFORMATION);
                        sucesso.setTitle("Salvo");
                        sucesso.setHeaderText(null);
                        sucesso.setContentText("✅ Pedido atualizado com sucesso!");
                        sucesso.showAndWait();

                        voltar();

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

    // ── CONSTRÓI LINHAS DE ITENS ──────────────────────────
    private void construirLinhasItens(VBox listaItens, Runnable[] atualizarItensRef, TextField campoTotal) {

        listaItens.getChildren().clear();

        for (Map.Entry<Long, Integer> entry : new LinkedHashMap<>(itensEditaveis).entrySet()) {
            Long produtoId = entry.getKey();
            Produto p   = produtosMap.get(produtoId);
            int     qtd = entry.getValue();

            BigDecimal precoUnit = precosMap.getOrDefault(produtoId, BigDecimal.ZERO);

            BigDecimal subtotal = precoUnit.multiply(BigDecimal.valueOf(qtd))
                    .setScale(2, RoundingMode.HALF_UP);

            Label lblNome = new Label(p.getNome());
            lblNome.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #333;");
            lblNome.setWrapText(true);

            // campo editável do preço unitário
            Label lblUnitTitulo = new Label("Unit. R$");
            lblUnitTitulo.setStyle("-fx-font-size: 11px; -fx-text-fill: #aaa;");

            TextField campoPrecoUnit = new TextField(precoUnit.setScale(2, RoundingMode.HALF_UP).toString());
            campoPrecoUnit.setPrefWidth(80);
            campoPrecoUnit.setMaxWidth(80);
            campoPrecoUnit.setStyle("""
                -fx-background-radius: 6; -fx-border-radius: 6;
                -fx-border-color: #ffd1dc; -fx-padding: 4 6; -fx-font-size: 12px;
            """);

            Label lblSub = new Label("R$ " + subtotal);
            lblSub.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");

            // campo editável de quantidade
            TextField campoQtd = new TextField(String.valueOf(qtd));
            campoQtd.setPrefWidth(50);
            campoQtd.setMaxWidth(50);
            campoQtd.setStyle("""
                -fx-background-radius: 6; -fx-border-radius: 6;
                -fx-border-color: #ffd1dc; -fx-padding: 4 6; -fx-font-size: 12px;
                -fx-alignment: center;
            """);

            final Long produtoIdRef = produtoId;

            // Listener para preço
            campoPrecoUnit.textProperty().addListener((obs, o, n) -> {
                try {
                    BigDecimal novoPreco = new BigDecimal(n.replace(",", "."));
                    precosMap.put(produtoIdRef, novoPreco);
                    BigDecimal novoSub = novoPreco
                            .multiply(BigDecimal.valueOf(Integer.parseInt(campoQtd.getText())))
                            .setScale(2, RoundingMode.HALF_UP);
                    lblSub.setText("R$ " + novoSub);
                    campoPrecoUnit.setStyle("""
                        -fx-background-radius: 6; -fx-border-radius: 6;
                        -fx-border-color: #ff4d6d; -fx-padding: 4 6; -fx-font-size: 12px;
                    """);
                    recalcularTotal(campoTotal);
                } catch (Exception ignored) {
                    campoPrecoUnit.setStyle("""
                        -fx-background-radius: 6; -fx-border-radius: 6;
                        -fx-border-color: #ffd1dc; -fx-padding: 4 6; -fx-font-size: 12px;
                    """);
                }
            });

            // Listener para quantidade
            campoQtd.textProperty().addListener((obs, o, n) -> {
                if (!n.matches("\\d*")) {
                    campoQtd.setText(o);
                    return;
                }
                try {
                    int novaQtd = n.isEmpty() ? 0 : Integer.parseInt(n);
                    if (novaQtd == 0) {
                        itensEditaveis.remove(produtoIdRef);
                    } else {
                        itensEditaveis.put(produtoIdRef, novaQtd);
                    }
                    BigDecimal novoSub = precosMap.get(produtoIdRef)
                            .multiply(BigDecimal.valueOf(novaQtd))
                            .setScale(2, RoundingMode.HALF_UP);
                    lblSub.setText("R$ " + novoSub);
                    campoQtd.setStyle("""
                        -fx-background-radius: 6; -fx-border-radius: 6;
                        -fx-border-color: #ff4d6d; -fx-padding: 4 6; -fx-font-size: 12px;
                        -fx-alignment: center;
                    """);
                    recalcularTotal(campoTotal);
                } catch (Exception ignored) {}
            });

            HBox linhaPreco = new HBox(6, lblUnitTitulo, campoPrecoUnit, new Label("Qtd:"), campoQtd, lblSub);
            linhaPreco.setAlignment(Pos.CENTER_LEFT);

            VBox infoBox = new VBox(4, lblNome, linhaPreco);
            HBox.setHgrow(infoBox, Priority.ALWAYS);

            Button btnRemover = new Button("Remover");
            btnRemover.setStyle("""
                -fx-background-color: transparent;
                -fx-text-fill: #ff4d6d;
                -fx-border-color: #ff4d6d;
                -fx-border-radius: 15; -fx-background-radius: 15;
                -fx-padding: 4 10; -fx-font-size: 11px; -fx-cursor: hand;
            """);

            HBox linha = new HBox(10, infoBox, btnRemover);
            linha.setAlignment(Pos.CENTER_LEFT);
            linha.setPadding(new Insets(8, 10, 8, 10));
            linha.setStyle("""
                -fx-background-color: #fafafa;
                -fx-background-radius: 8;
                -fx-border-color: #f0f0f0;
                -fx-border-radius: 8;
            """);

            btnRemover.setOnAction(e -> {
                itensEditaveis.remove(produtoIdRef);
                precosMap.remove(produtoIdRef);
                produtosMap.remove(produtoIdRef);
                atualizarItensRef[0].run();
                recalcularTotal(campoTotal);
            });

            listaItens.getChildren().add(linha);
        }

        if (itensEditaveis.isEmpty()) {
            Label vazio = new Label("Nenhum item no pedido.");
            vazio.setStyle("-fx-text-fill: #aaa; -fx-font-size: 12px;");
            listaItens.getChildren().add(vazio);
        }
    }

    // ── RECALCULA TOTAL ───────────────────────────────────
    private void recalcularTotal(TextField campoTotal) {
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<Long, Integer> entry : new LinkedHashMap<>(itensEditaveis).entrySet()) {
            Long produtoId = entry.getKey();
            BigDecimal pu = precosMap.getOrDefault(produtoId, BigDecimal.ZERO);
            total = total.add(pu.multiply(BigDecimal.valueOf(entry.getValue())));
        }
        campoTotal.setText(total.setScale(2, RoundingMode.HALF_UP).toString());
    }

    // ── POPUP ADICIONAR ITEM ──────────────────────────────
    private void mostrarPopupAdicionarItem(Runnable[] atualizarItensRef, TextField campoTotal) {

        Map<Produto, Integer> qtdTemp = new LinkedHashMap<>();

        BorderPane popup = new BorderPane();
        popup.setMaxWidth(480);
        popup.setMaxHeight(560);
        popup.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 15;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 20, 0, 0, 6);
        """);

        Label lblTitulo = new Label("Adicionar Item");
        lblTitulo.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        ToggleGroup grupo = new ToggleGroup();
        ToggleButton btnTodos    = criarFiltroBtn("Todos",    grupo);
        ToggleButton btnDoces    = criarFiltroBtn("Doces",    grupo);
        ToggleButton btnSalgados = criarFiltroBtn("Salgados", grupo);
        ToggleButton btnTortas   = criarFiltroBtn("Tortas",   grupo);
        btnTodos.setSelected(true);

        HBox filtros = new HBox(6, btnTodos, btnDoces, btnSalgados, btnTortas);
        filtros.setAlignment(Pos.CENTER_LEFT);

        TextField campoBusca = new TextField();
        campoBusca.setPromptText("Buscar produto...");
        campoBusca.setStyle("""
            -fx-background-radius: 8; -fx-border-radius: 8;
            -fx-border-color: #ffd1dc; -fx-padding: 8 12; -fx-font-size: 13px;
        """);

        VBox topoPopup = new VBox(10, lblTitulo, filtros, campoBusca);
        topoPopup.setPadding(new Insets(16, 16, 8, 16));
        topoPopup.setStyle("""
            -fx-background-color: white;
            -fx-border-color: transparent transparent #f0f0f0 transparent;
            -fx-border-width: 0 0 1 0; -fx-background-radius: 15 15 0 0;
        """);
        popup.setTop(topoPopup);

        VBox listaProdutos = new VBox(6);
        listaProdutos.setPadding(new Insets(10, 16, 10, 16));

        ScrollPane scrollLista = new ScrollPane(listaProdutos);
        scrollLista.setFitToWidth(true);
        scrollLista.setFocusTraversable(false);
        scrollLista.setPrefHeight(340);
        scrollLista.setStyle("""
            -fx-background: white; -fx-background-color: white;
            -fx-focus-color: transparent; -fx-faint-focus-color: transparent;
        """);
        popup.setCenter(scrollLista);

        String[] filtroAtual = {"TODOS"};

        Runnable[] construirListaRef = new Runnable[1];
        construirListaRef[0] = () -> {
            listaProdutos.getChildren().clear();

            List<Produto> produtos = switch (filtroAtual[0]) {
                case "DOCE"    -> new ArrayList<>(produtoService.listarDoces());
                case "SALGADO" -> new ArrayList<>(produtoService.listarSalgados());
                case "TORTA"   -> new ArrayList<>(produtoService.listarTortas());
                default        -> produtoService.listar();
            };

            String busca = campoBusca.getText().toLowerCase();
            if (!busca.isBlank()) {
                produtos = produtos.stream()
                        .filter(p -> p.getNome().toLowerCase().contains(busca))
                        .toList();
            }

            for (Produto p : produtos) {
                BigDecimal pu = BigDecimal.ZERO;
                if (p instanceof ProdutoSimples ps) pu = ps.getPreco();
                else if (p instanceof Torta t)      pu = t.getPrecoPorKg();

                Label lblNome  = new Label(p.getNome());
                lblNome.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #333;");
                Label lblPreco = new Label("R$ " + pu.setScale(2, RoundingMode.HALF_UP));
                lblPreco.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");

                VBox infoBox = new VBox(2, lblNome, lblPreco);
                HBox.setHgrow(infoBox, Priority.ALWAYS);

                HBox[] linhaRef = new HBox[1];

                int qtdAtual = qtdTemp.getOrDefault(p, 0);
                TextField lblQtd = new TextField(String.valueOf(qtdAtual));
                lblQtd.setPrefWidth(45);
                lblQtd.setMaxWidth(45);
                lblQtd.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-alignment: center; -fx-background-radius: 6; -fx-border-radius: 6; -fx-border-color: #ffd1dc; -fx-padding: 2 4;");
                lblQtd.textProperty().addListener((obs, o, n) -> {
                    if (!n.matches("\\d{0,3}")) { lblQtd.setText(o); return; }
                    try {
                        int val = Integer.parseInt(n);
                        if (val == 0) qtdTemp.remove(p);
                        else qtdTemp.put(p, val);
                        atualizarCorLinhaPopup(linhaRef[0], val);
                    } catch (Exception ignored) {}
                });

                Button bMenos = new Button("-");
                bMenos.setStyle("""
                    -fx-background-color: #6c757d; -fx-text-fill: white;
                    -fx-background-radius: 15; -fx-padding: 4 10;
                    -fx-font-weight: bold; -fx-cursor: hand;
                """);

                Button bMais = new Button("+");
                bMais.setStyle("""
                    -fx-background-color: #ff4d6d; -fx-text-fill: white;
                    -fx-background-radius: 15; -fx-padding: 4 10;
                    -fx-font-weight: bold; -fx-cursor: hand;
                """);

                HBox controles = new HBox(6, bMenos, lblQtd, bMais);
                controles.setAlignment(Pos.CENTER_RIGHT);

                HBox linha = new HBox(10, infoBox, controles);
                linhaRef[0] = linha;
                linha.setAlignment(Pos.CENTER_LEFT);
                linha.setPadding(new Insets(8, 10, 8, 10));
                atualizarCorLinhaPopup(linha, qtdAtual);

                final Produto prodRef = p;

                bMais.setOnAction(ev -> {
                    int q = qtdTemp.getOrDefault(prodRef, 0) + 1;
                    qtdTemp.put(prodRef, q);
                    lblQtd.setText(String.valueOf(q));
                    atualizarCorLinhaPopup(linha, q);
                });

                bMenos.setOnAction(ev -> {
                    int q = qtdTemp.getOrDefault(prodRef, 0);
                    if (q > 0) {
                        q--;
                        if (q == 0) qtdTemp.remove(prodRef);
                        else        qtdTemp.put(prodRef, q);
                        lblQtd.setText(String.valueOf(q));
                        atualizarCorLinhaPopup(linha, q);
                    }
                });

                listaProdutos.getChildren().add(linha);
            }
        };

        construirListaRef[0].run();

        btnTodos.setOnAction(e    -> { filtroAtual[0] = "TODOS";   construirListaRef[0].run(); });
        btnDoces.setOnAction(e    -> { filtroAtual[0] = "DOCE";    construirListaRef[0].run(); });
        btnSalgados.setOnAction(e -> { filtroAtual[0] = "SALGADO"; construirListaRef[0].run(); });
        btnTortas.setOnAction(e   -> { filtroAtual[0] = "TORTA";   construirListaRef[0].run(); });
        campoBusca.textProperty().addListener((obs, o, n) -> construirListaRef[0].run());

        Button btnOk       = BotaoFactory.primario("Confirmar");
        Button btnCancelar = BotaoFactory.secundario("Cancelar");

        HBox botoes = new HBox(10, btnCancelar, btnOk);
        botoes.setAlignment(Pos.CENTER_RIGHT);
        botoes.setPadding(new Insets(12, 16, 14, 16));
        botoes.setStyle("""
            -fx-background-color: white;
            -fx-border-color: #f0f0f0 transparent transparent transparent;
            -fx-border-width: 1 0 0 0; -fx-background-radius: 0 0 15 15;
        """);
        popup.setBottom(botoes);

        StackPane overlay = new StackPane(popup);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.4); -fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
        overlay.setAlignment(Pos.CENTER);
        overlay.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
        overlay.setFocusTraversable(false);

        rootPrincipal.getChildren().add(overlay);

        popup.setScaleX(0); popup.setScaleY(0);
        ScaleTransition anim = new ScaleTransition(Duration.millis(200), popup);
        anim.setToX(1); anim.setToY(1); anim.play();

        btnOk.setOnAction(ev -> {
            for (Map.Entry<Produto, Integer> entry : qtdTemp.entrySet()) {
                if (entry.getValue() > 0) {
                    Long prodId = entry.getKey().getId();
                    // Se já existe, soma as quantidades
                    if (itensEditaveis.containsKey(prodId)) {
                        itensEditaveis.put(prodId, itensEditaveis.get(prodId) + entry.getValue());
                    } else {
                        itensEditaveis.put(prodId, entry.getValue());
                        produtosMap.put(prodId, entry.getKey());

                        BigDecimal precoUnitario = BigDecimal.ZERO;
                        if (entry.getKey() instanceof ProdutoSimples ps) {
                            precoUnitario = ps.getPreco();
                        } else if (entry.getKey() instanceof Torta t) {
                            precoUnitario = t.getPrecoPorKg();
                        }
                        precosMap.put(prodId, precoUnitario);
                    }
                }
            }
            rootPrincipal.getChildren().remove(overlay);
            atualizarItensRef[0].run();
            recalcularTotal(campoTotal);
        });

        btnCancelar.setOnAction(ev -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Cancelar");
            confirm.setHeaderText("Deseja descartar os itens selecionados?");
            confirm.showAndWait().ifPresent(resp -> {
                if (resp == ButtonType.OK) rootPrincipal.getChildren().remove(overlay);
            });
        });
    }

    // ── POPUP CALENDÁRIO ─────────────────────────────────
    private void mostrarPopupCalendario(LocalDate[] dataEntregaSelecionada, TextField campoDataEntrega) {

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

        LocalDate[] mesAtual = {
                dataEntregaSelecionada[0] != null
                        ? dataEntregaSelecionada[0].withDayOfMonth(1)
                        : LocalDate.now().withDayOfMonth(1)
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
        gridDias.setHgap(4);
        gridDias.setVgap(4);
        gridDias.setAlignment(Pos.CENTER);

        String[] diasSemana = {"Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb"};
        for (int i = 0; i < 7; i++) {
            Label lblDia = new Label(diasSemana[i]);
            lblDia.setStyle("-fx-font-size: 10px; -fx-text-fill: #aaa; -fx-font-weight: bold;");
            lblDia.setMinWidth(32);
            lblDia.setAlignment(Pos.CENTER);
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
            campoDataEntrega.clear();
            rootPrincipal.getChildren().remove(overlay);
        });

        popup.getChildren().addAll(navMes, gridDias, btnLimpar);

        Runnable[] renderRef = new Runnable[1];
        renderRef[0] = () -> {
            gridDias.getChildren().removeIf(n ->
                    GridPane.getRowIndex(n) != null && GridPane.getRowIndex(n) > 0);

            LocalDate primeiro        = mesAtual[0];
            int       diaSemanaInicio = primeiro.getDayOfWeek().getValue() % 7;

            lblMesAno.setText(
                    primeiro.getMonth().getDisplayName(java.time.format.TextStyle.FULL,
                                    new java.util.Locale("pt", "BR"))
                            .substring(0, 1).toUpperCase() +
                            primeiro.getMonth().getDisplayName(java.time.format.TextStyle.FULL,
                                            new java.util.Locale("pt", "BR"))
                                    .substring(1) +
                            " " + primeiro.getYear()
            );

            int col = diaSemanaInicio, row = 1;
            for (int dia = 1; dia <= primeiro.lengthOfMonth(); dia++) {
                LocalDate dataAtual   = primeiro.withDayOfMonth(dia);
                Button    btnDia      = new Button(String.valueOf(dia));
                boolean   selecionado = dataEntregaSelecionada[0] != null
                        && dataEntregaSelecionada[0].equals(dataAtual);
                boolean   hoje        = dataAtual.equals(LocalDate.now());

                btnDia.setMinWidth(32);
                btnDia.setMinHeight(32);

                if (selecionado) {
                    btnDia.setStyle("-fx-background-color: #ff4d6d; -fx-text-fill: white; -fx-background-radius: 20; -fx-cursor: hand; -fx-font-weight: bold;");
                } else if (hoje) {
                    btnDia.setStyle("-fx-background-color: #ffccd5; -fx-text-fill: #ff4d6d; -fx-background-radius: 20; -fx-cursor: hand; -fx-font-weight: bold;");
                } else {
                    btnDia.setStyle("-fx-background-color: transparent; -fx-text-fill: #333; -fx-background-radius: 20; -fx-cursor: hand;");
                }

                btnDia.setOnAction(ev -> {
                    dataEntregaSelecionada[0] = dataAtual;
                    campoDataEntrega.setText(dataAtual.format(FMT_DIA));
                    rootPrincipal.getChildren().remove(overlay);
                });

                gridDias.add(btnDia, col, row);
                col++;
                if (col == 7) { col = 0; row++; }
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

    // ── HELPER: toggle button de filtro ──────────────────
    private ToggleButton criarFiltroBtn(String texto, ToggleGroup grupo) {
        ToggleButton btn = new ToggleButton(texto);
        btn.setToggleGroup(grupo);
        btn.setCursor(javafx.scene.Cursor.HAND);
        String estiloNormal = """
            -fx-background-color: #f5f5f5; -fx-text-fill: #555;
            -fx-background-radius: 20; -fx-padding: 5 14;
            -fx-font-weight: bold; -fx-font-size: 12px;
        """;
        String estiloSelecionado = """
            -fx-background-color: #ff4d6d; -fx-text-fill: white;
            -fx-background-radius: 20; -fx-padding: 5 14;
            -fx-font-weight: bold; -fx-font-size: 12px;
        """;
        btn.setStyle(estiloNormal);
        btn.selectedProperty().addListener((obs, o, selected) ->
                btn.setStyle(selected ? estiloSelecionado : estiloNormal));
        return btn;
    }

    // ── HELPER: cor de linha no popup de adicionar ────────
    private void atualizarCorLinhaPopup(HBox linha, int qtd) {
        if (qtd > 0) {
            linha.setStyle("""
                -fx-background-color: #ffe0e6; -fx-background-radius: 8;
                -fx-border-color: #ff4d6d; -fx-border-radius: 8;
            """);
        } else {
            linha.setStyle("""
                -fx-background-color: #fafafa; -fx-background-radius: 8;
                -fx-border-color: #f0f0f0; -fx-background-radius: 8;
            """);
        }
    }

    private void voltar() {
        TelaPedidos tela = new TelaPedidos(rootPrincipal, produtoService, carrinhoService, pedidoService, pedidoPdfService);
        rootPrincipal.getChildren().setAll(tela.criarTela());
    }
}