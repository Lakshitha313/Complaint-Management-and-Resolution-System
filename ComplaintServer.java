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

public class ComplaintServer {

    public static void main(String[] args) throws Exception {

        HttpServer server = HttpServer.create(
                new InetSocketAddress(8082), 0
        );

        server.createContext("/complaint", (HttpExchange exchange) -> {

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {

                InputStream input = exchange.getRequestBody();

                String data = new String(
                        input.readAllBytes(),
                        StandardCharsets.UTF_8
                );

                Map<String, String> formData = parseFormData(data);

                String userId = formData.get("userId");
                String title = formData.get("title");
                String category = formData.get("category");
                String description = formData.get("description");
                String location = formData.get("location");

                try {

                    Connection con = DBConnection.getConnection();

                    String sql = "INSERT INTO complaints " +
                            "(user_id, title, category, description, location) " +
                            "VALUES (?, ?, ?, ?, ?)";

                    PreparedStatement ps =
                            con.prepareStatement(sql);

                    ps.setInt(1, Integer.parseInt(userId));
                    ps.setString(2, title);
                    ps.setString(3, category);
                    ps.setString(4, description);
                    ps.setString(5, location);

                    ps.executeUpdate();

                    ps.close();
                    con.close();

                    sendResponse(
                            exchange,
                            "Complaint submitted successfully!"
                    );

                    System.out.println(
                            "Complaint submitted by user ID: " + userId
                    );

                } catch (Exception e) {

                    e.printStackTrace();

                    sendResponse(
                            exchange,
                            "Complaint submission failed!"
                    );
                }

            } else {

                sendResponse(
                        exchange,
                        "Invalid request!"
                );
            }
        });

        server.start();

        System.out.println(
                "Complaint server started at http://localhost:8082"
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