package com.foodsystem;

import com.foodsystem.config.DBConnection;
import com.foodsystem.ui.MainFrame;

import javax.swing.*;
import java.awt.*;

public class Main {

    public static void main(String[] args) {
        // 1. Configure Modern Look and Feel (FlatLaf if available, else Nimbus/System)
        try {
            // Attempt to load FlatLaf Light
            Class<?> flatLafClass = Class.forName("com.formdev.flatlaf.FlatLightLaf");
            LookAndFeel flatLaf = (LookAndFeel) flatLafClass.getDeclaredConstructor().newInstance();
            UIManager.setLookAndFeel(flatLaf);
        } catch (Exception e) {
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception ex) {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ignored) {}
            }
        }

        // Global UI tweaks
        UIManager.put("Button.arc", 8);
        UIManager.put("Component.arc", 8);
        UIManager.put("ProgressBar.arc", 8);
        UIManager.put("TextComponent.arc", 8);

        // 2. Test DB Connection and ensure schema exists
        System.out.println("Checking MySQL Database connection...");
        if (!DBConnection.testConnection()) {
            System.err.println("WARNING: Unable to connect to MySQL on 127.0.0.1:3306.");
            System.err.println("Please make sure XAMPP MySQL is running.");
        } else {
            System.out.println("MySQL Connected successfully.");
            DBConnection.ensureSchemaInitialized();
        }

        // 3. Launch GUI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
