package com.gerenciador.sistema_loja;

import com.gerenciador.sistema_loja.service.CarrinhoService;
import com.gerenciador.sistema_loja.service.ProdutoService;
import com.gerenciador.sistema_loja.ui.TelaPrincipal;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

public class MainApp extends Application {

    private ConfigurableApplicationContext context;

    @Override
    public void init() {
        System.out.println("Iniciando Spring...");
        context = SpringApplication.run(SistemaLojaApplication.class);
        System.out.println("Spring iniciado!");
    }

    @Override
    public void start(Stage stage) {
        System.out.println("Abrindo JavaFX...");

        StackPane root = new StackPane();

        ProdutoService produtoService = context.getBean(ProdutoService.class);
        CarrinhoService carrinhoService = context.getBean(CarrinhoService.class);

        TelaPrincipal tela = new TelaPrincipal(root, produtoService, carrinhoService);

        root.getChildren().setAll(tela.criarTela());

        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.setTitle("Sistema Loja");
        stage.setMaximized(true);
        stage.show();
    }

    @Override
    public void stop() {
        System.out.println("Fechando Spring...");
        context.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}