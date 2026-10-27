import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class MyComplaintsServer {

    public static void main(String[] args) throws Exception {

        HttpServer server = HttpServer.create(
                new InetSocketAddress(8083), 0
        );

        server.createContext("/mycomplaints", (HttpExchange exchange) -> {

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {

                InputStream input = exchange.getRequestBody();

                String data = new String(
                        input.readAllBytes(),
                        StandardCharsets.UTF_8
                );

                Map<String, String> formData = parseFormData(data);

                String userId = formData.get("userId");

                try {

                    Connection con = DBConnection.getConnection();

                    String sql = "SELECT id, title, category, description, location, status, created_at " +
             "FROM complaints " +
             "WHERE user_id = ? " +
             "ORDER BY id DESC";

                    PreparedStatement ps = con.prepareStatement(sql);

                    ps.setInt(1, Integer.parseInt(userId));

                    ResultSet rs = ps.executeQuery();

                    StringBuilder response = new StringBuilder();

                    while (rs.next()) {

                        response.append(rs.getInt("id"))
        .append("|")
        .append(rs.getString("title"))
        .append("|")
        .append(rs.getString("category"))
        .append("|")
        .append(rs.getString("description"))
        .append("|")
        .append(rs.getString("location"))
        .append("|")
        .append(rs.getString("status"))
        .append("|")
        .append(rs.getTimestamp("created_at"))
        .append("\n");
                    }

                    rs.close();
                    ps.close();
                    con.close();

                    sendResponse(exchange, response.toString());

                } catch (Exception e) {

                    e.printStackTrace();

                    sendResponse(exchange, "ERROR");
                }

            } else {

                sendResponse(exchange, "INVALID_REQUEST");
            }
        });

        server.start();

        System.out.println(
                "My Complaints server started at http://localhost:8083"
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