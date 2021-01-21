package com.example.tobyspring.user.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class NUserDao extends UserDao{

    @Override
    public Connection getConnection() throws SQLException, ClassNotFoundException {

        // N사 DB connection 생성 코드
        Class.forName("com.mysql.cj.jdbc.Driver");

        return DriverManager
            .getConnection("jdbc:mysql://localhost/spring?serverTimezone=UTC&useSSL=false", "root",
                "2495");
    }

}
