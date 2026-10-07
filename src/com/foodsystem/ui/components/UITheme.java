package com.foodsystem.ui.components;

import com.foodsystem.models.enums.OrderStatus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class UITheme {

    // Color Palette
    public static final Color PRIMARY = new Color(249, 115, 22);       // #F97316 (Food Orange)
    public static final Color PRIMARY_DARK = new Color(194, 65, 12);   // #C2410C
    public static final Color PRIMARY_LIGHT = new Color(255, 237, 213);// #FFEDD5

    public static final Color SECONDARY = new Color(30, 41, 59);       // Slate 800
    public static final Color BG_LIGHT = new Color(248, 250, 252);     // Slate 50
    public static final Color CARD_BG = Color.WHITE;
    public static final Color BORDER = new Color(226, 232, 240);       // Slate 200

    public static final Color TEXT_DARK = new Color(15, 23, 42);       // Slate 900
    public static final Color TEXT_MUTED = new Color(100, 116, 139);   // Slate 500
    public static final Color TEXT_LIGHT = Color.WHITE;

    public static final Color SUCCESS = new Color(16, 185, 129);       // Emerald 500
    public static final Color DANGER = new Color(239, 68, 68);         // Red 500
    public static final Color WARNING = new Color(245, 158, 11);       // Amber 500
    public static final Color INFO = new Color(59, 130, 246);          // Blue 500

    // Fonts
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_SUBHEADER = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_PRICE = new Font("Segoe UI", Font.BOLD, 16);

    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.US);

    public static String formatCurrency(BigDecimal amount) {
        if (amount == null) return "$0.00";
        return CURRENCY_FORMAT.format(amount);
    }

    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY_BOLD);
        btn.setBackground(PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(8, 16, 8, 16));
        btn.setOpaque(true);

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(PRIMARY_DARK);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(PRIMARY);
            }
        });
        return btn;
    }

    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY);
        btn.setBackground(new Color(241, 245, 249));
        btn.setForeground(TEXT_DARK);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            new EmptyBorder(6, 12, 6, 12)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JButton createDangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY_BOLD);
        btn.setBackground(DANGER);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(6, 12, 6, 12));
        btn.setOpaque(true);
        return btn;
    }

    public static JLabel createBadge(String text, Color bg, Color fg) {
        JLabel badge = new JLabel(text, SwingConstants.CENTER);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setOpaque(true);
        badge.setBackground(bg);
        badge.setForeground(fg);
        badge.setBorder(new EmptyBorder(3, 8, 3, 8));
        return badge;
    }

    public static JLabel createStatusBadge(OrderStatus status) {
        Color bg;
        Color fg = Color.WHITE;
        switch (status) {
            case PENDING:
                bg = WARNING;
                break;
            case CONFIRMED:
                bg = INFO;
                break;
            case PREPARING:
                bg = new Color(139, 92, 246); // Purple
                break;
            case OUT_FOR_DELIVERY:
                bg = new Color(6, 182, 212); // Cyan
                break;
            case DELIVERED:
                bg = SUCCESS;
                break;
            case CANCELLED:
                bg = DANGER;
                break;
            default:
                bg = TEXT_MUTED;
        }
        return createBadge(status.getDisplayName(), bg, fg);
    }
}
