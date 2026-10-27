import java.sql.Connection;
import java.sql.PreparedStatement;

public class Register {

    public static void main(String[] args) {

        String name = "Test User";
        String email = "testuser@example.com";
        String phone = "9000000000";
        String password = "TestPass123";

        try {

            Connection con = DBConnection.getConnection();

            String sql = "INSERT INTO users (name, email, phone, password) VALUES (?, ?, ?, ?)";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setString(4, password);

            ps.executeUpdate();

            System.out.println("User registered successfully!");

            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }
    }
 
    
 }
