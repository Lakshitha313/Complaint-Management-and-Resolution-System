import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.sql.*;

public class AdminServer {

    public static void main(String[] args) throws Exception {

        HttpServer server = HttpServer.create(
                new InetSocketAddress(8084), 0
        );

        server.createContext(
                "/admincomplaints",
                AdminServer::getComplaints
        );

        server.createContext(
                "/updatestatus",
                AdminServer::updateStatus
        );

        server.setExecutor(null);

        System.out.println(
                "Admin server started at http://localhost:8084"
        );

        server.start();
    }


    // ==============================
    // GET ALL COMPLAINTS
    // ==============================

    static void getComplaints(HttpExchange exchange)
            throws IOException {

        StringBuilder response = new StringBuilder();

        try {

            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection con = DriverManager.getConnection(
        "jdbc:mysql://localhost:3306/complaint_management",
        System.getenv("DB_USERNAME"),
        System.getenv("DB_PASSWORD")
);

            String sql =
                    "SELECT c.id, u.name, c.title, c.category, " +
                    "c.description, c.location, c.status, c.created_at " +
                    "FROM complaints c " +
                    "JOIN users u ON c.user_id = u.id " +
                    "ORDER BY c.id DESC";

            PreparedStatement ps =
                    con.prepareStatement(sql);

            ResultSet rs =
                    ps.executeQuery();

            while (rs.next()) {

                response.append(
                        rs.getInt("id")
                )
                .append("|")

                .append(
                        rs.getString("name")
                )
                .append("|")

                .append(
                        rs.getString("title")
                )
                .append("|")

                .append(
                        rs.getString("category")
                )
                .append("|")

                .append(
                        rs.getString("description")
                )
                .append("|")

                .append(
                        rs.getString("location")
                )
                .append("|")

                .append(
                        rs.getString("status")
                )
                .append("|")

                .append(
                        rs.getTimestamp("created_at")
                )
                .append("\n");
            }

            rs.close();
            ps.close();
            con.close();

        }
        catch (Exception e) {

            e.printStackTrace();

            response.append("ERROR");
        }


        exchange.getResponseHeaders().add(
                "Access-Control-Allow-Origin",
                "*"
        );

        byte[] bytes =
                response.toString().getBytes();

        exchange.sendResponseHeaders(
                200,
                bytes.length
        );

        OutputStream os =
                exchange.getResponseBody();

        os.write(bytes);

        os.close();
    }


    // ==============================
    // UPDATE COMPLAINT STATUS
    // ==============================

    static void updateStatus(HttpExchange exchange)
            throws IOException {

        StringBuilder requestBody =
                new StringBuilder();

        try {

            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(
                                    exchange.getRequestBody()
                            )
                    );

            String line;

            while ((line = reader.readLine()) != null) {

                requestBody.append(line);
            }


            String body =
                    requestBody.toString();


            String[] parts =
                    body.split("&");


            int complaintId = 0;

            String status = "";


            for (String part : parts) {

                String[] pair =
                        part.split("=");

                if (pair.length < 2) {

                    continue;
                }


                if (pair[0].equals("id")) {

                    complaintId =
                            Integer.parseInt(
                                    pair[1]
                            );
                }


                if (pair[0].equals("status")) {

                    status =
                            java.net.URLDecoder.decode(
                                    pair[1],
                                    "UTF-8"
                            );
                }
            }


            // MySQL connection

            Class.forName(
                    "com.mysql.cj.jdbc.Driver"
            );


            Connection con =
        DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/complaint_management",
                System.getenv("DB_USERNAME"),
                System.getenv("DB_PASSWORD")
        );

            String sql =
                    "UPDATE complaints " +
                    "SET status = ? " +
                    "WHERE id = ?";


            PreparedStatement ps =
                    con.prepareStatement(sql);


            ps.setString(
                    1,
                    status
            );


            ps.setInt(
                    2,
                    complaintId
            );


            ps.executeUpdate();


            ps.close();

            con.close();


            // Send success response

            String response =
                    "SUCCESS";


            exchange.getResponseHeaders().add(
                    "Access-Control-Allow-Origin",
                    "*"
            );


            byte[] bytes =
                    response.getBytes();


            exchange.sendResponseHeaders(
                    200,
                    bytes.length
            );


            OutputStream os =
                    exchange.getResponseBody();


            os.write(bytes);

            os.close();

        }
        catch (Exception e) {

            e.printStackTrace();


            String response =
                    "ERROR";


            byte[] bytes =
                    response.getBytes();


            exchange.sendResponseHeaders(
                    500,
                    bytes.length
            );


            OutputStream os =
                    exchange.getResponseBody();


            os.write(bytes);

            os.close();
        }
    }
}