import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    public static Connection getConnection() {

        String url = "jdbc:mysql://localhost:3306/complaint_management";
        String username = System.getenv("DB_USERNAME");
        String password = System.getenv("DB_PASSWORD");

        try {

            Connection con = DriverManager.getConnection(
                url,
                username,
                password
            );

            System.out.println("Database connected successfully!");

            return con;

        } catch (SQLException e) {

            System.out.println("Database connection failed!");
            e.printStackTrace();

            return null;
        }
    }

    public static void main(String[] args) {

        getConnection();

    }
}