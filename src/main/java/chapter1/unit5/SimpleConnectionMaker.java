package chapter1.unit5;

import java.sql.Connection;
import java.sql.SQLException;

public interface SimpleConnectionMaker {
    Connection makeConnection() throws SQLException;
}
