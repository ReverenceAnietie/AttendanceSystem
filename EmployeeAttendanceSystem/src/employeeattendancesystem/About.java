package employeeattendancesystem;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class About extends JFrame {
    private DatabaseManager.User currentUser;
    private JLabel profileImageLabel;
    private RoundedButton uploadImageButton;
    private RoundedButton closeButton;

    public About(DatabaseManager.User user) {
        this.currentUser = user;
        
        setTitle("About - Employee Attendance System");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(400, 400);
        setLocationRelativeTo(null);
        setResizable(false);

        initializeComponents();
        setupLayout();
        setupEventListeners();
        updateProfileImage();
    }

    private void initializeComponents() {
        profileImageLabel = new JLabel();
        profileImageLabel.setPreferredSize(new Dimension(100, 100));
        profileImageLabel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        profileImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        uploadImageButton = new RoundedButton("Upload Profile Image");
        uploadImageButton.setBackground(new Color(199, 21, 133));
        uploadImageButton.setForeground(Color.WHITE);
        uploadImageButton.setPreferredSize(new Dimension(160, 40));
        uploadImageButton.setFont(new Font("Helvetica Neue", Font.BOLD, 14));
        
        closeButton = new RoundedButton("Close");
        closeButton.setBackground(new Color(100, 100, 100));
        closeButton.setForeground(Color.WHITE);
        closeButton.setPreferredSize(new Dimension(120, 40));
        closeButton.setFont(new Font("Helvetica Neue", Font.BOLD, 14));
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
        
        JLabel versionLabel = new JLabel("Version 1.0");
        versionLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 14));
        versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(versionLabel);
        mainPanel.add(Box.createVerticalStrut(10));
        
        JLabel descriptionLabel = new JLabel("<html>A simple application to manage employee attendance.<br>Developed for efficient clock-in and clock-out tracking.</html>");
        descriptionLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 14));
        descriptionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(descriptionLabel);
        mainPanel.add(Box.createVerticalStrut(20));
        
        mainPanel.add(profileImageLabel);
        mainPanel.add(Box.createVerticalStrut(10));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(new Color(245, 245, 245));
        buttonPanel.add(uploadImageButton);
        buttonPanel.add(closeButton);
        mainPanel.add(buttonPanel);
        
        add(mainPanel, BorderLayout.CENTER);
    }

    private void setupEventListeners() {
        uploadImageButton.addActionListener(e -> uploadProfileImage());
        closeButton.addActionListener(e -> dispose());
    }

    private void uploadProfileImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Image files", "jpg", "png", "jpeg"));
        int result = fileChooser.showOpenDialog(this);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (!selectedFile.exists()) {
                JOptionPane.showMessageDialog(this, 
                    "Selected file does not exist.",
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            String fileName = selectedFile.getName().toLowerCase();
            if (!fileName.endsWith(".jpg") && !fileName.endsWith(".png") && !fileName.endsWith(".jpeg")) {
                JOptionPane.showMessageDialog(this, 
                    "Please select a valid image file (jpg, png, jpeg).",
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            Path profilesDir = Paths.get("src/employeeattendancesystem/profiles");
            try {
                Files.createDirectories(profilesDir);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, 
                    "Failed to create profiles directory: " + e.getMessage(),
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            String newFileName = "user_" + currentUser.getId() + "_" + System.currentTimeMillis() + "." + getFileExtension(fileName);
            Path targetPath = profilesDir.resolve(newFileName);
            try {
                Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                String imagePath = targetPath.toAbsolutePath().toString();
                if (DatabaseManager.updateProfileImage(currentUser.getId(), imagePath)) {
                    currentUser = DatabaseManager.getUserById(currentUser.getId());
                    if (currentUser == null) {
                        JOptionPane.showMessageDialog(this, 
                            "Failed to refresh user data after image upload.",
                            "Error", 
                            JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    updateProfileImage();
                    JOptionPane.showMessageDialog(this, 
                        "Profile image uploaded successfully.",
                        "Success", 
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Failed to update profile image in database for user ID: " + currentUser.getId(),
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, 
                    "Failed to copy image: " + e.getMessage(),
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex == -1 ? "" : fileName.substring(dotIndex + 1);
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
}