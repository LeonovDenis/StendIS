package ru.pelengator.dao;

import java.sql.*;
import java.util.Arrays;

public class BDreadData {
    static final String JDBC_DRIVER = "org.mariadb.jdbc.Driver";
    static final String DB_URL = "jdbc:mariadb://127.0.0.1/data";

    //  Database credentials
    static final String USER = "root";
    static final String PASS = "root";

    public void readDataBD(long expID) {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs= null;
        try {
            //STEP 2: Register JDBC driver
            Class.forName(JDBC_DRIVER);//.getDeclaredConstructor().newInstance();

            //STEP 3: Open a connection
            System.out.println("Connecting to a selected database...");
            conn = DriverManager.getConnection(
                    DB_URL, USER, PASS);
            System.out.println("Connected database successfully...");

            //STEP 4: Execute a query
            System.out.println("Creating table in given database...");
            stmt = conn.createStatement();

            String sql ="select * from exp";

            // executing SELECT query
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
               String count = rs.getString(2);
                System.out.println("Total number of books in the table : " + (count));
            }


            //  stmt.executeUpdate(sql);
            System.out.println("Created table in given database...");
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        } finally {
            //finally block used to close resources
            try {
                if (stmt != null) {
                    conn.close();
                }


            } catch (SQLException se) {
            }// do nothing
            try {
                if (conn != null) {
                    conn.close();
                }


            } catch (SQLException se) {
                se.printStackTrace();
            }//end finally try
        }//end try
        System.out.println("Goodbye!");
    }//end main
}//end JDBCExample

