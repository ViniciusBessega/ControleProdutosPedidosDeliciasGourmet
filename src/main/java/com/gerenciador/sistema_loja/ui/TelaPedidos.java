package com.gerenciador.sistema_loja.ui;

import com.gerenciador.sistema_loja.model.Pedido;
import com.gerenciador.sistema_loja.service.CarrinhoService;
import com.gerenciador.sistema_loja.service.PedidoPdfService;
import com.gerenciador.sistema_loja.service.PedidoService;
import com.gerenciador.sistema_loja.service.ProdutoService;
import com.gerenciador.sistema_loja.ui.util.BotaoFactory;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static javafx.collections.FXCollections.observableArrayList;

public class TelaPedidos {

    private StackPane rootPrincipal;
    private PedidoService pedidoService;
    private ProdutoService produtoService;
    private CarrinhoService carrinhoService;
    private PedidoPdfService pedidoPdfService;

    private String ordenacaoAtual = "Mais recente";
    private LocalDate filtroDataInicio = null;
    private LocalDate filtroDataFim = null;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FMT_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public TelaPedidos(StackPane rootPrincipal, ProdutoService produtoService,
                       CarrinhoService carrinhoService, PedidoService pedidoService, PedidoPdfService pedidoPdfService) {
        this.rootPrincipal = rootPrincipal;
        this.produtoService = produtoService;
        this.carrinhoService = carrinhoService;
        this.pedidoService = pedidoService;
        this.pedidoPdfService = pedidoPdfService;
    }

    public Parent criarTela() {

        VBox root = new VBox(15);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #ffe4ec;");

        // ── LOGO ──────────────────────────────────────────
        ImageView logo = new ImageView(new Image("/logo.png"));
        logo.setFitHeight(150);
        logo.setPreserveRatio(true);

        HBox logoBox = new HBox(logo);
        logoBox.setAlignment(Pos.CENTER_LEFT);
        logoBox.setStyle("-fx-cursor: hand;");
        logoBox.setOnMouseClicked(e -> voltar());

        // ── VOLTAR ────────────────────────────────────────
        Button btnVoltar = BotaoFactory.secundario("← Voltar");
        btnVoltar.setOnAction(e -> voltar());

        // ── TABELA ────────────────────────────────────────
        TableView<Pedido> tabela = new TableView<>();
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabela.setPrefHeight(500);

        // ── BUSCA ─────────────────────────────────────────
        TextField campoBusca = new TextField();
        campoBusca.setPromptText("Buscar pelo nome do cliente...");
        campoBusca.setFocusTraversable(false);
        campoBusca.setStyle("""
            -fx-background-radius: 8;
            -fx-border-radius: 8;
            -fx-border-color: #ffd1dc;
            -fx-padding: 8 12;
            -fx-font-size: 13px;
        """);
        campoBusca.textProperty().addListener((obs, o, n) -> atualizarTabela(tabela, n));

        // ── ORDENAÇÃO ─────────────────────────────────────
        ComboBox<String> cmbOrdenacao = new ComboBox<>();
        cmbOrdenacao.getItems().addAll("Mais recente", "Mais antigo", "Maior valor");
        cmbOrdenacao.setValue(ordenacaoAtual);
        cmbOrdenacao.setStyle("""
            -fx-background-color: #ffccd5;
            -fx-background-radius: 20;
            -fx-padding: 5 15;
            -fx-font-weight: bold;
        """);
        cmbOrdenacao.setOnAction(e -> {
            ordenacaoAtual = cmbOrdenacao.getValue();
            atualizarTabela(tabela, campoBusca.getText());
        });

        // ── BOTÃO PERÍODO ──────────────────────────────────
        Button btnPeriodo = BotaoFactory.secundario("📅 Período");
        Label lblPeriodoAtivo = new Label("");
        lblPeriodoAtivo.setStyle("-fx-font-size: 11px; -fx-text-fill: #ff4d6d;");
        lblPeriodoAtivo.managedProperty().bind(lblPeriodoAtivo.visibleProperty());
        lblPeriodoAtivo.setVisible(false);

        btnPeriodo.setOnAction(e ->
                mostrarPopupPeriodo(tabela, campoBusca, btnPeriodo, lblPeriodoAtivo)
        );

        HBox filtros = new HBox(10, cmbOrdenacao, btnPeriodo, lblPeriodoAtivo);
        filtros.setAlignment(Pos.CENTER_LEFT);

        // ── COLUNAS ───────────────────────────────────────
        TableColumn<Pedido, String> colNome = new TableColumn<>("Cliente");
        colNome.setReorderable(false);
        colNome.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getNomeCliente())
        );

        TableColumn<Pedido, String> colData = new TableColumn<>("Data");
        colData.setReorderable(false);
        colData.setCellValueFactory(c -> {
            LocalDateTime d = c.getValue().getData();
            String txt = d != null ? d.format(FMT) : "-";
            return new SimpleStringProperty(txt);
        });

        TableColumn<Pedido, String> colTotal = new TableColumn<>("Total");
        colTotal.setReorderable(false);
        colTotal.setCellValueFactory(c -> {
            BigDecimal t = c.getValue().getTotal();
            String txt = t != null ? "R$ " + t.setScale(2, RoundingMode.HALF_UP) : "-";
            return new SimpleStringProperty(txt);
        });

        TableColumn<Pedido, Void> colAcoes = new TableColumn<>("Ações");
        colAcoes.setReorderable(false);
        colAcoes.setCellFactory(param -> new TableCell<>() {

            private final Button btnGerarPdf = BotaoFactory.secundario("Gerar PDF");
            private final Button btnEditar   = BotaoFactory.primario("Editar");
            private final Button btnExcluir  = BotaoFactory.secundario("Excluir");
            private final HBox box = new HBox(10, btnGerarPdf, btnEditar, btnExcluir);

            {
                box.setAlignment(Pos.CENTER);

                btnEditar.setOnAction(e -> {
                    Pedido p = getTableRow().getItem();
                    if (p != null) {
                        Pedido pedidoCompleto = pedidoService.buscarComItens(p.getId()); // 👈
                        TelaEditarPedido tela = new TelaEditarPedido(
                                                        rootPrincipal, produtoService, carrinhoService, pedidoService, pedidoCompleto, pedidoPdfService);
                        rootPrincipal.getChildren().setAll(tela.criarTela());
                    }
                });

                btnExcluir.setOnAction(e -> {
                    Pedido p = getTableRow().getItem();
                    if (p != null) {
                        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                        confirm.setTitle("Excluir pedido");
                        confirm.setHeaderText("Tem certeza que deseja excluir o pedido de \"" + p.getNomeCliente() + "\"?");
                        confirm.setContentText("Esta ação não pode ser desfeita.");
                        confirm.showAndWait().ifPresent(resp -> {
                            if (resp == ButtonType.OK) {
                                pedidoService.deletar(p.getId());
                                atualizarTabela(getTableView(), campoBusca.getText());
                            }
                        });
                    }
                });

                btnGerarPdf.setOnAction(e -> {
                    Pedido p = getTableRow().getItem();
                    if (p != null) {
                        Pedido pedidoCompleto = pedidoService.buscarComItens(p.getId());
                        mostrarPopupSalvarPdf(pedidoCompleto);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || getTableRow().getItem() == null ? null : box);
            }
        });

        tabela.getColumns().addAll(colNome, colData, colTotal, colAcoes);

        // ── ESTILO HEADER ─────────────────────────────────
        Platform.runLater(() -> {
            Pane header = (Pane) tabela.lookup("TableHeaderRow");
            if (header != null) header.setStyle("-fx-background-color: #ffccd5; -fx-border-color: transparent;");

            for (Node n : tabela.lookupAll(".column-header-background"))
                n.setStyle("-fx-background-color: #ffccd5; -fx-border-color: transparent;");

            for (Node n : tabela.lookupAll(".column-header")) {
                n.setStyle("-fx-background-color: #ffccd5; -fx-border-width: 0; -fx-font-weight: bold; -fx-alignment: CENTER;");
                n.setOnMouseEntered(ev -> n.setStyle("-fx-background-color: #ffb3c1; -fx-border-width: 0; -fx-font-weight: bold; -fx-alignment: CENTER;"));
                n.setOnMouseExited(ev  -> n.setStyle("-fx-background-color: #ffccd5; -fx-border-width: 0; -fx-font-weight: bold; -fx-alignment: CENTER;"));
            }

            for (Node n : tabela.lookupAll(".filler"))
                n.setStyle("-fx-background-color: #ffccd5;");
        });

        atualizarTabela(tabela, "");

        root.getChildren().addAll(logoBox, btnVoltar, campoBusca, filtros, tabela);
        return root;
    }

    // ── POPUP PERÍODO ─────────────────────────────────────
    private void mostrarPopupPeriodo(TableView<Pedido> tabela, TextField campoBusca, Button btnPeriodo, Label lblPeriodoAtivo) {

        VBox popup = new VBox(12);
        popup.setPadding(new Insets(20));
        popup.setMaxWidth(340);
        popup.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 15;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 20, 0, 0, 6);
        """);
        popup.setMaxHeight(Region.USE_PREF_SIZE);
        popup.setPrefHeight(Region.USE_COMPUTED_SIZE);

        Label titulo = new Label("Filtrar por período");
        titulo.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        // ── DATA INÍCIO ───────────────────────────────────
        Label lblInicio = new Label("Data início");
        lblInicio.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");

        DatePicker dpInicio = new DatePicker(filtroDataInicio);
        dpInicio.setPromptText("dd/mm/aaaa");
        dpInicio.setMaxWidth(Double.MAX_VALUE);
        dpInicio.setStyle("""
            -fx-background-radius: 8;
            -fx-border-radius: 8;
            -fx-border-color: #ffd1dc;
            -fx-font-size: 13px;
        """);
        dpInicio.setConverter(new StringConverter<>() {
            final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            @Override public String toString(LocalDate d) { return d != null ? fmt.format(d) : ""; }
            @Override public LocalDate fromString(String s) {
                try { return s != null && !s.isBlank() ? LocalDate.parse(s, fmt) : null; }
                catch (Exception ex) { return null; }
            }
        });

        // ── DATA FIM ──────────────────────────────────────
        Label lblFim = new Label("Data fim");
        lblFim.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");

        DatePicker dpFim = new DatePicker(filtroDataFim);
        dpFim.setPromptText("dd/mm/aaaa");
        dpFim.setMaxWidth(Double.MAX_VALUE);
        dpFim.setStyle(dpInicio.getStyle());
        dpFim.setConverter(dpInicio.getConverter());

        // ── BOTÕES ────────────────────────────────────────
        Button btnAplicar = BotaoFactory.primario("Aplicar");
        Button btnLimpar  = BotaoFactory.secundario("Limpar filtro");

        HBox botoes = new HBox(10, btnLimpar, btnAplicar);
        botoes.setAlignment(Pos.CENTER_RIGHT);

        popup.getChildren().addAll(titulo,
                new VBox(4, lblInicio, dpInicio),
                new VBox(4, lblFim, dpFim),
                botoes);

        StackPane overlay = new StackPane(popup);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.4);");
        overlay.setAlignment(Pos.CENTER);
        overlay.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
        overlay.setFocusTraversable(false);

        rootPrincipal.getChildren().add(overlay);

        ScaleTransition anim = new ScaleTransition(Duration.millis(200), popup);
        popup.setScaleX(0); popup.setScaleY(0);
        anim.setToX(1); anim.setToY(1); anim.play();

        btnAplicar.setOnAction(ev -> {
            filtroDataInicio = dpInicio.getValue();
            filtroDataFim    = dpFim.getValue();

            if (filtroDataInicio != null || filtroDataFim != null) {
                String de  = filtroDataInicio != null ? filtroDataInicio.format(FMT_DATA) : "início";
                String ate = filtroDataFim    != null ? filtroDataFim.format(FMT_DATA)    : "hoje";
                lblPeriodoAtivo.setText("(" + de + " → " + ate + ")");
                lblPeriodoAtivo.setVisible(true);
            } else {
                lblPeriodoAtivo.setVisible(false);
            }

            rootPrincipal.getChildren().remove(overlay);
            atualizarTabela(tabela, campoBusca.getText());
        });

        btnLimpar.setOnAction(ev -> {
            filtroDataInicio = null;
            filtroDataFim    = null;
            lblPeriodoAtivo.setVisible(false);
            rootPrincipal.getChildren().remove(overlay);
            atualizarTabela(tabela, campoBusca.getText());
        });

        overlay.setOnMouseClicked(ev -> {
            if (ev.getTarget() == overlay)
                rootPrincipal.getChildren().remove(overlay);
        });
    }

    // ── ATUALIZAR TABELA ──────────────────────────────────
    private void atualizarTabela(TableView<Pedido> tabela, String busca) {

        List<Pedido> pedidos = pedidoService.listarOrdenado(true);

        // busca por nome
        if (busca != null && !busca.isBlank()) {
            String b = busca.toLowerCase();
            pedidos = pedidos.stream()
                    .filter(p -> p.getNomeCliente() != null &&
                            p.getNomeCliente().toLowerCase().contains(b))
                    .collect(Collectors.toList());
        }

        // filtro de período
        if (filtroDataInicio != null) {
            LocalDateTime inicio = filtroDataInicio.atStartOfDay();
            pedidos = pedidos.stream()
                    .filter(p -> p.getData() != null && !p.getData().isBefore(inicio))
                    .collect(Collectors.toList());
        }
        if (filtroDataFim != null) {
            LocalDateTime fim = filtroDataFim.plusDays(1).atStartOfDay();
            pedidos = pedidos.stream()
                    .filter(p -> p.getData() != null && p.getData().isBefore(fim))
                    .collect(Collectors.toList());
        }

        // ordenação
        pedidos = switch (ordenacaoAtual) {
            case "Mais antigo" -> pedidos.stream()
                    .sorted((a, b) -> {
                        if (a.getData() == null) return 1;
                        if (b.getData() == null) return -1;
                        return a.getData().compareTo(b.getData());
                    }).collect(Collectors.toList());
            case "Maior valor" -> pedidos.stream()
                    .sorted((a, b) -> {
                        if (a.getTotal() == null) return 1;
                        if (b.getTotal() == null) return -1;
                        return b.getTotal().compareTo(a.getTotal());
                    }).collect(Collectors.toList());
            default -> pedidos.stream()
                    .sorted((a, b) -> {
                        if (a.getData() == null) return 1;
                        if (b.getData() == null) return -1;
                        return b.getData().compareTo(a.getData());
                    }).collect(Collectors.toList());
        };

        tabela.setItems(observableArrayList(pedidos));
    }

    private void voltar() {
        TelaPrincipal tela = new TelaPrincipal(rootPrincipal, produtoService, carrinhoService, pedidoService, pedidoPdfService);
        rootPrincipal.getChildren().setAll(tela.criarTela());
    }

    private void mostrarPopupSalvarPdf(Pedido pedido) {

        VBox popup = new VBox(12);
        popup.setPadding(new Insets(24));
        popup.setMaxWidth(380);
        popup.setMaxHeight(Region.USE_PREF_SIZE);
        popup.setPrefHeight(Region.USE_COMPUTED_SIZE);
        popup.setStyle("""
        -fx-background-color: white;
        -fx-background-radius: 15;
        -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 20, 0, 0, 6);
    """);

        Label titulo = new Label("Gerar PDF");
        titulo.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        Label subtitulo = new Label("Selecione o documento que deseja gerar:");
        subtitulo.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");

        Button btnPedido  = BotaoFactory.primario("Pedido (A4)");
        Button btnComanda = BotaoFactory.primario("Comanda");
        Button btnAmbos   = BotaoFactory.secundario("Pedido e Comanda");
        Button btnNenhum  = BotaoFactory.secundario("Nenhum");

        btnPedido.setMaxWidth(Double.MAX_VALUE);
        btnComanda.setMaxWidth(Double.MAX_VALUE);
        btnAmbos.setMaxWidth(Double.MAX_VALUE);
        btnNenhum.setMaxWidth(Double.MAX_VALUE);

        popup.getChildren().addAll(titulo, subtitulo, btnPedido, btnComanda, btnAmbos, btnNenhum);

        StackPane overlay = new StackPane(popup);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.4); -fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
        overlay.setAlignment(Pos.CENTER);
        overlay.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
        overlay.setFocusTraversable(false);

        rootPrincipal.getChildren().add(overlay);

        popup.setScaleX(0);
        popup.setScaleY(0);
        ScaleTransition anim = new ScaleTransition(Duration.millis(200), popup);
        anim.setToX(1);
        anim.setToY(1);
        anim.play();

        String nomeBase = pedido.getNomeCliente().toUpperCase();

        btnPedido.setOnAction(ev -> {
            rootPrincipal.getChildren().remove(overlay);
            salvarArquivo(pedido, nomeBase + " - PEDIDO", false);
        });

        btnComanda.setOnAction(ev -> {
            rootPrincipal.getChildren().remove(overlay);
            salvarArquivo(pedido, nomeBase + " - COMANDA", true);
        });

        btnAmbos.setOnAction(ev -> {
            rootPrincipal.getChildren().remove(overlay);
            salvarArquivo(pedido, nomeBase + " - PEDIDO", false);
            salvarArquivo(pedido, nomeBase + " - COMANDA", true);
        });

        btnNenhum.setOnAction(ev -> rootPrincipal.getChildren().remove(overlay));
    }

    private void salvarArquivo(Pedido pedido, String nomeArquivo, boolean comanda) {

        java.util.prefs.Preferences prefs =
                java.util.prefs.Preferences.userNodeForPackage(PedidoPdfService.class);

        javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
        chooser.setTitle("Salvar " + (comanda ? "Comanda" : "Pedido"));
        chooser.setInitialFileName(nomeArquivo + ".pdf");
        chooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("PDF", "*.pdf"));

        String ultimoDir = prefs.get("ultimo_diretorio_pdf", null);
        if (ultimoDir != null) {
            java.io.File dirAnterior = new java.io.File(ultimoDir);
            if (dirAnterior.exists() && dirAnterior.isDirectory()) {
                chooser.setInitialDirectory(dirAnterior);
            }
        }

        javafx.stage.Window window = rootPrincipal.getScene().getWindow();
        java.io.File destino = chooser.showSaveDialog(window);

        if (destino != null) {
            prefs.put("ultimo_diretorio_pdf", destino.getParent());

            try {
                if (comanda) {
                    pedidoPdfService.gerarComanda(pedido, destino);
                } else {
                    pedidoPdfService.gerarPedidoA4(pedido, destino);
                }

                Alert ok = new Alert(Alert.AlertType.INFORMATION);
                ok.setTitle("Salvo");
                ok.setHeaderText(null);
                ok.setContentText("✅ Arquivo salvo em:\n" + destino.getAbsolutePath());
                ok.showAndWait();

            } catch (Exception ex) {
                Alert erro = new Alert(Alert.AlertType.ERROR);
                erro.setTitle("Erro ao salvar PDF");
                erro.setHeaderText("Não foi possível gerar o arquivo.");
                erro.setContentText(ex.getMessage());
                erro.showAndWait();
            }
        }
    }
}