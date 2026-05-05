package org.jairus.qcu_attendance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.ArrayList;

public class ScannerLogic {

    public HashMap<String, String[]> ID = new HashMap<>();
    public ArrayList<String> scannedToday = new ArrayList<>();

    public static boolean isEarlyDismissalActive = false;

    public ScannerLogic() {
        DatabaseManager.setupLocalVault();
        bootUpOffline();
    }

    public void bootUpOffline() {
        try (Connection localConn = DatabaseManager.getLocalConnection()) {
            // FIXED: Added exit_time to the local load query
            String sql = "SELECT barcode, full_name, section, class_time, exit_time FROM local_students";
            PreparedStatement stmt = localConn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while(rs.next()) {
                ID.put(rs.getString("barcode"), new String[]{
                        rs.getString("full_name"),
                        rs.getString("section"),
                        rs.getString("class_time"),
                        rs.getString("exit_time") // Added to Hashmap
                });
            }
            System.out.println("System Initialized: Local Cache Loaded. Records: " + ID.size());
        } catch(Exception e) {
            System.out.println("Database Initialization Error: " + e.getMessage());
        }
    }

    public String[] processLogicScan(String scannedBarcode) {
        LocalTime now = LocalTime.now();
        String timeStr = now.format(DateTimeFormatter.ofPattern("hh:mm a"));

        if (!ID.containsKey(scannedBarcode)) {
            return new String[]{"INVALID", "UNKNOWN CREDENTIAL", "N/A", timeStr};
        }

        String[] studentData = ID.get(scannedBarcode);
        String fullName = studentData[0];
        String block_section = studentData[1];
        String classTimeStr = studentData[2];
        String exitTimeStr = studentData[3];

        // FIXED: SECOND SCAN OF THE DAY (EXIT LOGIC)
        if (scannedToday.contains(scannedBarcode)) {
            try {
                LocalTime scheduledExit = LocalTime.parse(exitTimeStr);
                if (isEarlyDismissalActive || now.isBefore(scheduledExit)) {
                    return new String[]{"EXIT", fullName, block_section, timeStr};
                }
                return new String[]{"SUCCESS", fullName, block_section, timeStr};
            } catch (Exception e) {
                return new String[]{"EXIT", fullName, block_section, timeStr};
            }
        }

        // FIRST SCAN OF THE DAY (ENTRY LOGIC)
        scannedToday.add(scannedBarcode);
        saveToLocalVault(scannedBarcode);

        try {
            LocalTime classStartTime = LocalTime.parse(classTimeStr);
            if (now.isAfter(classStartTime.plusMinutes(15))) {
                return new String[]{"LATE", fullName, block_section, timeStr};
            }
        } catch (Exception e) {
            System.out.println("Time parse error for " + fullName + ". Defaulting to Success.");
        }

        return new String[]{"SUCCESS", fullName, block_section, timeStr};
    }

    private void saveToLocalVault(String barcode) {
        try (Connection conn = DatabaseManager.getLocalConnection()) {
            String sql = "INSERT INTO local_logs (barcode, scan_time) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, barcode);
            stmt.setString(2, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            stmt.executeUpdate();
            System.out.println("Record committed to local vault: " + barcode);
        } catch (Exception e) {
            System.out.println("Write operation failed: " + e.getMessage());
        }
    }

    public void syncOfflineLogsToCloud() {
        try (Connection localConn = DatabaseManager.getLocalConnection();
             Connection cloudConn = DatabaseManager.getCloudConnection()) {

            String getLogs = "SELECT barcode, scan_time FROM local_logs";
            PreparedStatement getStmt = localConn.prepareStatement(getLogs);
            ResultSet rs = getStmt.executeQuery();

            String cloudSql = "INSERT INTO attendance (student_barcode, time_in) VALUES (?, CAST(? AS TIMESTAMP))";
            PreparedStatement cloudStmt = cloudConn.prepareStatement(cloudSql);

            int syncCount = 0;
            while (rs.next()) {
                cloudStmt.setString(1, rs.getString("barcode"));
                cloudStmt.setString(2, rs.getString("scan_time"));
                cloudStmt.executeUpdate();
                syncCount++;
            }

            System.out.println("Synchronization complete. Records pushed: " + syncCount);

            scannedToday.clear();
            System.out.println("Session memory cleared.");

            try (Statement nukeStmt = localConn.createStatement()) {
                nukeStmt.execute("DELETE FROM local_logs");
                System.out.println("Local cache purged.");
            }

            System.out.println("Downloading updated master list...");
            downloadMasterListFromCloud(localConn, cloudConn);

        } catch (Exception e) {
            System.out.println("Synchronization failed: " + e.getMessage());
        }
    }

    private void downloadMasterListFromCloud(Connection localConn, Connection cloudConn) throws Exception {
        Statement clearLocal = localConn.createStatement();
        clearLocal.execute("DELETE FROM local_students");

        // FIXED: Added exit_time to the Supabase fetch
        String cloudSql = "SELECT barcode, first_name, last_name, block_section, class_time, exit_time FROM students";
        PreparedStatement cloudStmt = cloudConn.prepareStatement(cloudSql);
        ResultSet rs = cloudStmt.executeQuery();

        // FIXED: Added 5th parameter (?) for exit_time
        String localSql = "INSERT INTO local_students (barcode, full_name, section, class_time, exit_time) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement localStmt = localConn.prepareStatement(localSql);

        int count = 0;
        while (rs.next()) {
            localStmt.setString(1, rs.getString("barcode"));
            localStmt.setString(2, rs.getString("first_name") + " " + rs.getString("last_name"));
            localStmt.setString(3, rs.getString("block_section"));
            localStmt.setString(4, rs.getString("class_time"));
            localStmt.setString(5, rs.getString("exit_time")); // Pulling the exit time!
            localStmt.executeUpdate();
            count++;
        }

        System.out.println("Master list updated. Records cached: " + count);
        ID.clear();
        bootUpOffline();
    }
}