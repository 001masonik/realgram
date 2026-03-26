package com.example.realgram;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class HelloController {
    @FXML private TextArea logArea;
    @FXML private TextField messageField;
    @FXML private TextField ipField;
    @FXML private TextField portField;
    @FXML private TextField usernameField;
    @FXML private Button sendButton;
    @FXML private Label statusLabel;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    @FXML
    public void initialize() {
        sendButton.disableProperty().bind(messageField.textProperty().isEmpty());
    }

    @FXML
    protected void onKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER && !messageField.getText().isEmpty()) {
            onSendButtonClick();
        }
    }

    @FXML
    protected void onConnectButtonClick() {
        String ip = ipField.getText().isEmpty() ? "localhost" : ipField.getText();
        int port;
        try {
            port = portField.getText().isEmpty() ? 8888 : Integer.parseInt(portField.getText());
        } catch (NumberFormatException e) {
            updateLog("❌ Помилка: Порт має бути числом!");
            return;
        }

        new Thread(() -> {
            try {
                updateLog("🔌 Підключення до " + ip + "...");
                socket = new Socket(ip, port);

                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

                updateStatus("Online", "#4caf50");
                updateLog("✅ Успішно! Ви увійшли як " + getUsername());

                String serverMessage;
                while ((serverMessage = in.readLine()) != null) {
                    updateLog(serverMessage); // Отримуємо повідомлення від інших
                }
            } catch (IOException e) {
                updateStatus("Disconnected", "#f44336");
                updateLog("❌ Помилка зв'язку: " + e.getMessage());
            }
        }).start();
    }

    @FXML
    protected void onSendButtonClick() {
        if (out != null) {
            String msg = messageField.getText();
            String user = getUsername();

            out.println(user + ": " + msg);

            updateLog("Ви: " + msg);

            messageField.clear();
        } else {
            updateLog("⚠️ Спочатку підключіться (Connect)!");
        }
    }

    private String getUsername() {
        return usernameField.getText().isEmpty() ? "User" : usernameField.getText();
    }

    private void updateLog(String text) {
        Platform.runLater(() -> {
            logArea.appendText(text + "\n");
            logArea.setScrollTop(Double.MAX_VALUE); // Авто-скрол вниз
        });
    }

    private void updateStatus(String text, String color) {
        Platform.runLater(() -> {
            statusLabel.setText(text);
            statusLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
        });
    }
}