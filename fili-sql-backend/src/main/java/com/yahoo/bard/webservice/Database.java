package com.yahoo.bard.webservice;

import java.sql.*;

/**
 * Created by hinterlong on 5/30/17.
 */
public class Database {
    private static final String DATABASE_URL = "jdbc:h2:mem:test";

    public static void testDatabase() throws SQLException {
        Connection connection= DriverManager.getConnection(DATABASE_URL);
        Statement s = connection.createStatement();
        try {
            s.execute("DROP TABLE PERSON");
        } catch (SQLException sqle) {
            System.out.println("Table not found, not dropping");
        }
        s.execute("CREATE TABLE PERSON (ID INT PRIMARY KEY, FIRSTNAME VARCHAR(64))");
        s.execute("INSERT INTO PERSON (ID, FIRSTNAME) VALUES (1, 'TEST')");
        PreparedStatement ps = connection.prepareStatement("select * from PERSON");
        ResultSet r = ps.executeQuery();
        if (r.next()) {
            System.out.println("Found person: " + r.getString(2));
        }
        r.close();
        ps.close();
        s.close();
        connection.close();
    }
}

