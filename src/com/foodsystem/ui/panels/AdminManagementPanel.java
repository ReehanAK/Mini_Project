package com.foodsystem.ui.panels;

import com.foodsystem.models.MenuItem;
import com.foodsystem.models.Order;
import com.foodsystem.models.Restaurant;
import com.foodsystem.models.enums.OrderStatus;
import com.foodsystem.service.OrderService;
import com.foodsystem.ui.components.CardPanel;
import com.foodsystem.ui.components.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

public class AdminManagementPanel extends JPanel {

    private final OrderService orderService = OrderService.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    // Orders Tab components
    private JComboBox<String> orderFilterCombo;
    private JTable allOrdersTable;
    private DefaultTableModel ordersModel;
    private List<Order> allOrders;
    private JComboBox<OrderStatus> changeStatusCombo;
    private JTextField statusRemarkField;

    // Inventory Tab components
    private JComboBox<String> restaurantFilterCombo;
    private JTable inventoryTable;
    private DefaultTableModel inventoryModel;
    private List<MenuItem> allMenuItems;

    public AdminManagementPanel() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_LIGHT);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_BODY_BOLD);

        tabbedPane.addTab("📋 Kitchen & Dispatch Orders", createOrdersConsolePanel());
        tabbedPane.addTab("📦 Menu Inventory & Stock Control", createInventoryConsolePanel());

        add(tabbedPane, BorderLayout.CENTER);

        loadAllOrders();
        loadInventory();
    }

    // -------------------------------------------------------------
    // Tab 1: Kitchen & Dispatch Orders
    // -------------------------------------------------------------
    private JPanel createOrdersConsolePanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(UITheme.BG_LIGHT);
        panel.setBorder(new EmptyBorder(12, 16, 16, 16));

        // Header Toolbar
        JPanel topToolbar = new JPanel(new BorderLayout());
        topToolbar.setOpaque(false);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterPanel.setOpaque(false);
        JLabel filterLbl = new JLabel("Filter by Status:");
        filterLbl.setFont(UITheme.FONT_BODY_BOLD);

        orderFilterCombo = new JComboBox<>(new String[]{
            "All Orders", "PENDING", "CONFIRMED", "PREPARING", "OUT_FOR_DELIVERY", "DELIVERED", "CANCELLED"
        });
        orderFilterCombo.setFont(UITheme.FONT_BODY);
        orderFilterCombo.addActionListener(e -> loadAllOrders());

        filterPanel.add(filterLbl);
        filterPanel.add(orderFilterCombo);

        JButton refreshBtn = UITheme.createSecondaryButton("🔄 Refresh Orders");
        refreshBtn.addActionListener(e -> loadAllOrders());

        topToolbar.add(filterPanel, BorderLayout.WEST);
        topToolbar.add(refreshBtn, BorderLayout.EAST);
        panel.add(topToolbar, BorderLayout.NORTH);

        // Table
        String[] cols = {"Order ID", "Date", "Customer", "Restaurant", "Total", "Status"};
        ordersModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        allOrdersTable = new JTable(ordersModel);
        allOrdersTable.setRowHeight(34);
        allOrdersTable.setFont(UITheme.FONT_BODY);
        allOrdersTable.getTableHeader().setFont(UITheme.FONT_BODY_BOLD);
        allOrdersTable.getTableHeader().setBackground(new Color(241, 245, 249));

        DefaultTableCellRenderer centerRender = new DefaultTableCellRenderer();
        centerRender.setHorizontalAlignment(JLabel.CENTER);
        allOrdersTable.getColumnModel().getColumn(0).setCellRenderer(centerRender);
        allOrdersTable.getColumnModel().getColumn(5).setCellRenderer(centerRender);

        JScrollPane scrollPane = new JScrollPane(allOrdersTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));
        panel.add(scrollPane, BorderLayout.CENTER);

        // Status update footer
        CardPanel footerCard = new CardPanel(new FlowLayout(FlowLayout.LEFT, 12, 8), 10);
        JLabel updateLbl = new JLabel("Update Selected Order Status:");
        updateLbl.setFont(UITheme.FONT_BODY_BOLD);

        changeStatusCombo = new JComboBox<>(OrderStatus.values());
        changeStatusCombo.setFont(UITheme.FONT_BODY);

        statusRemarkField = new JTextField(20);
        statusRemarkField.setFont(UITheme.FONT_BODY);
        statusRemarkField.putClientProperty("JTextField.placeholderText", "Audit remark (optional)");

        JButton applyStatusBtn = UITheme.createPrimaryButton("Update Order Status");
        applyStatusBtn.addActionListener(e -> updateSelectedOrderStatus());

        footerCard.add(updateLbl);
        footerCard.add(changeStatusCombo);
        footerCard.add(statusRemarkField);
        footerCard.add(applyStatusBtn);

        panel.add(footerCard, BorderLayout.SOUTH);
        return panel;
    }

    public void loadAllOrders() {
        allOrders = orderService.getAllOrders();
        String selectedFilter = (String) orderFilterCombo.getSelectedItem();

        ordersModel.setRowCount(0);
        for (Order o : allOrders) {
            if (selectedFilter != null && !selectedFilter.equals("All Orders") && !o.getStatus().name().equalsIgnoreCase(selectedFilter)) {
                continue;
            }
            ordersModel.addRow(new Object[]{
                "#" + o.getOrderId(),
                dateFormat.format(o.getOrderDate()),
                o.getCustomerName() != null ? o.getCustomerName() : "Customer #" + o.getCustomerId(),
                o.getRestaurantName(),
                UITheme.formatCurrency(o.getTotalAmount()),
                o.getStatus().getDisplayName()
            });
        }
    }

    private void updateSelectedOrderStatus() {
        int selectedRow = allOrdersTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an order from the table.", "Select Order", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String orderIdStr = (String) ordersModel.getValueAt(selectedRow, 0);
        int orderId = Integer.parseInt(orderIdStr.replace("#", ""));
        OrderStatus newStatus = (OrderStatus) changeStatusCombo.getSelectedItem();
        String remark = statusRemarkField.getText().trim();
        if (remark.isEmpty()) {
            remark = "Status transitioned to " + newStatus.getDisplayName() + " by Admin Console";
        }

        try {
            if (newStatus == OrderStatus.CANCELLED) {
                orderService.cancelOrder(orderId, remark);
            } else {
                orderService.updateOrderStatus(orderId, newStatus, remark);
            }
            JOptionPane.showMessageDialog(this, "Order #" + orderId + " updated to " + newStatus.getDisplayName(), "Success", JOptionPane.INFORMATION_MESSAGE);
            loadAllOrders();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to update order: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // -------------------------------------------------------------
    // Tab 2: Menu Inventory & Stock Control
    // -------------------------------------------------------------
    private JPanel createInventoryConsolePanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(UITheme.BG_LIGHT);
        panel.setBorder(new EmptyBorder(12, 16, 16, 16));

        // Header Toolbar
        JPanel topToolbar = new JPanel(new BorderLayout());
        topToolbar.setOpaque(false);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterPanel.setOpaque(false);
        JLabel restLbl = new JLabel("Filter by Restaurant:");
        restLbl.setFont(UITheme.FONT_BODY_BOLD);

        restaurantFilterCombo = new JComboBox<>();
        restaurantFilterCombo.setFont(UITheme.FONT_BODY);
        restaurantFilterCombo.addItem("All Restaurants");
        for (Restaurant r : orderService.getAllRestaurants()) {
            restaurantFilterCombo.addItem(r.getName());
        }
        restaurantFilterCombo.addActionListener(e -> loadInventory());

        filterPanel.add(restLbl);
        filterPanel.add(restaurantFilterCombo);

        JButton refreshInvBtn = UITheme.createSecondaryButton("🔄 Refresh Inventory");
        refreshInvBtn.addActionListener(e -> loadInventory());

        topToolbar.add(filterPanel, BorderLayout.WEST);
        topToolbar.add(refreshInvBtn, BorderLayout.EAST);
        panel.add(topToolbar, BorderLayout.NORTH);

        // Inventory Table
        String[] invCols = {"Item ID", "Item Name", "Restaurant", "Category", "Price", "Stock Quantity", "Availability"};
        inventoryModel = new DefaultTableModel(invCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        inventoryTable = new JTable(inventoryModel);
        inventoryTable.setRowHeight(34);
        inventoryTable.setFont(UITheme.FONT_BODY);
        inventoryTable.getTableHeader().setFont(UITheme.FONT_BODY_BOLD);
        inventoryTable.getTableHeader().setBackground(new Color(241, 245, 249));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        inventoryTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        inventoryTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        inventoryTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        inventoryTable.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);

        JScrollPane invScroll = new JScrollPane(inventoryTable);
        invScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));
        panel.add(invScroll, BorderLayout.CENTER);

        // Inventory Action Controls Footer
        CardPanel footerCard = new CardPanel(new FlowLayout(FlowLayout.LEFT, 10, 8), 10);

        JButton restockBtn = UITheme.createPrimaryButton("📦 Restock Selected (+25 units)");
        restockBtn.addActionListener(e -> adjustStockForSelected(25));

        JButton drainStockBtn = UITheme.createSecondaryButton("⚠️ Set Stock to 0 (Test Out-of-Stock)");
        drainStockBtn.addActionListener(e -> setStockForSelected(0));

        JButton toggleAvailBtn = UITheme.createSecondaryButton("🔄 Toggle Availability");
        toggleAvailBtn.addActionListener(e -> toggleAvailabilityForSelected());

        footerCard.add(restockBtn);
        footerCard.add(drainStockBtn);
        footerCard.add(toggleAvailBtn);

        panel.add(footerCard, BorderLayout.SOUTH);
        return panel;
    }

    public void loadInventory() {
        allMenuItems = orderService.getAllMenuItems();
        String selectedRestName = (String) restaurantFilterCombo.getSelectedItem();

        inventoryModel.setRowCount(0);
        for (MenuItem item : allMenuItems) {
            Restaurant r = orderService.getRestaurantById(item.getRestaurantId());
            String restName = r != null ? r.getName() : "Unknown";

            if (selectedRestName != null && !selectedRestName.equals("All Restaurants") && !restName.equalsIgnoreCase(selectedRestName)) {
                continue;
            }

            String stockDisplay;
            if (item.getStockQuantity() <= 0) {
                stockDisplay = "0 (OUT OF STOCK)";
            } else if (item.getStockQuantity() <= 5) {
                stockDisplay = item.getStockQuantity() + " (LOW)";
            } else {
                stockDisplay = String.valueOf(item.getStockQuantity());
            }

            inventoryModel.addRow(new Object[]{
                item.getItemId(),
                item.getName(),
                restName,
                item.getCategory(),
                UITheme.formatCurrency(item.getPrice()),
                stockDisplay,
                item.getRawIsAvailable() ? "AVAILABLE" : "DISABLED"
            });
        }
    }

    private void adjustStockForSelected(int delta) {
        int row = inventoryTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an item row to modify stock.", "Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int itemId = (Integer) inventoryModel.getValueAt(row, 0);
        MenuItem item = allMenuItems.stream().filter(m -> m.getItemId() == itemId).findFirst().orElse(null);
        if (item == null) return;

        int newStock = Math.max(0, item.getStockQuantity() + delta);
        boolean success = orderService.updateStockAndAvailability(itemId, newStock, true);
        if (success) {
            JOptionPane.showMessageDialog(this, "Stock for '" + item.getName() + "' updated to " + newStock, "Stock Updated", JOptionPane.INFORMATION_MESSAGE);
            loadInventory();
        }
    }

    private void setStockForSelected(int targetStock) {
        int row = inventoryTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an item row to modify.", "Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int itemId = (Integer) inventoryModel.getValueAt(row, 0);
        MenuItem item = allMenuItems.stream().filter(m -> m.getItemId() == itemId).findFirst().orElse(null);
        if (item == null) return;

        boolean success = orderService.updateStockAndAvailability(itemId, targetStock, targetStock > 0);
        if (success) {
            JOptionPane.showMessageDialog(this, "Stock for '" + item.getName() + "' set to " + targetStock +
                    ".\nTry ordering this item in the menu to verify the invalid transaction prevention!", "Stock Updated", JOptionPane.INFORMATION_MESSAGE);
            loadInventory();
        }
    }

    private void toggleAvailabilityForSelected() {
        int row = inventoryTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an item row to toggle.", "Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int itemId = (Integer) inventoryModel.getValueAt(row, 0);
        MenuItem item = allMenuItems.stream().filter(m -> m.getItemId() == itemId).findFirst().orElse(null);
        if (item == null) return;

        boolean newAvail = !item.getRawIsAvailable();
        boolean success = orderService.updateStockAndAvailability(itemId, item.getStockQuantity(), newAvail);
        if (success) {
            JOptionPane.showMessageDialog(this, "Item '" + item.getName() + "' availability toggled to " + (newAvail ? "AVAILABLE" : "DISABLED"), "Updated", JOptionPane.INFORMATION_MESSAGE);
            loadInventory();
        }
    }
}
