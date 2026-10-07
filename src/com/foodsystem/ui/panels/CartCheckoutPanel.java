package com.foodsystem.ui.panels;

import com.foodsystem.models.CartItem;
import com.foodsystem.models.Customer;
import com.foodsystem.models.Order;
import com.foodsystem.models.enums.OrderStatus;
import com.foodsystem.models.enums.PaymentMethod;
import com.foodsystem.service.CartManager;
import com.foodsystem.service.OrderService;
import com.foodsystem.ui.components.CardPanel;
import com.foodsystem.ui.components.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Consumer;

public class CartCheckoutPanel extends JPanel {

    private final CartManager cartManager = CartManager.getInstance();
    private final OrderService orderService = OrderService.getInstance();

    private final Consumer<Integer> onOrderPlacedSuccess;
    private final Runnable onBrowseRestaurants;

    private JPanel emptyCartPanel;
    private JPanel activeCartPanel;

    private JLabel restaurantNameLabel;
    private JTable cartTable;
    private DefaultTableModel tableModel;

    private JLabel subtotalLabel;
    private JLabel deliveryFeeLabel;
    private JLabel taxLabel;
    private JLabel grandTotalLabel;

    private JTextArea addressArea;
    private JTextField instructionsField;
    private JComboBox<PaymentMethod> paymentMethodCombo;
    private JPanel paymentDetailsCard;
    private JTextField paymentInputPrimary;
    private JTextField paymentInputSecondary;

    private JButton placeOrderBtn;

    public CartCheckoutPanel(Consumer<Integer> onOrderPlacedSuccess, Runnable onBrowseRestaurants) {
        this.onOrderPlacedSuccess = onOrderPlacedSuccess;
        this.onBrowseRestaurants = onBrowseRestaurants;

        setLayout(new CardLayout());
        setBackground(UITheme.BG_LIGHT);
        setBorder(new EmptyBorder(12, 16, 16, 16));

        initUI();

        cartManager.addListener(this::refreshCartView);
        refreshCartView();
    }

    private void initUI() {
        // 1. Empty Cart Card
        emptyCartPanel = new JPanel(new GridBagLayout());
        emptyCartPanel.setBackground(UITheme.BG_LIGHT);

        JPanel emptyContent = new JPanel();
        emptyContent.setLayout(new BoxLayout(emptyContent, BoxLayout.Y_AXIS));
        emptyContent.setOpaque(false);

        JLabel iconLabel = new JLabel("🛒", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 64));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("Your Cart is Empty", SwingConstants.CENTER);
        titleLabel.setFont(UITheme.FONT_HEADER);
        titleLabel.setForeground(UITheme.TEXT_DARK);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Browse available restaurants and add your favorite dishes!", SwingConstants.CENTER);
        subtitleLabel.setFont(UITheme.FONT_BODY);
        subtitleLabel.setForeground(UITheme.TEXT_MUTED);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton browseBtn = UITheme.createPrimaryButton("Browse Restaurants");
        browseBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        browseBtn.addActionListener(e -> {
            if (onBrowseRestaurants != null) {
                onBrowseRestaurants.run();
            }
        });

        emptyContent.add(iconLabel);
        emptyContent.add(Box.createVerticalStrut(12));
        emptyContent.add(titleLabel);
        emptyContent.add(Box.createVerticalStrut(6));
        emptyContent.add(subtitleLabel);
        emptyContent.add(Box.createVerticalStrut(18));
        emptyContent.add(browseBtn);

        emptyCartPanel.add(emptyContent);
        add(emptyCartPanel, "EMPTY");

        // 2. Active Cart Card
        activeCartPanel = new JPanel(new BorderLayout(16, 16));
        activeCartPanel.setBackground(UITheme.BG_LIGHT);

        // Left Panel: Items List
        JPanel leftPanel = new JPanel(new BorderLayout(10, 10));
        leftPanel.setOpaque(false);

        // Top info bar
        JPanel topBar = new JPanel(new BorderLayout(8, 0));
        topBar.setOpaque(false);

        restaurantNameLabel = new JLabel("Ordering from: ");
        restaurantNameLabel.setFont(UITheme.FONT_SUBHEADER);
        restaurantNameLabel.setForeground(UITheme.TEXT_DARK);

        JButton clearBtn = UITheme.createSecondaryButton("🗑️ Clear Cart");
        clearBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                this, "Are you sure you want to empty your cart?", "Clear Cart",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE
            );
            if (confirm == JOptionPane.YES_OPTION) {
                cartManager.clearCart();
            }
        });

        topBar.add(restaurantNameLabel, BorderLayout.WEST);
        topBar.add(clearBtn, BorderLayout.EAST);
        leftPanel.add(topBar, BorderLayout.NORTH);

        // Cart Table
        String[] columnNames = {"#", "Item Name", "Price", "Qty", "Subtotal", "Actions"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Handled via action buttons below
            }
        };

        cartTable = new JTable(tableModel);
        cartTable.setRowHeight(36);
        cartTable.setFont(UITheme.FONT_BODY);
        cartTable.getTableHeader().setFont(UITheme.FONT_BODY_BOLD);
        cartTable.getTableHeader().setBackground(new Color(241, 245, 249));
        cartTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Column widths
        cartTable.getColumnModel().getColumn(0).setPreferredWidth(35);
        cartTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        cartTable.getColumnModel().getColumn(2).setPreferredWidth(70);
        cartTable.getColumnModel().getColumn(3).setPreferredWidth(50);
        cartTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        cartTable.getColumnModel().getColumn(5).setPreferredWidth(100);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        cartTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        cartTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

        JScrollPane tableScroll = new JScrollPane(cartTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));
        leftPanel.add(tableScroll, BorderLayout.CENTER);

        // Item manipulation toolbar under table
        JPanel tableToolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        tableToolbar.setOpaque(false);

        JButton increaseBtn = UITheme.createSecondaryButton("➕ Increase Qty");
        increaseBtn.addActionListener(e -> modifySelectedQuantity(1));

        JButton decreaseBtn = UITheme.createSecondaryButton("➖ Decrease Qty");
        decreaseBtn.addActionListener(e -> modifySelectedQuantity(-1));

        JButton removeBtn = UITheme.createDangerButton("✕ Remove Item");
        removeBtn.addActionListener(e -> {
            int selectedRow = cartTable.getSelectedRow();
            if (selectedRow >= 0) {
                CartItem item = cartManager.getItems().get(selectedRow);
                cartManager.removeItem(item.getMenuItem().getItemId());
            } else {
                JOptionPane.showMessageDialog(this, "Please select an item from the table to remove.", "Selection", JOptionPane.WARNING_MESSAGE);
            }
        });

        tableToolbar.add(increaseBtn);
        tableToolbar.add(decreaseBtn);
        tableToolbar.add(removeBtn);
        leftPanel.add(tableToolbar, BorderLayout.SOUTH);

        activeCartPanel.add(leftPanel, BorderLayout.CENTER);

        // Right Panel: Order Summary & Checkout
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setPreferredSize(new Dimension(380, 500));
        rightPanel.setBackground(UITheme.BG_LIGHT);

        // Summary Card
        CardPanel summaryCard = new CardPanel(new BorderLayout(10, 10), 16);
        summaryCard.setLayout(new BoxLayout(summaryCard, BoxLayout.Y_AXIS));

        JLabel summaryTitle = new JLabel("Order Summary");
        summaryTitle.setFont(UITheme.FONT_SUBHEADER);
        summaryTitle.setForeground(UITheme.TEXT_DARK);
        summaryCard.add(summaryTitle);
        summaryCard.add(Box.createVerticalStrut(12));

        // Subtotal row
        subtotalLabel = new JLabel("$0.00", SwingConstants.RIGHT);
        summaryCard.add(createSummaryRow("Items Subtotal:", subtotalLabel));
        summaryCard.add(Box.createVerticalStrut(6));

        // Delivery fee row
        deliveryFeeLabel = new JLabel("$3.99", SwingConstants.RIGHT);
        summaryCard.add(createSummaryRow("Delivery Fee:", deliveryFeeLabel));
        summaryCard.add(Box.createVerticalStrut(6));

        // Tax row
        taxLabel = new JLabel("$0.00", SwingConstants.RIGHT);
        summaryCard.add(createSummaryRow("Estimated Taxes (5%):", taxLabel));
        summaryCard.add(Box.createVerticalStrut(10));

        JSeparator sep = new JSeparator();
        sep.setForeground(UITheme.BORDER);
        summaryCard.add(sep);
        summaryCard.add(Box.createVerticalStrut(10));

        // Grand Total row
        grandTotalLabel = new JLabel("$0.00", SwingConstants.RIGHT);
        grandTotalLabel.setFont(UITheme.FONT_PRICE);
        grandTotalLabel.setForeground(UITheme.PRIMARY_DARK);

        JPanel totalRow = new JPanel(new BorderLayout());
        totalRow.setOpaque(false);
        JLabel totalText = new JLabel("Total Payable:");
        totalText.setFont(UITheme.FONT_BODY_BOLD);
        totalRow.add(totalText, BorderLayout.WEST);
        totalRow.add(grandTotalLabel, BorderLayout.EAST);
        summaryCard.add(totalRow);

        rightPanel.add(summaryCard);
        rightPanel.add(Box.createVerticalStrut(12));

        // Checkout Details Card
        CardPanel checkoutCard = new CardPanel(new BorderLayout(8, 8), 14);
        checkoutCard.setLayout(new BoxLayout(checkoutCard, BoxLayout.Y_AXIS));

        JLabel checkoutTitle = new JLabel("Delivery & Payment");
        checkoutTitle.setFont(UITheme.FONT_BODY_BOLD);
        checkoutTitle.setForeground(UITheme.TEXT_DARK);
        checkoutCard.add(checkoutTitle);
        checkoutCard.add(Box.createVerticalStrut(8));

        // Delivery Address
        JLabel addrLbl = new JLabel("Delivery Destination:");
        addrLbl.setFont(UITheme.FONT_SMALL);
        addrLbl.setForeground(UITheme.TEXT_MUTED);
        checkoutCard.add(addrLbl);
        checkoutCard.add(Box.createVerticalStrut(4));

        addressArea = new JTextArea(2, 20);
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);
        addressArea.setFont(UITheme.FONT_BODY);
        JScrollPane addrScroll = new JScrollPane(addressArea);
        addrScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));
        checkoutCard.add(addrScroll);
        checkoutCard.add(Box.createVerticalStrut(8));

        // Special Instructions
        JLabel instrLbl = new JLabel("Special Instructions (optional):");
        instrLbl.setFont(UITheme.FONT_SMALL);
        instrLbl.setForeground(UITheme.TEXT_MUTED);
        checkoutCard.add(instrLbl);
        checkoutCard.add(Box.createVerticalStrut(4));

        instructionsField = new JTextField();
        instructionsField.setFont(UITheme.FONT_BODY);
        instructionsField.putClientProperty("JTextField.placeholderText", "e.g. Ring bell, leave at door...");
        instructionsField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER),
            new EmptyBorder(4, 6, 4, 6)
        ));
        checkoutCard.add(instructionsField);
        checkoutCard.add(Box.createVerticalStrut(8));

        // Payment Method
        JLabel payLbl = new JLabel("Payment Method:");
        payLbl.setFont(UITheme.FONT_SMALL);
        payLbl.setForeground(UITheme.TEXT_MUTED);
        checkoutCard.add(payLbl);
        checkoutCard.add(Box.createVerticalStrut(4));

        paymentMethodCombo = new JComboBox<>(PaymentMethod.values());
        paymentMethodCombo.setFont(UITheme.FONT_BODY);
        paymentMethodCombo.addActionListener(e -> updatePaymentFields());
        checkoutCard.add(paymentMethodCombo);
        checkoutCard.add(Box.createVerticalStrut(8));

        // Payment Detail Inputs
        paymentDetailsCard = new JPanel();
        paymentDetailsCard.setLayout(new BoxLayout(paymentDetailsCard, BoxLayout.Y_AXIS));
        paymentDetailsCard.setOpaque(false);

        paymentInputPrimary = new JTextField();
        paymentInputPrimary.setFont(UITheme.FONT_BODY);
        paymentInputPrimary.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER),
            new EmptyBorder(4, 6, 4, 6)
        ));

        paymentInputSecondary = new JTextField();
        paymentInputSecondary.setFont(UITheme.FONT_BODY);
        paymentInputSecondary.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER),
            new EmptyBorder(4, 6, 4, 6)
        ));

        paymentDetailsCard.add(paymentInputPrimary);
        paymentDetailsCard.add(Box.createVerticalStrut(4));
        paymentDetailsCard.add(paymentInputSecondary);
        checkoutCard.add(paymentDetailsCard);
        checkoutCard.add(Box.createVerticalStrut(14));

        // Place Order Button
        placeOrderBtn = UITheme.createPrimaryButton("🔒 Place Order with JDBC Transaction");
        placeOrderBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        placeOrderBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        placeOrderBtn.addActionListener(e -> executeOrderPlacement());
        checkoutCard.add(placeOrderBtn);

        rightPanel.add(checkoutCard);
        activeCartPanel.add(rightPanel, BorderLayout.EAST);

        add(activeCartPanel, "ACTIVE");

        updatePaymentFields();
    }

    private JPanel createSummaryRow(String title, JLabel valueLabel) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        JLabel lbl = new JLabel(title);
        lbl.setFont(UITheme.FONT_BODY);
        lbl.setForeground(UITheme.TEXT_MUTED);
        valueLabel.setFont(UITheme.FONT_BODY_BOLD);
        valueLabel.setForeground(UITheme.TEXT_DARK);
        row.add(lbl, BorderLayout.WEST);
        row.add(valueLabel, BorderLayout.EAST);
        return row;
    }

    private void modifySelectedQuantity(int delta) {
        int selectedRow = cartTable.getSelectedRow();
        if (selectedRow >= 0) {
            CartItem item = cartManager.getItems().get(selectedRow);
            int newQty = item.getQuantity() + delta;
            cartManager.updateQuantity(item.getMenuItem().getItemId(), newQty);
        } else {
            JOptionPane.showMessageDialog(this, "Please select an item row from the table first.", "Select Item", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void updatePaymentFields() {
        PaymentMethod method = (PaymentMethod) paymentMethodCombo.getSelectedItem();
        if (method == null) return;

        paymentInputPrimary.setVisible(true);
        paymentInputSecondary.setVisible(false);

        switch (method) {
            case CREDIT_CARD:
            case DEBIT_CARD:
                paymentInputPrimary.putClientProperty("JTextField.placeholderText", "Card Number (e.g. 4532 8901 2345 6789)");
                paymentInputPrimary.setText("4532-8901-2345-6789");
                paymentInputSecondary.setVisible(true);
                paymentInputSecondary.putClientProperty("JTextField.placeholderText", "MM/YY  |  CVV");
                paymentInputSecondary.setText("12/28  |  321");
                break;
            case UPI:
                paymentInputPrimary.putClientProperty("JTextField.placeholderText", "Enter UPI ID (e.g. yourname@okhdfcbank)");
                paymentInputPrimary.setText("customer@upi");
                break;
            case NET_BANKING:
                paymentInputPrimary.putClientProperty("JTextField.placeholderText", "Select Bank (HDFC / ICICI / Chase)");
                paymentInputPrimary.setText("Chase Premier Banking");
                break;
            case CASH_ON_DELIVERY:
                paymentInputPrimary.setVisible(false);
                break;
        }

        paymentDetailsCard.revalidate();
        paymentDetailsCard.repaint();
    }

    public void refreshCartView() {
        CardLayout cl = (CardLayout) getLayout();
        List<CartItem> items = cartManager.getItems();

        if (items.isEmpty()) {
            cl.show(this, "EMPTY");
            return;
        }

        cl.show(this, "ACTIVE");

        if (cartManager.getCurrentRestaurant() != null) {
            restaurantNameLabel.setText("Ordering from: " + cartManager.getCurrentRestaurant().getName() + " (" + cartManager.getCurrentRestaurant().getCuisineType() + ")");
        }

        // Populate table
        tableModel.setRowCount(0);
        int idx = 1;
        for (CartItem ci : items) {
            tableModel.addRow(new Object[]{
                idx++,
                ci.getMenuItem().getName() + " [" + ci.getMenuItem().getCategory() + "]",
                UITheme.formatCurrency(ci.getUnitPrice()),
                ci.getQuantity(),
                UITheme.formatCurrency(ci.getSubtotal()),
                "Stock: " + ci.getMenuItem().getStockQuantity()
            });
        }

        // Financial figures
        subtotalLabel.setText(UITheme.formatCurrency(cartManager.getSubtotal()));
        BigDecimal fee = cartManager.getDeliveryFee();
        if (fee.compareTo(BigDecimal.ZERO) == 0) {
            deliveryFeeLabel.setText("FREE ($35+ bonus)");
            deliveryFeeLabel.setForeground(UITheme.SUCCESS);
        } else {
            deliveryFeeLabel.setText(UITheme.formatCurrency(fee));
            deliveryFeeLabel.setForeground(UITheme.TEXT_DARK);
        }

        taxLabel.setText(UITheme.formatCurrency(cartManager.getTax()));
        grandTotalLabel.setText(UITheme.formatCurrency(cartManager.getGrandTotal()));

        // Populate address from active customer if empty
        Customer activeCust = orderService.getActiveCustomer();
        if (activeCust != null && (addressArea.getText() == null || addressArea.getText().trim().isEmpty())) {
            addressArea.setText(activeCust.getAddress());
        }
    }

    public void updateCustomerAddress(String address) {
        if (addressArea != null && (addressArea.getText() == null || addressArea.getText().trim().isEmpty())) {
            addressArea.setText(address);
        }
    }

    private void executeOrderPlacement() {
        List<CartItem> items = cartManager.getItems();
        if (items.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cart is empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Customer activeCustomer = orderService.getActiveCustomer();
        if (activeCustomer == null) {
            JOptionPane.showMessageDialog(this, "Please select an active customer profile in the top bar.", "No Customer", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String deliveryAddress = addressArea.getText().trim();
        if (deliveryAddress.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please provide a valid delivery destination address.", "Validation", JOptionPane.WARNING_MESSAGE);
            addressArea.requestFocus();
            return;
        }

        PaymentMethod method = (PaymentMethod) paymentMethodCombo.getSelectedItem();
        String instructions = instructionsField.getText().trim();

        Order order = new Order();
        order.setCustomerId(activeCustomer.getCustomerId());
        order.setRestaurantId(cartManager.getCurrentRestaurant().getRestaurantId());
        order.setTotalAmount(cartManager.getGrandTotal());
        order.setStatus(OrderStatus.PENDING);
        order.setDeliveryAddress(deliveryAddress);
        order.setSpecialInstructions(instructions);

        placeOrderBtn.setEnabled(false);
        placeOrderBtn.setText("⏳ Executing ACID JDBC Transaction...");

        SwingWorker<Integer, Void> worker = new SwingWorker<>() {
            private String errorMessage = null;

            @Override
            protected Integer doInBackground() {
                try {
                    return orderService.placeOrder(order, items, method);
                } catch (Exception ex) {
                    errorMessage = ex.getMessage();
                    ex.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void done() {
                placeOrderBtn.setEnabled(true);
                placeOrderBtn.setText("🔒 Place Order with JDBC Transaction");

                try {
                    Integer orderId = get();
                    if (orderId != null && orderId > 0) {
                        // Success modal
                        cartManager.clearCart();
                        showOrderSuccessModal(orderId, order.getTotalAmount(), method);

                        if (onOrderPlacedSuccess != null) {
                            onOrderPlacedSuccess.accept(orderId);
                        }
                    } else {
                        JOptionPane.showMessageDialog(
                            CartCheckoutPanel.this,
                            "<html><body style='width: 320px;'>" +
                            "<b>Transaction Aborted & Rolled Back!</b><br><br>" +
                            (errorMessage != null ? errorMessage : "An unknown error occurred during transaction processing.") +
                            "<br><br><i>No charges were deducted and menu stock remained unchanged.</i>" +
                            "</body></html>",
                            "Transaction Failed",
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(CartCheckoutPanel.this, "Error executing order: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    private void showOrderSuccessModal(int orderId, BigDecimal total, PaymentMethod method) {
        String msg = "<html><body style='width: 350px; padding: 6px;'>" +
                "<h2 style='color: #10B981;'>🎉 Order Placed Successfully!</h2>" +
                "<p>Your order has been recorded into <b>MySQL (InnoDB)</b> with full transaction atomicity.</p>" +
                "<table style='margin-top: 8px; width: 100%; border-collapse: collapse;'>" +
                "<tr><td><b>Order ID:</b></td><td>#" + orderId + "</td></tr>" +
                "<tr><td><b>Amount Settled:</b></td><td>" + UITheme.formatCurrency(total) + "</td></tr>" +
                "<tr><td><b>Payment Method:</b></td><td>" + method.getDisplayName() + "</td></tr>" +
                "<tr><td><b>Status:</b></td><td><span style='color: #F59E0B;'>PENDING (Confirmed by Kitchen)</span></td></tr>" +
                "<tr><td><b>Estimated Delivery:</b></td><td>30 - 40 mins</td></tr>" +
                "</table><br>" +
                "<i>Now redirecting you to live Order Tracking...</i>" +
                "</body></html>";

        JOptionPane.showMessageDialog(this, msg, "Order Confirmed #" + orderId, JOptionPane.INFORMATION_MESSAGE);
    }
}
