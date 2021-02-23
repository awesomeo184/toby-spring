package chapter1.unit4;

import java.sql.Connection;
import java.sql.SQLException;

public interface SimpleConnectionMaker {
    Connection makeConnection() throws SQLException;
}
