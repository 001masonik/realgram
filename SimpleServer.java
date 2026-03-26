package com.example.realgram;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class SimpleServer {
    private static Set<PrintWriter> clientWriters = new HashSet<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(8888)) {
            String ip = InetAddress.getLocalHost().getHostAddress();

            System.out.println("🚀 Сервер Realgram запущено!");
            System.out.println("📍 IP для підключення: " + ip);
            System.out.println("🔌 Порт: 8888");
            System.out.println("--------------------------------8<--------------------------------");

            while (true) {
                new ClientHandler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            System.err.println("Помилка сервера: " + e.getMessage());
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

                synchronized (clientWriters) { clientWriters.add(out); }

                String message;
                while ((message = in.readLine()) != null) {
                    broadcast(message, out);
                }
            } catch (IOException e) {
                System.out.println("Користувач відключився.");
            } finally {
                if (out != null) {
                    synchronized (clientWriters) { clientWriters.remove(out); }
                }
            }
        }

        private void broadcast(String message, PrintWriter senderOut) {
            synchronized (clientWriters) {
                for (PrintWriter writer : clientWriters) {
                    if (writer != senderOut) {
                        writer.println(message);
                    }
                }
            }
        }
    }
}