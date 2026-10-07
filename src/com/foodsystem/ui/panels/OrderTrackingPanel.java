package com.foodsystem.ui.panels;

import com.foodsystem.models.Order;
import com.foodsystem.models.OrderItem;
import com.foodsystem.models.OrderStatusLog;
import com.foodsystem.models.Payment;
import com.foodsystem.models.enums.OrderStatus;
import com.foodsystem.service.OrderService;
import com.foodsystem.ui.components.CardPanel;
import com.foodsystem.ui.components.OrderStepProgressBar;
import com.foodsystem.ui.components.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

public class OrderTrackingPanel extends JPanel {

    private final OrderService orderService = OrderService.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm");

    private JTable ordersTable;
    private DefaultTableModel ordersTableModel;
    private List<Order> currentOrders;

    private JPanel detailContainer;
    private OrderStepProgressBar progressBar;

    private JLabel orderHeaderLabel;
    private JLabel restaurantDetailsLabel;
    private JLabel deliveryAddressLabel;
    private JLabel orderTotalLabel;

    private DefaultTableModel itemsTableModel;
    private DefaultTableModel timelineTableModel;

    private JLabel paymentMethodLabel;
    private JLabel paymentStatusLabel;
    private JLabel paymentTxnLabel;

    private JButton advanceStatusBtn;
    private JButton cancelOrderBtn;
    private JButton printReceiptBtn;

    private Order selectedOrder;

    public OrderTrackingPanel() {
        setLayout(new BorderLayout(14, 14));
        setBackground(UITheme.BG_LIGHT);
        setBorder(new EmptyBorder(12, 16, 16, 16));

        initUI();
        loadOrders();
    }

    private void initUI() {
        // Main split pane: Left is Orders List, Right is Live Tracker & Audit Details
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(360);
        splitPane.setDividerSize(4);
        splitPane.setContinuousLayout(true);
        splitPane.setBorder(null);

        // Left Panel: Orders List
        JPanel leftPanel = new JPanel(new BorderLayout(8, 8));
        leftPanel.setBackground(UITheme.BG_LIGHT);

        JPanel leftHeader = new JPanel(new BorderLayout());
        leftHeader.setOpaque(false);

        JLabel leftTitle = new JLabel("My Orders History");
        leftTitle.setFont(UITheme.FONT_SUBHEADER);
        leftTitle.setForeground(UITheme.TEXT_DARK);

        JButton refreshBtn = UITheme.createSecondaryButton("🔄 Refresh");
        refreshBtn.addActionListener(e -> loadOrders());

        leftHeader.add(leftTitle, BorderLayout.WEST);
        leftHeader.add(refreshBtn, BorderLayout.EAST);
        leftPanel.add(leftHeader, BorderLayout.NORTH);

        String[] orderColumns = {"#", "Restaurant", "Total", "Status"};
        ordersTableModel = new DefaultTableModel(orderColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        ordersTable = new JTable(ordersTableModel);
        ordersTable.setRowHeight(36);
        ordersTable.setFont(UITheme.FONT_BODY);
        ordersTable.getTableHeader().setFont(UITheme.FONT_BODY_BOLD);
        ordersTable.getTableHeader().setBackground(new Color(241, 245, 249));
        ordersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        ordersTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        ordersTable.getColumnModel().getColumn(1).setPreferredWidth(140);
        ordersTable.getColumnModel().getColumn(2).setPreferredWidth(65);
        ordersTable.getColumnModel().getColumn(3).setPreferredWidth(90);

        DefaultTableCellRenderer centerRender = new DefaultTableCellRenderer();
        centerRender.setHorizontalAlignment(JLabel.CENTER);
        ordersTable.getColumnModel().getColumn(0).setCellRenderer(centerRender);
        ordersTable.getColumnModel().getColumn(3).setCellRenderer(centerRender);

        ordersTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = ordersTable.getSelectedRow();
                if (row >= 0 && currentOrders != null && row < currentOrders.size()) {
                    selectOrder(currentOrders.get(row).getOrderId());
                }
            }
        });

        JScrollPane ordersScroll = new JScrollPane(ordersTable);
        ordersScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));
        leftPanel.add(ordersScroll, BorderLayout.CENTER);
        splitPane.setLeftComponent(leftPanel);

        // Right Panel: Live Tracker & Timeline
        detailContainer = new JPanel(new BorderLayout(10, 10));
        detailContainer.setBackground(UITheme.BG_LIGHT);

        JPanel detailsScrollContent = new JPanel();
        detailsScrollContent.setLayout(new BoxLayout(detailsScrollContent, BoxLayout.Y_AXIS));
        detailsScrollContent.setBackground(UITheme.BG_LIGHT);

        // Top Tracker Card: Step Progress Bar
        CardPanel trackerCard = new CardPanel(new BorderLayout(8, 8), 14);
        trackerCard.setLayout(new BoxLayout(trackerCard, BoxLayout.Y_AXIS));

        orderHeaderLabel = new JLabel("Select an order to track");
        orderHeaderLabel.setFont(UITheme.FONT_HEADER);
        orderHeaderLabel.setForeground(UITheme.TEXT_DARK);
        trackerCard.add(orderHeaderLabel);
        trackerCard.add(Box.createVerticalStrut(4));

        restaurantDetailsLabel = new JLabel("");
        restaurantDetailsLabel.setFont(UITheme.FONT_BODY);
        restaurantDetailsLabel.setForeground(UITheme.TEXT_MUTED);
        trackerCard.add(restaurantDetailsLabel);
        trackerCard.add(Box.createVerticalStrut(4));

        deliveryAddressLabel = new JLabel("");
        deliveryAddressLabel.setFont(UITheme.FONT_BODY);
        deliveryAddressLabel.setForeground(UITheme.TEXT_MUTED);
        trackerCard.add(deliveryAddressLabel);
        trackerCard.add(Box.createVerticalStrut(12));

        progressBar = new OrderStepProgressBar();
        trackerCard.add(progressBar);
        trackerCard.add(Box.createVerticalStrut(8));

        // Action Toolbar
        JPanel actionToolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        actionToolbar.setOpaque(false);

        advanceStatusBtn = UITheme.createPrimaryButton("⚡ Advance Next Status");
        advanceStatusBtn.addActionListener(e -> advanceCurrentOrderStatus());

        cancelOrderBtn = UITheme.createDangerButton("🛑 Cancel Order & Refund");
        cancelOrderBtn.addActionListener(e -> cancelCurrentOrder());

        printReceiptBtn = UITheme.createSecondaryButton("🖨️ View Receipt");
        printReceiptBtn.addActionListener(e -> showPrintableReceipt());

        actionToolbar.add(printReceiptBtn);
        actionToolbar.add(cancelOrderBtn);
        actionToolbar.add(advanceStatusBtn);
        trackerCard.add(actionToolbar);

        detailsScrollContent.add(trackerCard);
        detailsScrollContent.add(Box.createVerticalStrut(12));

        // Two Column middle section: Order Items and Payment Details
        JPanel middleRow = new JPanel(new GridLayout(1, 2, 12, 0));
        middleRow.setOpaque(false);

        // Items Card
        CardPanel itemsCard = new CardPanel(new BorderLayout(6, 6), 12);
        JLabel itemsTitle = new JLabel("Order Line Items");
        itemsTitle.setFont(UITheme.FONT_SUBHEADER);
        itemsCard.add(itemsTitle, BorderLayout.NORTH);

        String[] itemCols = {"Item", "Qty", "Price", "Subtotal"};
        itemsTableModel = new DefaultTableModel(itemCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable itemsTable = new JTable(itemsTableModel);
        itemsTable.setRowHeight(28);
        itemsTable.setFont(UITheme.FONT_BODY);
        JScrollPane itemsScroll = new JScrollPane(itemsTable);
        itemsScroll.setPreferredSize(new Dimension(280, 150));
        itemsScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));
        itemsCard.add(itemsScroll, BorderLayout.CENTER);

        orderTotalLabel = new JLabel("Total: $0.00", SwingConstants.RIGHT);
        orderTotalLabel.setFont(UITheme.FONT_BODY_BOLD);
        orderTotalLabel.setForeground(UITheme.PRIMARY_DARK);
        itemsCard.add(orderTotalLabel, BorderLayout.SOUTH);

        middleRow.add(itemsCard);

        // Payment Info Card
        CardPanel paymentCard = new CardPanel(new BorderLayout(8, 8), 12);
        paymentCard.setLayout(new BoxLayout(paymentCard, BoxLayout.Y_AXIS));

        JLabel payTitle = new JLabel("Financial Settlement (InnoDB)");
        payTitle.setFont(UITheme.FONT_SUBHEADER);
        paymentCard.add(payTitle);
        paymentCard.add(Box.createVerticalStrut(10));

        paymentMethodLabel = new JLabel("Payment Method: -");
        paymentMethodLabel.setFont(UITheme.FONT_BODY);
        paymentCard.add(paymentMethodLabel);
        paymentCard.add(Box.createVerticalStrut(6));

        paymentStatusLabel = new JLabel("Settlement Status: -");
        paymentStatusLabel.setFont(UITheme.FONT_BODY_BOLD);
        paymentCard.add(paymentStatusLabel);
        paymentCard.add(Box.createVerticalStrut(6));

        paymentTxnLabel = new JLabel("Transaction Ref: -");
        paymentTxnLabel.setFont(UITheme.FONT_SMALL);
        paymentTxnLabel.setForeground(UITheme.TEXT_MUTED);
        paymentCard.add(paymentTxnLabel);
        paymentCard.add(Box.createVerticalStrut(10));

        JLabel auditNote = new JLabel("<html><body style='width: 200px; color: #64748B; font-size: 10px;'>" +
                "Guaranteed by MySQL UNIQUE constraint on order_id and InnoDB atomic commits.</body></html>");
        paymentCard.add(auditNote);

        middleRow.add(paymentCard);
        detailsScrollContent.add(middleRow);
        detailsScrollContent.add(Box.createVerticalStrut(12));

        // Chronological Audit Timeline Card
        CardPanel timelineCard = new CardPanel(new BorderLayout(6, 6), 12);
        JLabel timelineTitle = new JLabel("Chronological Order Audit Trail (order_status_logs)");
        timelineTitle.setFont(UITheme.FONT_SUBHEADER);
        timelineCard.add(timelineTitle, BorderLayout.NORTH);

        String[] logCols = {"Timestamp", "Status", "Activity / Event Remarks"};
        timelineTableModel = new DefaultTableModel(logCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable timelineTable = new JTable(timelineTableModel);
        timelineTable.setRowHeight(28);
        timelineTable.setFont(UITheme.FONT_BODY);
        timelineTable.getColumnModel().getColumn(0).setPreferredWidth(140);
        timelineTable.getColumnModel().getColumn(1).setPreferredWidth(90);
        timelineTable.getColumnModel().getColumn(2).setPreferredWidth(300);

        JScrollPane timelineScroll = new JScrollPane(timelineTable);
        timelineScroll.setPreferredSize(new Dimension(500, 160));
        timelineScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));
        timelineCard.add(timelineScroll, BorderLayout.CENTER);

        detailsScrollContent.add(timelineCard);

        JScrollPane rightScroll = new JScrollPane(detailsScrollContent);
        rightScroll.setBorder(null);
        rightScroll.getVerticalScrollBar().setUnitIncrement(16);
        detailContainer.add(rightScroll, BorderLayout.CENTER);

        splitPane.setRightComponent(detailContainer);
        add(splitPane, BorderLayout.CENTER);
    }

    public void loadOrders() {
        currentOrders = orderService.getOrdersForActiveCustomer();
        ordersTableModel.setRowCount(0);

        for (Order o : currentOrders) {
            ordersTableModel.addRow(new Object[]{
                "#" + o.getOrderId(),
                o.getRestaurantName(),
                UITheme.formatCurrency(o.getTotalAmount()),
                o.getStatus().getDisplayName()
            });
        }

        if (!currentOrders.isEmpty()) {
            if (selectedOrder == null) {
                ordersTable.setRowSelectionInterval(0, 0);
            } else {
                boolean found = false;
                for (int i = 0; i < currentOrders.size(); i++) {
                    if (currentOrders.get(i).getOrderId() == selectedOrder.getOrderId()) {
                        ordersTable.setRowSelectionInterval(i, i);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    ordersTable.setRowSelectionInterval(0, 0);
                }
            }
        } else {
            clearDetailView();
        }
    }

    public void selectOrder(int orderId) {
        Order fullOrder = orderService.getOrderById(orderId);
        if (fullOrder == null) return;
        this.selectedOrder = fullOrder;

        orderHeaderLabel.setText("Order #" + fullOrder.getOrderId() + "  (" + dateFormat.format(fullOrder.getOrderDate()) + ")");
        restaurantDetailsLabel.setText("🍽️ " + fullOrder.getRestaurantName() + "  |  📞 " + fullOrder.getRestaurantPhone());
        deliveryAddressLabel.setText("📍 Deliver to: " + fullOrder.getDeliveryAddress() +
                (fullOrder.getSpecialInstructions() != null && !fullOrder.getSpecialInstructions().isEmpty() ?
                        "  |  Note: \"" + fullOrder.getSpecialInstructions() + "\"" : ""));

        progressBar.setStatus(fullOrder.getStatus());

        // Update Buttons state
        advanceStatusBtn.setEnabled(fullOrder.getStatus() != OrderStatus.DELIVERED && fullOrder.getStatus() != OrderStatus.CANCELLED);
        cancelOrderBtn.setEnabled(fullOrder.getStatus().canBeCancelled());

        // Populate items
        itemsTableModel.setRowCount(0);
        for (OrderItem oi : fullOrder.getItems()) {
            itemsTableModel.addRow(new Object[]{
                oi.getItemName(),
                oi.getQuantity(),
                UITheme.formatCurrency(oi.getUnitPrice()),
                UITheme.formatCurrency(oi.getSubtotal())
            });
        }
        orderTotalLabel.setText("Total Amount: " + UITheme.formatCurrency(fullOrder.getTotalAmount()));

        // Populate Payment
        Payment p = fullOrder.getPayment();
        if (p != null) {
            paymentMethodLabel.setText("Payment Method: " + p.getPaymentMethod().getDisplayName());
            paymentStatusLabel.setText("Settlement Status: " + p.getPaymentStatus().getDisplayName() +
                    (p.getPaymentStatus().name().equals("SUCCESS") ? " ✅" : (p.getPaymentStatus().name().equals("REFUNDED") ? " 🔄 (Refunded)" : " ⚠️")));
            paymentTxnLabel.setText("Transaction Ref: " + p.getTransactionReference());
        }

        // Populate Timeline logs
        List<OrderStatusLog> logs = orderService.getOrderLogs(fullOrder.getOrderId());
        timelineTableModel.setRowCount(0);
        for (OrderStatusLog log : logs) {
            timelineTableModel.addRow(new Object[]{
                dateFormat.format(log.getUpdatedAt()),
                log.getStatus(),
                log.getRemarks()
            });
        }
    }

    private void clearDetailView() {
        orderHeaderLabel.setText("No orders placed yet.");
        restaurantDetailsLabel.setText("");
        deliveryAddressLabel.setText("");
        progressBar.setStatus(OrderStatus.PENDING);
        itemsTableModel.setRowCount(0);
        timelineTableModel.setRowCount(0);
        advanceStatusBtn.setEnabled(false);
        cancelOrderBtn.setEnabled(false);
    }

    private void advanceCurrentOrderStatus() {
        if (selectedOrder == null) return;
        OrderStatus next = selectedOrder.getStatus().next();
        if (next == selectedOrder.getStatus()) return;

        String remarks = JOptionPane.showInputDialog(
            this,
            "Advancing order #" + selectedOrder.getOrderId() + " from '" +
            selectedOrder.getStatus().getDisplayName() + "' to '" + next.getDisplayName() + "'.\n" +
            "Enter optional status update remarks:",
            "Status Transition Simulation",
            JOptionPane.PLAIN_MESSAGE
        );

        if (remarks == null) return; // Cancelled dialog
        if (remarks.trim().isEmpty()) {
            remarks = "Order status updated to " + next.getDisplayName();
        }

        try {
            orderService.updateOrderStatus(selectedOrder.getOrderId(), next, remarks);
            loadOrders();
            selectOrder(selectedOrder.getOrderId());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to update order status: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cancelCurrentOrder() {
        if (selectedOrder == null) return;

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to cancel Order #" + selectedOrder.getOrderId() + "?\n\n" +
            "This will trigger an atomic ACID transaction in MySQL:\n" +
            "1. Restore reserved food stock in menu_items.\n" +
            "2. Update payment status to REFUNDED.\n" +
            "3. Record status transition in order_status_logs.\n",
            "Confirm Order Cancellation & Refund",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            String reason = JOptionPane.showInputDialog(this, "Enter reason for cancellation:", "Customer changed mind");
            if (reason == null || reason.trim().isEmpty()) {
                reason = "Customer requested cancellation";
            }

            try {
                orderService.cancelOrder(selectedOrder.getOrderId(), reason);
                JOptionPane.showMessageDialog(this,
                    "Order #" + selectedOrder.getOrderId() + " has been cancelled.\n" +
                    "Stock inventory restored and payment refunded.",
                    "Order Cancelled",
                    JOptionPane.INFORMATION_MESSAGE
                );
                loadOrders();
                selectOrder(selectedOrder.getOrderId());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Cancellation failed: " + ex.getMessage(), "Transaction Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showPrintableReceipt() {
        if (selectedOrder == null) return;

        StringBuilder sb = new StringBuilder();
        sb.append("====================================================\n");
        sb.append("                 FOODIEHUB RECEIPT                  \n");
        sb.append("====================================================\n");
        sb.append("Order ID: #").append(selectedOrder.getOrderId()).append("\n");
        sb.append("Date: ").append(dateFormat.format(selectedOrder.getOrderDate())).append("\n");
        sb.append("Restaurant: ").append(selectedOrder.getRestaurantName()).append("\n");
        sb.append("Customer: ").append(selectedOrder.getCustomerName()).append(" (").append(selectedOrder.getCustomerPhone()).append(")\n");
        sb.append("Delivery Address: ").append(selectedOrder.getDeliveryAddress()).append("\n");
        sb.append("----------------------------------------------------\n");
        sb.append(String.format("%-25s %-5s %-10s %-10s\n", "ITEM", "QTY", "PRICE", "SUBTOTAL"));
        sb.append("----------------------------------------------------\n");

        for (OrderItem oi : selectedOrder.getItems()) {
            sb.append(String.format("%-25s %-5d %-10s %-10s\n",
                oi.getItemName().length() > 24 ? oi.getItemName().substring(0, 21) + "..." : oi.getItemName(),
                oi.getQuantity(),
                UITheme.formatCurrency(oi.getUnitPrice()),
                UITheme.formatCurrency(oi.getSubtotal())
            ));
        }

        sb.append("----------------------------------------------------\n");
        sb.append(String.format("%-35s %s\n", "TOTAL SETTLED:", UITheme.formatCurrency(selectedOrder.getTotalAmount())));
        if (selectedOrder.getPayment() != null) {
            sb.append("Payment Method: ").append(selectedOrder.getPayment().getPaymentMethod().getDisplayName()).append("\n");
            sb.append("Payment Status: ").append(selectedOrder.getPayment().getPaymentStatus().getDisplayName()).append("\n");
            sb.append("Txn Reference: ").append(selectedOrder.getPayment().getTransactionReference()).append("\n");
        }
        sb.append("Current Order Status: ").append(selectedOrder.getStatus().getDisplayName()).append("\n");
        sb.append("====================================================\n");
        sb.append("Thank you for ordering with FoodieHub!\n");

        JTextArea receiptText = new JTextArea(sb.toString(), 22, 45);
        receiptText.setFont(new Font("Consolas", Font.PLAIN, 12));
        receiptText.setEditable(false);
        receiptText.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(receiptText);
        JOptionPane.showMessageDialog(this, scroll, "Official Order Receipt #" + selectedOrder.getOrderId(), JOptionPane.PLAIN_MESSAGE);
    }
}
