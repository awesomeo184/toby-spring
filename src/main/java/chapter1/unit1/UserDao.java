package chapter1.unit1;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDao {

    public void add(User user) throws SQLException {
        Connection con = DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/spring", "root", "2495"
        );

        PreparedStatement ps = con
            .prepareStatement("insert into users (id, name, password) values (?,?,?)");

        ps.setString(1, user.getId());
        ps.setString(2, user.getName());
        ps.setString(3, user.getPassword());

        ps.executeUpdate();

        ps.close();
        con.close();
    }

    public User get(String id) throws SQLException {
        Connection con = DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/spring", "root", "2495"
        );

        PreparedStatement ps = con
            .prepareStatement("select * from users where id = ?");

        ps.setString(1, id);

        ResultSet rs = ps.executeQuery();
        rs.next();
        User user = new User();
        user.setId(rs.getString("id"));
        user.setName(rs.getString("name"));
        user.setPassword(rs.getString("password"));

        rs.close();
        ps.close();
        con.close();

        return user;
    }

}
