import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;

public class RegisterServer {

    public static void main(String[] args) throws Exception {

        HttpServer server = HttpServer.create(
            new InetSocketAddress(8080), 0
        );

        server.createContext("/register", (HttpExchange exchange) -> {

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {

                InputStream input = exchange.getRequestBody();

                String data = new String(
                    input.readAllBytes(),
                    StandardCharsets.UTF_8
                );

                Map<String, String> formData = parseFormData(data);

                String name = formData.get("name");
                String email = formData.get("email");
                String phone = formData.get("phone");
                String password = formData.get("password");

                try {

                    Connection con = DBConnection.getConnection();

                    String sql = "INSERT INTO users " +
                            "(name, email, phone, password) " +
                            "VALUES (?, ?, ?, ?)";

                    PreparedStatement ps =
                            con.prepareStatement(sql);

                    ps.setString(1, name);
                    ps.setString(2, email);
                    ps.setString(3, phone);
                    ps.setString(4, password);

                    ps.executeUpdate();

                    ps.close();
                    con.close();

                    sendResponse(exchange,
                            "Registration successful!");

                    System.out.println(
                            "New user registered: " + email
                    );

                } catch (Exception e) {

                    e.printStackTrace();

                    sendResponse(exchange,
                            "Registration failed!");
                }

            } else {

                sendResponse(exchange,
                        "Invalid request!");
            }
        });

        server.start();

        System.out.println(
                "Server started at http://localhost:8080"
        );
    }


    public static Map<String, String> parseFormData(String data) {

        Map<String, String> map = new HashMap<>();

        String[] pairs = data.split("&");

        for (String pair : pairs) {

            String[] keyValue = pair.split("=", 2);

            if (keyValue.length == 2) {

                String key = URLDecoder.decode(
                        keyValue[0],
                        StandardCharsets.UTF_8
                );

                String value = URLDecoder.decode(
                        keyValue[1],
                        StandardCharsets.UTF_8
                );

                map.put(key, value);
            }
        }

        return map;
    }


    public static void sendResponse(
            HttpExchange exchange,
            String response
    ) throws IOException {

        exchange.getResponseHeaders().add(
                "Access-Control-Allow-Origin", "*"
        );

        exchange.sendResponseHeaders(
                200,
                response.getBytes().length
        );

        OutputStream output =
                exchange.getResponseBody();

        output.write(response.getBytes());

        output.close();
    }
}