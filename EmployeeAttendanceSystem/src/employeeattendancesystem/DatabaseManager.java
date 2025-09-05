package employeeattendancesystem;

import java.sql.*;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import javax.swing.JOptionPane;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String URL = "jdbc:postgresql://localhost:5432/AttendanceDB";
    private static final String USER = "postgres";
    private static final String PASSWORD = "Admin";
    
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL Driver not found", e);
        }
    }
    
    public static void initializeDatabase() {
        try (Connection conn = getConnection()) {
            String createUsersTable = """
                CREATE TABLE IF NOT EXISTS users (
                    id SERIAL PRIMARY KEY,
                    username VARCHAR(50) UNIQUE NOT NULL,
                    password VARCHAR(64) NOT NULL,
                    full_name VARCHAR(100) NOT NULL,
                    email VARCHAR(100),
                    is_admin BOOLEAN DEFAULT FALSE,
                    is_active BOOLEAN DEFAULT TRUE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    profile_image VARCHAR(255)
                )
            """;
            
            String createAttendanceTable = """
                CREATE TABLE IF NOT EXISTS attendance (
                    id SERIAL PRIMARY KEY,
                    user_id INTEGER REFERENCES users(id),
                    clock_in TIMESTAMP,
                    clock_out TIMESTAMP,
                    date DATE DEFAULT CURRENT_DATE,
                    total_hours DECIMAL(4,2),
                    status VARCHAR(20) DEFAULT 'CLOCKED_IN'
                )
            """;
            
            Statement stmt = conn.createStatement();
            stmt.execute(createUsersTable);
            stmt.execute(createAttendanceTable);
            
            String alterAdmin = "ALTER TABLE users ADD COLUMN IF NOT EXISTS is_admin BOOLEAN DEFAULT FALSE";
            String alterActive = "ALTER TABLE users ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE";
            String alterProfileImage = "ALTER TABLE users ADD COLUMN IF NOT EXISTS profile_image VARCHAR(255)";
            stmt.execute(alterAdmin);
            stmt.execute(alterActive);
            stmt.execute(alterProfileImage);
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, 
                "Database connection failed. Please ensure PostgreSQL is running.\nError: " + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public static boolean registerUser(String username, String password, String fullName, String email) {
        try (Connection conn = getConnection()) {
            String countSql = "SELECT COUNT(*) FROM users";
            PreparedStatement countPstmt = conn.prepareStatement(countSql);
            ResultSet rs = countPstmt.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            boolean isAdmin = (count == 0);
            
            String sql = "INSERT INTO users (username, password, full_name, email, is_admin, is_active, profile_image) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            
            pstmt.setString(1, username);
            pstmt.setString(2, hashPassword(password));
            pstmt.setString(3, fullName);
            pstmt.setString(4, email);
            pstmt.setBoolean(5, isAdmin);
            pstmt.setBoolean(6, true);
            pstmt.setNull(7, Types.VARCHAR);
            
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
    
    public static User authenticateUser(String username, String password) {
        String sql = "SELECT id, username, full_name, email, is_admin, is_active, profile_image FROM users WHERE LOWER(username) = ? AND password = ? AND is_active = true";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username.toLowerCase());
            pstmt.setString(2, hashPassword(password));
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("full_name"),
                    rs.getString("email"),
                    rs.getBoolean("is_admin"),
                    rs.getBoolean("is_active"),
                    rs.getString("profile_image")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static User getUserById(int userId) {
        String sql = "SELECT id, username, full_name, email, is_admin, is_active, profile_image FROM users WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("full_name"),
                    rs.getString("email"),
                    rs.getBoolean("is_admin"),
                    rs.getBoolean("is_active"),
                    rs.getString("profile_image")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static boolean clockIn(int userId) {
        String checkSql = "SELECT id FROM attendance WHERE user_id = ? AND date = CURRENT_DATE AND status = 'CLOCKED_IN'";
        try (Connection conn = getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            
            checkStmt.setInt(1, userId);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                return false; // Already clocked in
            }
        } catch (SQLException e) {
            return false;
        }
        
        String sql = "INSERT INTO attendance (user_id, clock_in, status) VALUES (?, CURRENT_TIMESTAMP, 'CLOCKED_IN')";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
    
    public static boolean clockOut(int userId) {
        String sql = """
            UPDATE attendance 
            SET clock_out = CURRENT_TIMESTAMP, 
                status = 'CLOCKED_OUT',
                total_hours = EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - clock_in))/3600
            WHERE user_id = ? AND date = CURRENT_DATE AND status = 'CLOCKED_IN'
        """;
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            return false;
        }
    }
    
    public static String getTodayStatus(int userId) {
        String sql = """
            SELECT clock_in, clock_out, status, total_hours 
            FROM attendance 
            WHERE user_id = ? AND date = CURRENT_DATE
        """;
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String status = rs.getString("status");
                Timestamp clockIn = rs.getTimestamp("clock_in");
                Timestamp clockOut = rs.getTimestamp("clock_out");
                Double totalHours = rs.getDouble("total_hours");
                
                StringBuilder sb = new StringBuilder();
                sb.append("Status: ").append(status).append("\n");
                sb.append("Clock In: ").append(clockIn != null ? clockIn.toString() : "N/A").append("\n");
                if (clockOut != null) {
                    sb.append("Clock Out: ").append(clockOut.toString()).append("\n");
                    int hours = (int) totalHours.doubleValue();
                    int mins = (int) ((totalHours - hours) * 60);
                    sb.append("Time Spent: ").append(hours).append(" hours ").append(mins).append(" minutes");
                } else {
                    sb.append("Clock Out: N/A\n");
                    sb.append("Time Spent: N/A (Still clocked in)");
                }
                return sb.toString();
            }
            return "No attendance record for today";
        } catch (SQLException e) {
            return "Error retrieving status";
        }
    }
    
    public static List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, username, full_name, email, is_admin, is_active, profile_image FROM users";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                users.add(new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("full_name"),
                    rs.getString("email"),
                    rs.getBoolean("is_admin"),
                    rs.getBoolean("is_active"),
                    rs.getString("profile_image")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }
    
    public static boolean toggleActive(int userId, boolean active) {
        String sql = "UPDATE users SET is_active = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setBoolean(1, active);
            pstmt.setInt(2, userId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            return false;
        }
    }
    
    public static List<AttendanceRecord> getTodayAttendances() {
        List<AttendanceRecord> records = new ArrayList<>();
        String sql = """
            SELECT a.user_id, u.full_name, a.clock_in, a.clock_out, a.total_hours, a.date 
            FROM attendance a 
            JOIN users u ON a.user_id = u.id 
            WHERE a.date = CURRENT_DATE
        """;
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                records.add(new AttendanceRecord(
                    rs.getInt("user_id"),
                    rs.getString("full_name"),
                    rs.getTimestamp("clock_in"),
                    rs.getTimestamp("clock_out"),
                    rs.getDouble("total_hours"),
                    rs.getDate("date")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }
    
    public static List<AttendanceRecord> getAllAttendances() {
        List<AttendanceRecord> records = new ArrayList<>();
        String sql = """
            SELECT a.user_id, u.full_name, a.clock_in, a.clock_out, a.total_hours, a.date 
            FROM attendance a 
            JOIN users u ON a.user_id = u.id 
            ORDER BY a.date DESC, a.clock_in DESC
        """;
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                records.add(new AttendanceRecord(
                    rs.getInt("user_id"),
                    rs.getString("full_name"),
                    rs.getTimestamp("clock_in"),
                    rs.getTimestamp("clock_out"),
                    rs.getDouble("total_hours"),
                    rs.getDate("date")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }
    
    public static List<AttendanceRecord> getAttendancesByDate(Date date) {
        List<AttendanceRecord> records = new ArrayList<>();
        String sql = """
            SELECT a.user_id, u.full_name, a.clock_in, a.clock_out, a.total_hours, a.date 
            FROM attendance a 
            JOIN users u ON a.user_id = u.id 
            WHERE a.date = ?
            ORDER BY a.clock_in DESC
        """;
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDate(1, date);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                records.add(new AttendanceRecord(
                    rs.getInt("user_id"),
                    rs.getString("full_name"),
                    rs.getTimestamp("clock_in"),
                    rs.getTimestamp("clock_out"),
                    rs.getDouble("total_hours"),
                    rs.getDate("date")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }
    
    public static int getTotalUsers() {
        String sql = "SELECT COUNT(*) FROM users";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    public static boolean updateProfileImage(int userId, String imagePath) {
        String sql = "UPDATE users SET profile_image = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            if (imagePath != null && !imagePath.isEmpty()) {
                pstmt.setString(1, imagePath);
            } else {
                pstmt.setNull(1, Types.VARCHAR);
            }
            pstmt.setInt(2, userId);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                System.err.println("No rows updated for userId: " + userId);
                return false;
            }
            return true;
        } catch (SQLException e) {
            System.err.println("SQL error updating profile image for userId " + userId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public static class User {
        private int id;
        private String username;
        private String fullName;
        private String email;
        private boolean isAdmin;
        private boolean isActive;
        private String profileImage;
        
        public User(int id, String username, String fullName, String email, boolean isAdmin, boolean isActive, String profileImage) {
            this.id = id;
            this.username = username;
            this.fullName = fullName;
            this.email = email;
            this.isAdmin = isAdmin;
            this.isActive = isActive;
            this.profileImage = profileImage;
        }
        
        public int getId() { return id; }
        public String getUsername() { return username; }
        public String getFullName() { return fullName; }
        public String getEmail() { return email; }
        public boolean isAdmin() { return isAdmin; }
        public boolean isActive() { return isActive; }
        public String getProfileImage() { return profileImage; }
    }
    
    public static class AttendanceRecord {
        private int userId;
        private String fullName;
        private Timestamp clockIn;
        private Timestamp clockOut;
        private double totalHours;
        private Date date;
        
        public AttendanceRecord(int userId, String fullName, Timestamp clockIn, Timestamp clockOut, double totalHours, Date date) {
            this.userId = userId;
            this.fullName = fullName;
            this.clockIn = clockIn;
            this.clockOut = clockOut;
            this.totalHours = totalHours;
            this.date = date;
        }
        
        public int getUserId() { return userId; }
        public String getFullName() { return fullName; }
        public Timestamp getClockIn() { return clockIn; }
        public Timestamp getClockOut() { return clockOut; }
        public double getTotalHours() { return totalHours; }
        public Date getDate() { return date; }
    }
}