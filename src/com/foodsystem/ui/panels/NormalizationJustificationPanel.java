package com.foodsystem.ui.panels;

import com.foodsystem.ui.components.CardPanel;
import com.foodsystem.ui.components.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class NormalizationJustificationPanel extends JPanel {

    public NormalizationJustificationPanel() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_LIGHT);

        initUI();
    }

    private void initUI() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(UITheme.BG_LIGHT);
        container.setBorder(new EmptyBorder(16, 20, 20, 20));

        // Header Title
        JLabel titleLabel = new JLabel("Database Normalization & ACID Transaction Architecture");
        titleLabel.setFont(UITheme.FONT_HEADER);
        titleLabel.setForeground(UITheme.TEXT_DARK);
        container.add(titleLabel);
        container.add(Box.createVerticalStrut(4));

        JLabel subLabel = new JLabel("Theoretical Justification (UNF → 1NF → 2NF → 3NF → BCNF) & MySQL InnoDB Constraints");
        subLabel.setFont(UITheme.FONT_BODY);
        subLabel.setForeground(UITheme.TEXT_MUTED);
        container.add(subLabel);
        container.add(Box.createVerticalStrut(16));

        // Card 1: Normalization Stages
        container.add(createCard(
            "1. Relational Normalization Breakdown & Justification",
            "<html><body style='width: 720px; font-family: Segoe UI; font-size: 11pt; color: #1E293B;'>" +
            "<p><b>Unnormalized Form (UNF):</b> A flat order receipt combines customer, restaurant, menu item arrays, payment, and status logs into a single record. " +
            "This causes repeating groups, multi-valued attributes, insertion anomalies (cannot add an item without an order), update anomalies (changing a restaurant phone mutates 100s of past orders), and deletion anomalies (deleting an order erases menu items).</p>" +
            "<br>" +
            "<table border='1' cellpadding='6' cellspacing='0' style='border-color: #CBD5E1; border-collapse: collapse; width: 100%;'>" +
            "<tr style='background-color: #F1F5F9;'><th>Normal Form</th><th>Violation Identified & Eliminated</th><th>Decomposition Strategy Applied</th></tr>" +
            "<tr><td><b>1NF (First Normal Form)</b></td><td>Repeating food items & comma-separated arrays</td><td>All column domains made strictly atomic; unique primary keys designated for each row.</td></tr>" +
            "<tr><td><b>2NF (Second Normal Form)</b></td><td>Partial functional dependency on composite key: in <i>(order_id, item_id)</i>, <i>item_name</i> depends solely on <i>item_id</i>.</td><td>Decomposed into <b>menu_items</b> (keyed by <i>item_id</i>) and <b>order_items</b> (storing quantity and historical snapshot <i>unit_price</i>).</td></tr>" +
            "<tr><td><b>3NF (Third Normal Form)</b></td><td>Transitive dependencies: <i>order_id → customer_id → customer_details</i> and <i>order_id → restaurant_id → restaurant_details</i>.</td><td>Decomposed into separate <b>customers</b> and <b>restaurants</b> tables. Separated <b>payments</b> (1-to-1) and <b>order_status_logs</b> (1-to-many).</td></tr>" +
            "<tr><td><b>BCNF (Boyce-Codd)</b></td><td>Non-superkey determinants</td><td>Every determinant across all 7 relations is a Candidate Key / Superkey. Strict BCNF compliance achieved.</td></tr>" +
            "</table>" +
            "</body></html>"
        ));
        container.add(Box.createVerticalStrut(14));

        // Card 2: 7 Decomposed Tables Schema
        container.add(createCard(
            "2. Normalized 7-Table Relational Schema",
            "<html><body style='width: 720px; font-family: Segoe UI; font-size: 11pt; color: #1E293B;'>" +
            "<ul style='margin-left: 15px;'>" +
            "<li><b>customers</b> (<u>customer_id</u>, full_name, email [UNIQUE], phone, address, created_at)</li>" +
            "<li><b>restaurants</b> (<u>restaurant_id</u>, name, cuisine_type, address, phone, rating, is_active, created_at)</li>" +
            "<li><b>menu_items</b> (<u>item_id</u>, restaurant_id [FK], name, description, category, price, is_available, stock_quantity)</li>" +
            "<li><b>orders</b> (<u>order_id</u>, customer_id [FK], restaurant_id [FK], order_date, total_amount, status, delivery_address, special_instructions)</li>" +
            "<li><b>order_items</b> (<u>order_item_id</u>, order_id [FK], item_id [FK], quantity, unit_price, subtotal)</li>" +
            "<li><b>payments</b> (<u>payment_id</u>, order_id [FK UNIQUE], payment_date, amount, payment_method, payment_status, transaction_reference)</li>" +
            "<li><b>order_status_logs</b> (<u>log_id</u>, order_id [FK], status, remarks, updated_at)</li>" +
            "</ul>" +
            "</body></html>"
        ));
        container.add(Box.createVerticalStrut(14));

        // Card 3: Prevention of Invalid Transactions
        container.add(createCard(
            "3. Prevention of Invalid Transactions & ACID Guarantees",
            "<html><body style='width: 720px; font-family: Segoe UI; font-size: 11pt; color: #1E293B;'>" +
            "<p><b>RDBMS MySQL / MariaDB Constraints:</b></p>" +
            "<ul>" +
            "<li><b>Engine InnoDB:</b> Enforces row-level locking, foreign keys, and write-ahead transaction redo logging.</li>" +
            "<li><b>Stock Depletion Check:</b> <code>CHECK (stock_quantity &gt;= 0)</code> and atomic conditional decrement <code>WHERE stock_quantity &gt;= ?</code> prevent overselling.</li>" +
            "<li><b>Payment Uniqueness:</b> <code>UNIQUE(order_id)</code> on <b>payments</b> table prevents duplicate settlement or double charges.</li>" +
            "<li><b>Quantity Positivity:</b> <code>CHECK (quantity &gt; 0)</code> and <code>CHECK (total_amount &gt;= 0)</code> prevent zero or negative order corruptions.</li>" +
            "<li><b>Integrity Cascades:</b> <code>ON DELETE RESTRICT</code> on customers, restaurants, and menu items prevents accidental deletion of entities tied to active order ledgers.</li>" +
            "</ul>" +
            "<p><b>Atomic Multi-Table JDBC Protocol:</b></p>" +
            "<p>Orders execute with <code>conn.setAutoCommit(false)</code>. If stock reservation, order row creation, line items batch insertion, or payment settlement fails, <code>conn.rollback()</code> is triggered immediately. No orphan records or partial stock deductions can occur.</p>" +
            "<p><b>Cancellation & Inventory Refund Invariant:</b></p>" +
            "<p>When an order is cancelled, a transaction verifies status is cancellable (<i>PENDING/CONFIRMED/PREPARING</i>), restores line-item quantities back to <b>menu_items.stock_quantity</b>, marks payment as <i>REFUNDED</i>, and logs the cancellation event.</p>" +
            "</body></html>"
        ));

        JScrollPane scrollPane = new JScrollPane(container);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    private CardPanel createCard(String title, String htmlBody) {
        CardPanel card = new CardPanel(new BorderLayout(8, 8), 16);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UITheme.FONT_SUBHEADER);
        titleLbl.setForeground(UITheme.PRIMARY_DARK);
        card.add(titleLbl);
        card.add(Box.createVerticalStrut(10));

        JLabel contentLbl = new JLabel(htmlBody);
        card.add(contentLbl);

        return card;
    }
}
