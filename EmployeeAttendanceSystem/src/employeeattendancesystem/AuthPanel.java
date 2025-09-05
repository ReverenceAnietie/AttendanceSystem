package employeeattendancesystem;

import javax.swing.*;
import java.awt.*;

public class AuthPanel extends JFrame {
    private JTextField usernameFieldLogin;
    private JPasswordField passwordFieldLogin;
    private JTextField usernameFieldRegister;
    private JPasswordField passwordFieldRegister;
    private JTextField fullNameField;
    private JTextField emailField;
    private RoundedButton loginButton;
    private RoundedButton registerButton;

    public AuthPanel() {
        setTitle("Employee Attendance System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 500);
        setLocationRelativeTo(null);
        setResizable(false);

        initializeComponents();
        setupLayout();
        setupEventListeners();
    }

    private void initializeComponents() {
        usernameFieldLogin = new JTextField(20);
        usernameFieldLogin.setFont(new Font("Helvetica Neue", Font.PLAIN, 14));
        
        passwordFieldLogin = new JPasswordField(20);
        passwordFieldLogin.setFont(new Font("Helvetica Neue", Font.PLAIN, 14));
        
        fullNameField = new JTextField(20);
        fullNameField.setFont(new Font("Helvetica Neue", Font.PLAIN, 14));
        
        usernameFieldRegister = new JTextField(20);
        usernameFieldRegister.setFont(new Font("Helvetica Neue", Font.PLAIN, 14));
        
        passwordFieldRegister = new JPasswordField(20);
        passwordFieldRegister.setFont(new Font("Helvetica Neue", Font.PLAIN, 14));
        
        emailField = new JTextField(20);
        emailField.setFont(new Font("Helvetica Neue", Font.PLAIN, 14));
        
        loginButton = new RoundedButton("Login");
        loginButton.setBackground(new Color(199, 21, 133));
        loginButton.setForeground(Color.WHITE);
        loginButton.setPreferredSize(new Dimension(120, 40));
        loginButton.setFont(new Font("Helvetica Neue", Font.BOLD, 14));
        
        registerButton = new RoundedButton("Register");
        registerButton.setBackground(new Color(199, 21, 133));
        registerButton.setForeground(Color.WHITE);
        registerButton.setPreferredSize(new Dimension(120, 40));
        registerButton.setFont(new Font("Helvetica Neue", Font.BOLD, 14));
    }

    private void setupLayout() {
        setLayout(new BorderLayout());
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(245, 245, 245));
        
        JLabel titleLabel = new JLabel("Employee Attendance System");
        titleLabel.setFont(new Font("Helvetica Neue", Font.BOLD, 20));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(20));
        
        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new GridBagLayout());
        loginPanel.setBackground(new Color(245, 245, 245));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        loginPanel.add(new JLabel("Username:"), gbc);
        
        gbc.gridx = 1;
        loginPanel.add(usernameFieldLogin, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        loginPanel.add(new JLabel("Password:"), gbc);
        
        gbc.gridx = 1;
        loginPanel.add(passwordFieldLogin, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        loginPanel.add(loginButton, gbc);
        
        JPanel registerPanel = new JPanel();
        registerPanel.setLayout(new GridBagLayout());
        registerPanel.setBackground(new Color(245, 245, 245));
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        registerPanel.add(new JLabel("Full Name:"), gbc);
        
        gbc.gridx = 1;
        registerPanel.add(fullNameField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        registerPanel.add(new JLabel("Username:"), gbc);
        
        gbc.gridx = 1;
        registerPanel.add(usernameFieldRegister, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        registerPanel.add(new JLabel("Email:"), gbc);
        
        gbc.gridx = 1;
        registerPanel.add(emailField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        registerPanel.add(new JLabel("Password:"), gbc);
        
        gbc.gridx = 1;
        registerPanel.add(passwordFieldRegister, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        registerPanel.add(registerButton, gbc);
        
        JTabbedPane authTabs = new JTabbedPane();
        authTabs.addTab("Login", loginPanel);
        authTabs.addTab("Register", registerPanel);
        
        mainPanel.add(authTabs);
        add(mainPanel, BorderLayout.CENTER);
    }

    private void setupEventListeners() {
        loginButton.addActionListener(e -> login());
        registerButton.addActionListener(e -> register());
    }

    private void login() {
        String username = usernameFieldLogin.getText().trim();
        String password = new String(passwordFieldLogin.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please fill in all fields.",
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        DatabaseManager.User user = DatabaseManager.authenticateUser(username, password);
        if (user != null) {
            if (user.isAdmin()) {
                new AdminPanel(user).setVisible(true);
            } else {
                new AttendancePanel(user).setVisible(true);
            }
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, 
                "Invalid username or password.",
                "Login Failed", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void register() {
        String username = usernameFieldRegister.getText().trim();
        String password = new String(passwordFieldRegister.getPassword());
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        
        if (username.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please fill in all required fields.",
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (DatabaseManager.registerUser(username, password, fullName, email)) {
            JOptionPane.showMessageDialog(this, 
                "Registration successful! Please login.",
                "Success", 
                JOptionPane.INFORMATION_MESSAGE);
            usernameFieldRegister.setText("");
            passwordFieldRegister.setText("");
            fullNameField.setText("");
            emailField.setText("");
        } else {
            JOptionPane.showMessageDialog(this, 
                "Registration failed. Username may already exist.",
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
}