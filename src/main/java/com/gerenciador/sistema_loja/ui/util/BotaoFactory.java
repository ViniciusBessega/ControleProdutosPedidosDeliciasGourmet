package com.gerenciador.sistema_loja.ui.util;

import javafx.scene.control.Button;
import javafx.scene.Cursor;

public class BotaoFactory {

    public static Button primario(String texto) {
        Button btn = new Button(texto);

        btn.setCursor(Cursor.HAND);

        btn.setStyle("""
            -fx-background-color: #ff4d6d;
            -fx-text-fill: white;
            -fx-background-radius: 25;
            -fx-padding: 12 25;
            -fx-font-weight: bold;
        """);

        btn.setOnMouseEntered(e -> btn.setStyle("""
            -fx-background-color: #ff3355;
            -fx-text-fill: white;
            -fx-background-radius: 25;
            -fx-padding: 12 25;
            -fx-font-weight: bold;
        """));

        btn.setOnMouseExited(e -> btn.setStyle("""
            -fx-background-color: #ff4d6d;
            -fx-text-fill: white;
            -fx-background-radius: 25;
            -fx-padding: 12 25;
            -fx-font-weight: bold;
        """));

        return btn;
    }

    public static Button secundario(String texto) {
        Button btn = new Button(texto);

        btn.setCursor(Cursor.HAND);

        btn.setStyle("""
            -fx-background-color: #6c757d;
            -fx-text-fill: white;
            -fx-background-radius: 20;
            -fx-padding: 10 20;
            -fx-font-weight: bold;
        """);

        btn.setOnMouseEntered(e -> btn.setStyle("""
            -fx-background-color: #5a6268;
            -fx-text-fill: white;
            -fx-background-radius: 20;
            -fx-padding: 10 20;
            -fx-font-weight: bold;
        """));

        btn.setOnMouseExited(e -> btn.setStyle("""
            -fx-background-color: #6c757d;
            -fx-text-fill: white;
            -fx-background-radius: 20;
            -fx-padding: 10 20;
            -fx-font-weight: bold;
        """));

        return btn;
    }
}