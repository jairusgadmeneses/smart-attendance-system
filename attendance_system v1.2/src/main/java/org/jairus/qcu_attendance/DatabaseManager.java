package org.jairus.qcu_attendance;

import java.sql.Connection; //highway or yung bridge para maconnect yung mga packets or known entity
import java.sql.DriverManager; //hihingi or fetch link kung san coconnect
import java.sql.Statement; // parang envelope like ano yung entity nasa loob ng table

public class DatabaseManager {

    private static final String CLOUD_URL = "";
    private static final String CLOUD_USER = "";
    private static final String CLOUD_PASS = "";
    private static final  String LOCAL_URL = "jdbc:sqlite:local_logs.db";

    //wag niyo save toh akin toh eh

    public static Connection getCloudConnection() throws Exception {
        return DriverManager.getConnection(CLOUD_URL, CLOUD_USER, CLOUD_PASS);
    }

    public static Connection getLocalConnection() throws Exception {
        return DriverManager.getConnection(LOCAL_URL);
    }

    public static void setupLocalVault() {
        try (Connection conn = getLocalConnection();
             Statement stmt = conn.createStatement()) {

            String sqlLogs = "CREATE TABLE IF NOT EXISTS local_logs (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "barcode TEXT NOT NULL, " +
                    "scan_time TEXT NOT NULL)";
            stmt.execute(sqlLogs);

            String sqlStudents = "CREATE TABLE IF NOT EXISTS local_students (" +
                    "barcode TEXT PRIMARY KEY, " +
                    "full_name TEXT NOT NULL, " +
                    "section TEXT NOT NULL, " +
                    "class_time TEXT NOT NULL, " +
                    "exit_time TEXT NOT NULL)";

            stmt.execute(sqlStudents);

            System.out.println("Local SQLite Vault is completely ready.");

        } catch (Exception e) {
            System.out.println("Local Vault Error: " + e.getMessage());
        }
    }
}