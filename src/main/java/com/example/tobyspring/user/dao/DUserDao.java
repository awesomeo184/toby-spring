package com.example.tobyspring.user.dao;

import java.sql.Connection;
import java.sql.SQLException;

public class DUserDao extends UserDao{

    @Override
    public Connection getConnection() throws SQLException, ClassNotFoundException {
        // D 사 DB connection 생성 코드
        return null;
    }
}
