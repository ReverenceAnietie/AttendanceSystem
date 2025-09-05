package employeeattendancesystem;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import com.toedter.calendar.JDateChooser;
import java.io.File;

public class AdminPanel extends JFrame {
    private DatabaseManager.User currentUser;
    private JLabel welcomeLabel;
    private JLabel timeLabel;
    private JLabel statusLabel;
    private JTextArea statusArea;
    private JLabel profileImageLabel;
    private RoundedButton clockInButton;
    private RoundedButton clockOutButton;
    private RoundedButton refreshPersonalButton;
    private JTable usersTable;
    private JTable attendanceTable;
    private JTable historyTable;
    private JLabel totalUsersLabel;
    private RoundedButton activateButton;
    private RoundedButton deactivateButton;
    private RoundedButton refreshUsersButton;
    private RoundedButton refreshAttendanceButton;
    private RoundedButton refreshHistoryButton;
    private JDateChooser dateChooser;
    private RoundedButton filterHistoryButton;
    private RoundedButton logoutButton;
    private RoundedButton aboutButton;
    private Timer timeUpdateTimer;
    private JTabbedPane tabbedPane;
    
    public AdminPanel(DatabaseManager.User user) {
        this.currentUser = user;
        
        setTitle("Admin Panel - Employee Attendance System - " + user.getFullName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setResizable(false);
        
        initializeComponents();
        setupLayout();
        setupEventListeners();
        startTimeUpdater();
        refreshPersonalStatus();
        refreshUsersTable();
        refreshAttendanceTable();
        refreshHistoryTable();
        updateProfileImage();
    }
    
    private void initializeComponents() {
        welcomeLabel = new JLabel("Welcome, " + currentUser.getFullName() + " (Admin)");
        welcomeLabel.setFont(new Font("Helvetica Neue", Font.BOLD, 24));
        welcomeLabel.setForeground(new Color(33, 33, 33));
        
        timeLabel = new JLabel();
        timeLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 16));
        timeLabel.setForeground(new Color(80, 80, 80));
        
        profileImageLabel = new JLabel();
        profileImageLabel.setPreferredSize(new Dimension(100, 100));
        profileImageLabel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        profileImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Helvetica Neue", Font.PLAIN, 14));
        
        statusLabel = new JLabel("Today's Attendance Status:");
        statusLabel.setFont(new Font("Helvetica Neue", Font.BOLD, 16));
        statusLabel.setForeground(new Color(60, 60, 60));
        
        statusArea = new JTextArea(8, 40);
        statusArea.setEditable(false);
        statusArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        statusArea.setBackground(new Color(248, 248, 248));
        statusArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        clockInButton = new RoundedButton("Clock In");
        clockInButton.setPreferredSize(new Dimension(140, 45));
        clockInButton.setBackground(new Color(199, 21, 133));
        clockInButton.setForeground(Color.WHITE);
        clockInButton.setFont(new Font("Helvetica Neue", Font.BOLD, 14));
        clockInButton.setFocusPainted(false);
        
        clockOutButton = new RoundedButton("Clock Out");
        clockOutButton.setPreferredSize(new Dimension(140, 45));
        clockOutButton.setBackground(new Color(199, 21, 133));
        clockOutButton.setForeground(Color.WHITE);
        clockOutButton.setFont(new Font("Helvetica Neue", Font.BOLD, 14));
        clockOutButton.setFocusPainted(false);
        
        refreshPersonalButton = new RoundedButton("Refresh Status");
        refreshPersonalButton.setPreferredSize(new Dimension(140, 45));
        refreshPersonalButton.setBackground(new Color(199, 21, 133));
        refreshPersonalButton.setForeground(Color.WHITE);
        refreshPersonalButton.setFont(new Font("Helvetica Neue", Font.BOLD, 14));
        refreshPersonalButton.setFocusPainted(false);
        
        usersTable = new JTable();
        usersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        totalUsersLabel = new JLabel("Total Users: 0");
        totalUsersLabel.setFont(new Font("Helvetica Neue", Font.BOLD, 16));
        
        activateButton = new RoundedButton("Activate Selected");
        activateButton.setBackground(new Color(199, 21, 133));
        activateButton.setForeground(Color.WHITE);
        activateButton.setPreferredSize(new Dimension(160, 45));
        activateButton.setFont(new Font("Helvetica Neue", Font.BOLD, 12));
        activateButton.setEnabled(false);
        
        deactivateButton = new RoundedButton("Deactivate Selected");
        deactivateButton.setBackground(new Color(199, 21, 133));
        deactivateButton.setForeground(Color.WHITE);
        deactivateButton.setPreferredSize(new Dimension(160, 45));
        deactivateButton.setFont(new Font("Helvetica Neue", Font.BOLD, 12));
        deactivateButton.setEnabled(false);
        
        refreshUsersButton = new RoundedButton("Refresh Users");
        refreshUsersButton.setBackground(new Color(199, 21, 133));
        refreshUsersButton.setForeground(Color.WHITE);
        refreshUsersButton.setPreferredSize(new Dimension(140, 45));
        refreshUsersButton.setFont(new Font("Helvetica Neue", Font.BOLD, 12));
        
        attendanceTable = new JTable();
        
        refreshAttendanceButton = new RoundedButton("Refresh Attendance");
        refreshAttendanceButton.setBackground(new Color(199, 21, 133));
        refreshAttendanceButton.setForeground(Color.WHITE);
        refreshAttendanceButton.setPreferredSize(new Dimension(160, 45));
        refreshAttendanceButton.setFont(new Font("Helvetica Neue", Font.BOLD, 12));
        
        historyTable = new JTable();
        
        dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-dd");
        dateChooser.setPreferredSize(new Dimension(150, 30));
        
        refreshHistoryButton = new RoundedButton("Show All History");
        refreshHistoryButton.setBackground(new Color(199, 21, 133));
        refreshHistoryButton.setForeground(Color.WHITE);
        refreshHistoryButton.setPreferredSize(new Dimension(160, 45));
        refreshHistoryButton.setFont(new Font("Helvetica Neue", Font.BOLD, 12));
        
        filterHistoryButton = new RoundedButton("Filter by Date");
        filterHistoryButton.setBackground(new Color(199, 21, 133));
        filterHistoryButton.setForeground(Color.WHITE);
        filterHistoryButton.setPreferredSize(new Dimension(160, 45));
        filterHistoryButton.setFont(new Font("Helvetica Neue", Font.BOLD, 12));
        
        logoutButton = new RoundedButton("Logout");
        logoutButton.setPreferredSize(new Dimension(140, 45));
        logoutButton.setBackground(new Color(100, 100, 100));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFont(new Font("Helvetica Neue", Font.BOLD, 14));
        logoutButton.setFocusPainted(false);
        
        aboutButton = new RoundedButton("About");
        aboutButton.setPreferredSize(new Dimension(140, 45));
        aboutButton.setBackground(new Color(100, 100, 100));
        aboutButton.setForeground(Color.WHITE);
        aboutButton.setFont(new Font("Helvetica Neue", Font.BOLD, 14));
        aboutButton.setFocusPainted(false);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        headerPanel.setBackground(new Color(240, 248, 255));
        
        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        timePanel.setBackground(new Color(240, 248, 255));
        timePanel.add(timeLabel);
        headerPanel.add(timePanel);
        
        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        userInfoPanel.setBackground(new Color(240, 248, 255));
        userInfoPanel.add(welcomeLabel);
        userInfoPanel.add(Box.createHorizontalStrut(20));
        userInfoPanel.add(profileImageLabel);
        headerPanel.add(userInfoPanel);
        
        add(headerPanel, BorderLayout.NORTH);
        
        JPanel personalPanel = new JPanel(new BorderLayout());
        personalPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        personalPanel.setBackground(new Color(245, 245, 245));
        
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.add(statusLabel, BorderLayout.NORTH);
        JScrollPane statusScroll = new JScrollPane(statusArea);
        statusScroll.setBorder(null);
        statusPanel.add(statusScroll, BorderLayout.CENTER);
        
        JPanel personalButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        personalButtonPanel.setBackground(new Color(245, 245, 245));
        personalButtonPanel.add(clockInButton);
        personalButtonPanel.add(clockOutButton);
        personalButtonPanel.add(refreshPersonalButton);
        
        personalPanel.add(statusPanel, BorderLayout.CENTER);
        personalPanel.add(personalButtonPanel, BorderLayout.SOUTH);
        
        tabbedPane.addTab("Personal Attendance", personalPanel);
        
        JPanel usersPanel = new JPanel(new BorderLayout());
        usersPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        usersPanel.setBackground(new Color(245, 245, 245));
        
        JPanel usersHeaderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        usersHeaderPanel.setBackground(new Color(245, 245, 245));
        usersHeaderPanel.add(totalUsersLabel);
        usersPanel.add(usersHeaderPanel, BorderLayout.NORTH);
        
        JScrollPane usersScroll = new JScrollPane(usersTable);
        usersPanel.add(usersScroll, BorderLayout.CENTER);
        
        JPanel usersButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        usersButtonPanel.setBackground(new Color(245, 245, 245));
        usersButtonPanel.add(activateButton);
        usersButtonPanel.add(deactivateButton);
        usersButtonPanel.add(refreshUsersButton);
        usersPanel.add(usersButtonPanel, BorderLayout.SOUTH);
        
        tabbedPane.addTab("Manage Users", usersPanel);
        
        JPanel attendancePanel = new JPanel(new BorderLayout());
        attendancePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        attendancePanel.setBackground(new Color(245, 245, 245));
        
        JPanel attendanceHeaderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        attendanceHeaderPanel.setBackground(new Color(245, 245, 245));
        JLabel attendanceLabel = new JLabel("Today's Attendances");
        attendanceLabel.setFont(new Font("Helvetica Neue", Font.BOLD, 16));
        attendanceHeaderPanel.add(attendanceLabel);
        attendancePanel.add(attendanceHeaderPanel, BorderLayout.NORTH);
        
        JScrollPane attendanceScroll = new JScrollPane(attendanceTable);
        attendancePanel.add(attendanceScroll, BorderLayout.CENTER);
        
        JPanel attendanceButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        attendanceButtonPanel.setBackground(new Color(245, 245, 245));
        attendanceButtonPanel.add(refreshAttendanceButton);
        attendancePanel.add(attendanceButtonPanel, BorderLayout.SOUTH);
        
        tabbedPane.addTab("Today's Attendance", attendancePanel);
        
        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        historyPanel.setBackground(new Color(245, 245, 245));
        
        JPanel historyHeaderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        historyHeaderPanel.setBackground(new Color(245, 245, 245));
        JLabel historyLabel = new JLabel("Attendance History");
        historyLabel.setFont(new Font("Helvetica Neue", Font.BOLD, 16));
        historyHeaderPanel.add(historyLabel);
        historyHeaderPanel.add(Box.createHorizontalStrut(10));
        historyHeaderPanel.add(new JLabel("Select Date:"));
        historyHeaderPanel.add(dateChooser);
        historyPanel.add(historyHeaderPanel, BorderLayout.NORTH);
        
        JScrollPane historyScroll = new JScrollPane(historyTable);
        historyPanel.add(historyScroll, BorderLayout.CENTER);
        
        JPanel historyButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        historyButtonPanel.setBackground(new Color(245, 245, 245));
        historyButtonPanel.add(filterHistoryButton);
        historyButtonPanel.add(refreshHistoryButton);
        historyPanel.add(historyButtonPanel, BorderLayout.SOUTH);
        
        tabbedPane.addTab("Attendance History", historyPanel);
        
        add(tabbedPane, BorderLayout.CENTER);
        
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        footerPanel.setBackground(new Color(240, 240, 240));
        footerPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 200)));
        footerPanel.add(aboutButton);
        footerPanel.add(logoutButton);
        
        add(footerPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventListeners() {
        clockInButton.addActionListener(e -> clockIn());
        clockOutButton.addActionListener(e -> clockOut());
        refreshPersonalButton.addActionListener(e -> {
            refreshPersonalStatus();
            updateProfileImage();
        });
        logoutButton.addActionListener(e -> logout());
        aboutButton.addActionListener(e -> {
            About about = new About(currentUser);
            about.setVisible(true);
            about.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent e) {
                    DatabaseManager.User updatedUser = DatabaseManager.getUserById(currentUser.getId());
                    if (updatedUser != null) {
                        currentUser = updatedUser;
                        updateProfileImage();
                        welcomeLabel.setText("Welcome, " + currentUser.getFullName() + " (Admin)");
                    } else {
                        JOptionPane.showMessageDialog(AdminPanel.this, 
                            "Failed to refresh user data.",
                            "Error", 
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
        });
        
        refreshUsersButton.addActionListener(e -> refreshUsersTable());
        refreshAttendanceButton.addActionListener(e -> refreshAttendanceTable());
        refreshHistoryButton.addActionListener(e -> refreshHistoryTable());
        filterHistoryButton.addActionListener(e -> filterHistoryByDate());
        
        usersTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                boolean selected = usersTable.getSelectedRow() != -1;
                activateButton.setEnabled(selected);
                deactivateButton.setEnabled(selected);
            }
        });
        
        activateButton.addActionListener(e -> toggleUserActive(true));
        deactivateButton.addActionListener(e -> toggleUserActive(false));
    }
    
    private void startTimeUpdater() {
        timeUpdateTimer = new Timer(1000, e -> updateTime());
        timeUpdateTimer.start();
        updateTime();
    }
    
    private void updateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy - HH:mm:ss");
        timeLabel.setText(now.format(formatter));
    }
    
    private void clockIn() {
        if (DatabaseManager.clockIn(currentUser.getId())) {
            JOptionPane.showMessageDialog(this, 
                "Successfully clocked in at " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                "Clock In Successful", 
                JOptionPane.INFORMATION_MESSAGE);
            refreshPersonalStatus();
        } else {
            JOptionPane.showMessageDialog(this, 
                "Clock in failed. You might already be clocked in for today.",
                "Clock In Failed", 
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void clockOut() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to clock out?",
            "Confirm Clock Out",
            JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            if (DatabaseManager.clockOut(currentUser.getId())) {
                JOptionPane.showMessageDialog(this, 
                    "Successfully clocked out at " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                    "Clock Out Successful", 
                    JOptionPane.INFORMATION_MESSAGE);
                refreshPersonalStatus();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Clock out failed. You might not be clocked in for today.",
                    "Clock Out Failed", 
                    JOptionPane.WARNING_MESSAGE);
            }
        }
    }
    
    private void updateProfileImage() {
        String imagePath = currentUser.getProfileImage();
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                File imageFile = new File(imagePath);
                if (!imageFile.exists()) {
                    profileImageLabel.setIcon(null);
                    profileImageLabel.setText("Image file missing");
                    System.err.println("Image file not found for user ID " + currentUser.getId() + ": " + imagePath);
                    return;
                }
                ImageIcon imageIcon = new ImageIcon(imagePath);
                if (imageIcon.getIconWidth() == -1) {
                    profileImageLabel.setIcon(null);
                    profileImageLabel.setText("Invalid image");
                    System.err.println("Invalid image for user ID " + currentUser.getId() + ": " + imagePath);
                    return;
                }
                Image image = imageIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                profileImageLabel.setIcon(new ImageIcon(image));
                profileImageLabel.setText("");
            } catch (Exception e) {
                profileImageLabel.setIcon(null);
                profileImageLabel.setText("Error loading image");
                System.err.println("Error loading profile image for user ID " + currentUser.getId() + ": " + e.getMessage());
            }
        } else {
            profileImageLabel.setIcon(null);
            profileImageLabel.setText("No image");
        }
    }
    
    private void refreshPersonalStatus() {
        String status = DatabaseManager.getTodayStatus(currentUser.getId());
        statusArea.setText(status);
        
        statusArea.append("\n\n--- Employee Information ---\n");
        statusArea.append("Username: " + currentUser.getUsername() + "\n");
        statusArea.append("Full Name: " + currentUser.getFullName() + "\n");
        statusArea.append("Email: " + (currentUser.getEmail() != null ? currentUser.getEmail() : "Not provided") + "\n");
        statusArea.append("Last Updated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }
    
    private void refreshUsersTable() {
        List<DatabaseManager.User> users = DatabaseManager.getAllUsers();
        totalUsersLabel.setText("Total Users: " + DatabaseManager.getTotalUsers());
        
        String[] columns = {"ID", "Username", "Full Name", "Email", "Active"};
        Object[][] data = new Object[users.size()][5];
        for (int i = 0; i < users.size(); i++) {
            DatabaseManager.User user = users.get(i);
            data[i][0] = user.getId();
            data[i][1] = user.getUsername();
            data[i][2] = user.getFullName();
            data[i][3] = user.getEmail();
            data[i][4] = user.isActive() ? "Yes" : "No";
        }
        usersTable.setModel(new DefaultTableModel(data, columns));
    }
    
    private void refreshAttendanceTable() {
        List<DatabaseManager.AttendanceRecord> records = DatabaseManager.getTodayAttendances();
        
        String[] columns = {"User ID", "Full Name", "Clock In", "Clock Out", "Time Spent"};
        Object[][] data = new Object[records.size()][5];
        for (int i = 0; i < records.size(); i++) {
            DatabaseManager.AttendanceRecord record = records.get(i);
            data[i][0] = record.getUserId();
            data[i][1] = record.getFullName();
            data[i][2] = record.getClockIn() != null ? record.getClockIn().toString() : "N/A";
            data[i][3] = record.getClockOut() != null ? record.getClockOut().toString() : "N/A";
            if (record.getClockOut() != null) {
                double total = record.getTotalHours();
                int hours = (int) total;
                int mins = (int) ((total - hours) * 60);
                data[i][4] = hours + " hours " + mins + " minutes";
            } else {
                data[i][4] = "Still Clocked In";
            }
        }
        attendanceTable.setModel(new DefaultTableModel(data, columns));
    }
    
    private void refreshHistoryTable() {
        List<DatabaseManager.AttendanceRecord> records = DatabaseManager.getAllAttendances();
        updateHistoryTable(records);
    }
    
    private void filterHistoryByDate() {
        java.util.Date selectedDate = dateChooser.getDate();
        if (selectedDate == null) {
            JOptionPane.showMessageDialog(this, 
                "Please select a date to filter.",
                "Error", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        List<DatabaseManager.AttendanceRecord> records = DatabaseManager.getAttendancesByDate(new java.sql.Date(selectedDate.getTime()));
        updateHistoryTable(records);
    }
    
    private void updateHistoryTable(List<DatabaseManager.AttendanceRecord> records) {
        String[] columns = {"Date", "User ID", "Full Name", "Clock In", "Clock Out", "Time Spent"};
        Object[][] data = new Object[records.size()][6];
        for (int i = 0; i < records.size(); i++) {
            DatabaseManager.AttendanceRecord record = records.get(i);
            data[i][0] = record.getDate() != null ? record.getDate().toString() : "N/A";
            data[i][1] = record.getUserId();
            data[i][2] = record.getFullName();
            data[i][3] = record.getClockIn() != null ? record.getClockIn().toString() : "N/A";
            data[i][4] = record.getClockOut() != null ? record.getClockOut().toString() : "N/A";
            if (record.getClockOut() != null) {
                double total = record.getTotalHours();
                int hours = (int) total;
                int mins = (int) ((total - hours) * 60);
                data[i][5] = hours + " hours " + mins + " minutes";
            } else {
                data[i][5] = "Still Clocked In";
            }
        }
        historyTable.setModel(new DefaultTableModel(data, columns));
    }
    
    private void toggleUserActive(boolean active) {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow != -1) {
            int userId = (int) usersTable.getValueAt(selectedRow, 0);
            if (userId == currentUser.getId()) {
                JOptionPane.showMessageDialog(this, "Cannot modify your own account status.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (DatabaseManager.toggleActive(userId, active)) {
                JOptionPane.showMessageDialog(this, "User status updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshUsersTable();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update user status.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            if (timeUpdateTimer != null) {
                timeUpdateTimer.stop();
            }
            
            SwingUtilities.invokeLater(() -> {
                new AuthPanel().setVisible(true);
                this.dispose();
            });
        }
    }
    
    @Override
    public void dispose() {
        if (timeUpdateTimer != null) {
            timeUpdateTimer.stop();
        }
        super.dispose();
    }
}