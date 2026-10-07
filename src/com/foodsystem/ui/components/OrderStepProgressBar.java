package com.foodsystem.ui.components;

import com.foodsystem.models.enums.OrderStatus;

import javax.swing.*;
import java.awt.*;

public class OrderStepProgressBar extends JPanel {

    private OrderStatus status = OrderStatus.PENDING;

    private static final String[] STEPS = {
        "Placed", "Confirmed", "Preparing", "Out for Delivery", "Delivered"
    };

    private static final OrderStatus[] STEP_STATUSES = {
        OrderStatus.PENDING,
        OrderStatus.CONFIRMED,
        OrderStatus.PREPARING,
        OrderStatus.OUT_FOR_DELIVERY,
        OrderStatus.DELIVERED
    };

    public OrderStepProgressBar() {
        setPreferredSize(new Dimension(550, 75));
        setOpaque(false);
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        if (status == OrderStatus.CANCELLED) {
            // Draw cancelled banner
            g2.setColor(new Color(254, 242, 242));
            g2.fillRoundRect(10, 10, width - 20, height - 20, 8, 8);
            g2.setColor(UITheme.DANGER);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(10, 10, width - 20, height - 20, 8, 8);

            g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
            g2.setColor(UITheme.DANGER);
            String cancelText = "🛑 ORDER CANCELLED & PAYMENT REFUNDED";
            FontMetrics fm = g2.getFontMetrics();
            int tx = (width - fm.stringWidth(cancelText)) / 2;
            int ty = (height + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(cancelText, tx, ty);
            g2.dispose();
            return;
        }

        int activeIndex = getStepIndex(status);

        int startX = 45;
        int endX = width - 45;
        int nodeY = 28;
        int circleRadius = 14;

        int stepCount = STEPS.length;
        int stepSpacing = (endX - startX) / (stepCount - 1);

        // Draw connecting background line
        g2.setColor(UITheme.BORDER);
        g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(startX, nodeY, endX, nodeY);

        // Draw active connecting line
        if (activeIndex > 0) {
            int currentLineEnd = startX + activeIndex * stepSpacing;
            g2.setColor(UITheme.PRIMARY);
            g2.drawLine(startX, nodeY, currentLineEnd, nodeY);
        }

        // Draw nodes
        for (int i = 0; i < stepCount; i++) {
            int cx = startX + i * stepSpacing;
            boolean isCompleted = i < activeIndex;
            boolean isCurrent = i == activeIndex;

            if (isCompleted) {
                // Completed node: Green or Primary filled with check
                g2.setColor(UITheme.SUCCESS);
                g2.fillOval(cx - circleRadius, nodeY - circleRadius, circleRadius * 2, circleRadius * 2);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                // Draw checkmark
                g2.drawLine(cx - 5, nodeY, cx - 2, nodeY + 4);
                g2.drawLine(cx - 2, nodeY + 4, cx + 5, nodeY - 4);
            } else if (isCurrent) {
                // Current node: Primary filled with ring
                g2.setColor(UITheme.PRIMARY_LIGHT);
                g2.fillOval(cx - circleRadius - 4, nodeY - circleRadius - 4, (circleRadius + 4) * 2, (circleRadius + 4) * 2);
                g2.setColor(UITheme.PRIMARY);
                g2.fillOval(cx - circleRadius, nodeY - circleRadius, circleRadius * 2, circleRadius * 2);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                String num = String.valueOf(i + 1);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(num, cx - fm.stringWidth(num) / 2, nodeY + fm.getAscent() / 2 - 1);
            } else {
                // Inactive node: Gray outline
                g2.setColor(Color.WHITE);
                g2.fillOval(cx - circleRadius, nodeY - circleRadius, circleRadius * 2, circleRadius * 2);
                g2.setColor(UITheme.BORDER);
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(cx - circleRadius, nodeY - circleRadius, circleRadius * 2, circleRadius * 2);

                g2.setColor(UITheme.TEXT_MUTED);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                String num = String.valueOf(i + 1);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(num, cx - fm.stringWidth(num) / 2, nodeY + fm.getAscent() / 2 - 1);
            }

            // Draw Step Title
            g2.setFont(isCurrent ? new Font("Segoe UI", Font.BOLD, 12) : new Font("Segoe UI", Font.PLAIN, 11));
            g2.setColor(isCurrent ? UITheme.TEXT_DARK : (isCompleted ? UITheme.SUCCESS : UITheme.TEXT_MUTED));
            FontMetrics fmTitle = g2.getFontMetrics();
            int titleWidth = fmTitle.stringWidth(STEPS[i]);
            g2.drawString(STEPS[i], cx - titleWidth / 2, nodeY + circleRadius + 18);
        }

        g2.dispose();
    }

    private int getStepIndex(OrderStatus st) {
        for (int i = 0; i < STEP_STATUSES.length; i++) {
            if (STEP_STATUSES[i] == st) {
                return i;
            }
        }
        return 0;
    }
}
