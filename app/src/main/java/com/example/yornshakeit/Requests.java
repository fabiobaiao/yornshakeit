package com.example.yornshakeit;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class Requests {

    public static CompletableFuture<Response> get(String url, Map<String, Map<String, String>> args) {
        CompletableFuture<Response> cf = new CompletableFuture<>();

        new Thread(() -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) createURL(url, args.get("params")).openConnection();

                try {
                    conn.setRequestMethod("GET");
                    setup(conn, args);
                    connect(conn, args.get("data"));
                    // response type !
                    String responseText = responseText(conn);
                    System.out.println(responseText);
                    cf.complete(new Response(conn.getHeaderFields(), responseText));
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    conn.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        return cf;
    }

    public static CompletableFuture<Response> post(String url, Map<String, Map<String, String>> args, Map<String, String> data) {
        CompletableFuture<Response> cf = new CompletableFuture<>();

        new Thread(() -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) createURL(url, args.get("params")).openConnection();

                try {
                    conn.setRequestMethod("POST");
                    setup(conn, args);
                    connect(conn, data);
                    // response type !
                    String responseText = responseText(conn);
                    System.out.println(responseText);
                    cf.complete(new Response(conn.getHeaderFields(), responseText));
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    conn.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        return cf;
    }

    public static CompletableFuture<Response> post(String url, Map<String, Map<String, String>> args, JSONObject json) {
        CompletableFuture<Response> cf = new CompletableFuture<>();

        new Thread(() -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) createURL(url, args.get("params")).openConnection();

                try {
                    conn.setRequestMethod("POST");
                    setup(conn, args);
                    connect(conn, json);
                    String responseText = responseText(conn);
                    System.out.println(responseText);
                    cf.complete(new Response(conn.getHeaderFields(), responseText));
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    conn.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        return cf;
    }

    private static URL createURL(String url, Map<String, String> params) throws MalformedURLException {
        return new URL(url + (params != null ? "?" + format(params, "&") : ""));
    }

    private static String format(Map<String, String> args, String separator) {
        StringBuilder formatArgs = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : args.entrySet()) {
            if (!first) {
                formatArgs.append(separator);
            }
            first = false;
            formatArgs.append(entry.getKey());
            formatArgs.append("=");
            formatArgs.append(entry.getValue());
        }
        return formatArgs.toString();
    }

    private static void setup(HttpURLConnection conn, Map<String, Map<String, String>> args) throws IOException {
        for (Map.Entry<String, Map<String, String>> arg : args.entrySet()) {
            switch (arg.getKey()) {
                case "cookies":
                    conn.setRequestProperty("Cookie", format(arg.getValue(), "; "));
                    break;
                case "headers":
                    for (Map.Entry<String, String> header : arg.getValue().entrySet()) {
                        conn.setRequestProperty(header.getKey(), header.getValue());
                    }
                    break;
            }
        }
    }

    private static void connect(HttpURLConnection conn, Map<String, String> data) throws IOException {
        if (data != null) {
            OutputStream out = conn.getOutputStream();
            out.write(format(data, "&").getBytes());
        }
        else {
            conn.connect();
        }
    }

    private static void connect(HttpURLConnection conn, JSONObject json) throws IOException {
        if (json != null) {
            OutputStream out = conn.getOutputStream();
            out.write(json.toString().getBytes());
        }
        else {
            conn.connect();
        }
    }

    private static String responseText(HttpURLConnection conn) throws IOException {
        InputStream in = conn.getInputStream();
        int len = 1024;
        byte[] inputBytes = new byte[len];
        int n = in.read(inputBytes);
        while (n == len) {
            byte[] resizedInputBytes = new byte[inputBytes.length + len];
            System.arraycopy(inputBytes, 0, resizedInputBytes, 0, inputBytes.length);
            n = in.read(resizedInputBytes, inputBytes.length, len);
            inputBytes = resizedInputBytes;
        }

        String responseText = "";
        if (inputBytes.length != len || n != -1) {
            responseText = new String(inputBytes, 0, inputBytes.length - len + n);
        }

        return responseText;
    }
}
