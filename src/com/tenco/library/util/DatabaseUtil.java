package com.tenco.library.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtil {


    private static final String URL = "jdbc:mysql://localhost:3306/library";

    private static final String DB_USER = System.getenv("DB_USER");
    private static final String DB_PWD = System.getenv("DB_PASSWORD");


    // 새로운 DB 연결 객체를 반환
    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(URL, DB_USER, DB_PWD);

        System.out.println(conn.getMetaData().getDatabaseProductName());
        System.out.println(conn.getMetaData().getDatabaseProductVersion());

        return conn;
    }
}
