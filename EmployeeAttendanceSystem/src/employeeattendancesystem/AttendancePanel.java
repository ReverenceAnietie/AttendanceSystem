package employeeattendancesystem;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.toedter.calendar.JDateChooser;
import java.io.File;
import java.sql.Timestamp;
import java.util.List;
import javax.swing.table.DefaultTableModel;

public class AttendancePanel extends JFrame {
    private DatabaseManager.User currentUser;
    private JLabel welcomeLabel;
    private JLabel timeLabel;
    private JLabel statusLabel;
    private JTextArea statusArea;
    private JLabel profileImageLabel;
    private RoundedButton clockInButton;
    private RoundedButton clockOutButton;
    private RoundedButton refreshButton;
    private RoundedButton logoutButton;
    private RoundedButton aboutButton;
    private JTable historyTable;
    private JDateChooser dateChooser;
    private RoundedButton filterHistoryButton;
    private RoundedButton showAllHistoryButton;
    private Timer timeUpdateTimer;
    private JTabbedPane tabbedPane;

    public AttendancePanel(DatabaseManager.User user) {
        this.currentUser = user;

        setTitle("Attendance Panel - Employee Attendance System - " + user.getFullName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        initializeComponents();
        setupLayout();
        setupEventListeners();
        startTimeUpdater();
        refreshStatus();
        updateProfileImage();
        refreshHistoryTable();
    }

    private void initializeComponents() {
        welcomeLabel = new JLabel("Welcome, " + currentUser.getFullName());
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

        refreshButton = new RoundedButton("Refresh Status");
        refreshButton.setPreferredSize(new Dimension(140, 45));
        refreshButton.setBackground(new Color(199, 21, 133));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFont(new Font("Helvetica Neue", Font.BOLD, 14));
        refreshButton.setFocusPainted(false);

        historyTable = new JTable();

        dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-dd");
        dateChooser.setPreferredSize(new Dimension(150, 30));

        filterHistoryButton = new RoundedButton("Filter by Date");
        filterHistoryButton.setBackground(new Color(199, 21, 133));
        filterHistoryButton.setForeground(Color.WHITE);
        filterHistoryButton.setPreferredSize(new Dimension(160, 45));
        filterHistoryButton.setFont(new Font("Helvetica Neue", Font.BOLD, 12));

        showAllHistoryButton = new RoundedButton("Show All History");
        showAllHistoryButton.setBackground(new Color(199, 21, 133));
        showAllHistoryButton.setForeground(Color.WHITE);
        showAllHistoryButton.setPreferredSize(new Dimension(160, 45));
        showAllHistoryButton.setFont(new Font("Helvetica Neue", Font.BOLD, 12));

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

        // Header
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
        personalButtonPanel.add(refreshButton);

        personalPanel.add(statusPanel, BorderLayout.CENTER);
        personalPanel.add(personalButtonPanel, BorderLayout.SOUTH);

        tabbedPane.addTab("Personal Attendance", personalPanel);

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
        historyButtonPanel.add(showAllHistoryButton);
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
        refreshButton.addActionListener(e -> {
            refreshStatus();
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
                        welcomeLabel.setText("Welcome, " + currentUser.getFullName());
                    } else {
                        JOptionPane.showMessageDialog(AttendancePanel.this, 
                            "Failed to refresh user data.",
                            "Error", 
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
        });
        filterHistoryButton.addActionListener(e -> filterHistoryByDate());
        showAllHistoryButton.addActionListener(e -> refreshHistoryTable());
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
            refreshStatus();
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
                refreshStatus();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Clock out failed. You might not be clocked in for today.",
                    "Clock Out Failed",
                    JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void refreshStatus() {
        String status = DatabaseManager.getTodayStatus(currentUser.getId());
        statusArea.setText(status);

        statusArea.append("\n\n--- Employee Information ---\n");
        statusArea.append("Username: " + currentUser.getUsername() + "\n");
        statusArea.append("Full Name: " + currentUser.getFullName() + "\n");
        statusArea.append("Email: " + (currentUser.getEmail() != null ? currentUser.getEmail() : "Not provided") + "\n");
        statusArea.append("Last Updated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
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
        int rowCount = 0;
        for (DatabaseManager.AttendanceRecord record : records) {
            if (record.getUserId() == currentUser.getId()) {
                data[rowCount][0] = record.getDate() != null ? record.getDate().toString() : "N/A";
                data[rowCount][1] = record.getUserId();
                data[rowCount][2] = record.getFullName();
                data[rowCount][3] = record.getClockIn() != null ? record.getClockIn().toString() : "N/A";
                data[rowCount][4] = record.getClockOut() != null ? record.getClockOut().toString() : "N/A";
                if (record.getClockOut() != null) {
                    double total = record.getTotalHours();
                    int hours = (int) total;
                    int mins = (int) ((total - hours) * 60);
                    data[rowCount][5] = hours + " hours " + mins + " minutes";
                } else {
                    data[rowCount][5] = "Still Clocked In";
                }
                rowCount++;
            }
        }
        Object[][] filteredData = new Object[rowCount][6];
        System.arraycopy(data, 0, filteredData, 0, rowCount);
        historyTable.setModel(new DefaultTableModel(filteredData, columns));
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