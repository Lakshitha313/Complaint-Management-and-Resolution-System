import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class LoginServer {

    public static void main(String[] args) throws Exception {

        HttpServer server = HttpServer.create(
                new InetSocketAddress(8081), 0
        );

        server.createContext("/login", (HttpExchange exchange) -> {

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {

                InputStream input = exchange.getRequestBody();

                String data = new String(
                        input.readAllBytes(),
                        StandardCharsets.UTF_8
                );

                Map<String, String> formData = parseFormData(data);

                String email = formData.get("email");
                String password = formData.get("password");

                try {

                    Connection con = DBConnection.getConnection();

                    String sql = "SELECT id, name, role FROM users " +
                                 "WHERE email = ? AND password = ?";

                    PreparedStatement ps =
                            con.prepareStatement(sql);

                    ps.setString(1, email);
                    ps.setString(2, password);

                    ResultSet rs = ps.executeQuery();

                    if (rs.next()) {

                        int id = rs.getInt("id");
                        String name = rs.getString("name");
                        String role = rs.getString("role");

                        String response =
                                "SUCCESS|" + id + "|" + name + "|" + role;

                        sendResponse(exchange, response);

                        System.out.println(
                                "Login successful: " + email
                        );

                    } else {

                        sendResponse(
                                exchange,
                                "INVALID"
                        );

                        System.out.println(
                                "Invalid login: " + email
                        );
                    }

                    rs.close();
                    ps.close();
                    con.close();

                } catch (Exception e) {

                    e.printStackTrace();

                    sendResponse(
                            exchange,
                            "ERROR"
                    );
                }

            } else {

                sendResponse(
                        exchange,
                        "INVALID_REQUEST"
                );
            }
        });

        server.start();

        System.out.println(
                "Login server started at http://localhost:8081"
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
                "Access-Control-Allow-Origin",
                "*"
        );

        byte[] responseBytes =
                response.getBytes(StandardCharsets.UTF_8);

        exchange.sendResponseHeaders(
                200,
                responseBytes.length
        );

        OutputStream output =
                exchange.getResponseBody();

        output.write(responseBytes);

        output.close();
    }
}
