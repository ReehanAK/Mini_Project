package com.foodsystem.ui;

import com.foodsystem.config.DBConnection;
import com.foodsystem.config.DatabaseConfig;
import com.foodsystem.models.Customer;
import com.foodsystem.service.CartManager;
import com.foodsystem.service.OrderService;
import com.foodsystem.ui.components.UITheme;
import com.foodsystem.ui.panels.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class MainFrame extends JFrame {

    private final OrderService orderService = OrderService.getInstance();
    private final CartManager cartManager = CartManager.getInstance();

    private JComboBox<Customer> customerComboBox;
    private JButton cartHeaderBtn;
    private JTabbedPane mainTabs;

    private BrowseRestaurantsPanel browsePanel;
    private CartCheckoutPanel cartPanel;
    private OrderTrackingPanel trackingPanel;
    private AdminManagementPanel adminPanel;
    private NormalizationJustificationPanel normPanel;

    public MainFrame() {
        super("FoodieHub — Online Food Ordering System (Java Swing + MySQL)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1240, 800);
        setMinimumSize(new Dimension(1000, 680));
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(UITheme.BG_LIGHT);

        // 1. Top Global Navigation / Header Bar
        JPanel headerBar = new JPanel(new BorderLayout(16, 0));
        headerBar.setBackground(UITheme.SECONDARY);
        headerBar.setBorder(new EmptyBorder(12, 20, 12, 20));

        // Brand & Logo (Left)
        JPanel brandPanel = new JPanel();
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setOpaque(false);

        JLabel brandTitle = new JLabel(" FoodieHub");
        brandTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        brandTitle.setForeground(Color.WHITE);

        JLabel brandSub = new JLabel("Online Food Ordering System  •  JDBC & MySQL/InnoDB");
        brandSub.setFont(UITheme.FONT_SMALL);
        brandSub.setForeground(new Color(203, 213, 225));

        brandPanel.add(brandTitle);
        brandPanel.add(brandSub);
        headerBar.add(brandPanel, BorderLayout.WEST);

        // Header Controls (Right)
        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 4));
        rightControls.setOpaque(false);

        // DB Status Indicator
        boolean isDbConnected = DBConnection.testConnection();
        JLabel dbStatus = UITheme.createBadge(
                isDbConnected ? "● MySQL Online: " + DatabaseConfig.getDatabase() : "● DB Offline",
                isDbConnected ? new Color(22, 101, 52) : new Color(153, 27, 27),
                Color.WHITE);
        rightControls.add(dbStatus);

        // Customer Profile Switcher
        JLabel custLabel = new JLabel("Active Customer:");
        custLabel.setFont(UITheme.FONT_BODY_BOLD);
        custLabel.setForeground(Color.WHITE);
        rightControls.add(custLabel);

        customerComboBox = new JComboBox<>();
        customerComboBox.setFont(UITheme.FONT_BODY);
        customerComboBox.setPreferredSize(new Dimension(190, 32));
        populateCustomers();

        customerComboBox.addActionListener(e -> {
            Customer selected = (Customer) customerComboBox.getSelectedItem();
            if (selected != null) {
                orderService.setActiveCustomer(selected);
                if (trackingPanel != null) {
                    trackingPanel.loadOrders();
                }
                if (cartPanel != null) {
                    cartPanel.updateCustomerAddress(selected.getAddress());
                }
            }
        });
        rightControls.add(customerComboBox);

        // "+ New Customer" button
        JButton newCustBtn = UITheme.createSecondaryButton("+ New");
        newCustBtn.setPreferredSize(new Dimension(75, 32));
        newCustBtn.addActionListener(e -> showNewCustomerDialog());
        rightControls.add(newCustBtn);

        // Cart Quick Action Button
        cartHeaderBtn = UITheme.createPrimaryButton(" Cart (0)");
        cartHeaderBtn.setPreferredSize(new Dimension(115, 32));
        cartHeaderBtn.addActionListener(e -> mainTabs.setSelectedIndex(1));
        rightControls.add(cartHeaderBtn);

        headerBar.add(rightControls, BorderLayout.EAST);
        rootPanel.add(headerBar, BorderLayout.NORTH);

        // 2. Main Content Tabs
        mainTabs = new JTabbedPane();
        mainTabs.setFont(UITheme.FONT_BODY_BOLD);

        browsePanel = new BrowseRestaurantsPanel(() -> mainTabs.setSelectedIndex(1));
        cartPanel = new CartCheckoutPanel(
                orderId -> {
                    // On order placed successfully, refresh tracking panel and switch to it
                    trackingPanel.loadOrders();
                    trackingPanel.selectOrder(orderId);
                    adminPanel.loadAllOrders();
                    adminPanel.loadInventory();
                    browsePanel.loadMenuItems();
                    mainTabs.setSelectedIndex(2);
                },
                () -> mainTabs.setSelectedIndex(0));
        trackingPanel = new OrderTrackingPanel();
        adminPanel = new AdminManagementPanel();
        normPanel = new NormalizationJustificationPanel();

        mainTabs.addTab(" Browse & Menu", browsePanel);
        mainTabs.addTab(" Cart & Checkout (0)", cartPanel);
        mainTabs.addTab(" Track Orders & Status", trackingPanel);
        mainTabs.addTab(" Admin & Inventory Console", adminPanel);
        mainTabs.addTab(" Normalization & Architecture", normPanel);

        rootPanel.add(mainTabs, BorderLayout.CENTER);
        setContentPane(rootPanel);

        // Cart listener to update header button and tab label
        cartManager.addListener(() -> {
            int count = cartManager.getTotalItemCount();
            cartHeaderBtn.setText(" Cart (" + count + ")");
            mainTabs.setTitleAt(1, " Cart & Checkout (" + count + ")");
        });

        // Tab change listener to auto-refresh tabs when opened
        mainTabs.addChangeListener(e -> {
            int idx = mainTabs.getSelectedIndex();
            if (idx == 0) {
                browsePanel.loadMenuItems();
            } else if (idx == 1) {
                cartPanel.refreshCartView();
            } else if (idx == 2) {
                trackingPanel.loadOrders();
            } else if (idx == 3) {
                adminPanel.loadAllOrders();
                adminPanel.loadInventory();
            }
        });
    }

    private void populateCustomers() {
        customerComboBox.removeAllItems();
        List<Customer> customers = orderService.getAllCustomers();
        for (Customer c : customers) {
            customerComboBox.addItem(c);
        }
        if (!customers.isEmpty()) {
            customerComboBox.setSelectedIndex(0);
            orderService.setActiveCustomer(customers.get(0));
        }
    }

    private void showNewCustomerDialog() {
        JTextField nameField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextArea addressArea = new JTextArea(3, 20);

        JPanel form = new JPanel(new GridLayout(0, 1, 4, 4));
        form.add(new JLabel("Full Name:"));
        form.add(nameField);
        form.add(new JLabel("Email Address (Unique):"));
        form.add(emailField);
        form.add(new JLabel("Phone Number:"));
        form.add(phoneField);
        form.add(new JLabel("Delivery Address:"));
        form.add(new JScrollPane(addressArea));

        int result = JOptionPane.showConfirmDialog(this, form, "Register New Customer", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String addr = addressArea.getText().trim();

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || addr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All customer fields are mandatory.", "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            Customer c = new Customer(0, name, email, phone, addr);
            try {
                Customer created = orderService.createCustomer(c);
                if (created != null) {
                    JOptionPane.showMessageDialog(this,
                            "Customer registered successfully! (ID: " + created.getCustomerId() + ")", "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    populateCustomers();
                    // Select new customer
                    for (int i = 0; i < customerComboBox.getItemCount(); i++) {
                        if (customerComboBox.getItemAt(i).getCustomerId() == created.getCustomerId()) {
                            customerComboBox.setSelectedIndex(i);
                            break;
                        }
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to register customer: " + ex.getMessage(), "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
