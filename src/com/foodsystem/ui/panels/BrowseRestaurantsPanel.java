package com.foodsystem.ui.panels;

import com.foodsystem.models.MenuItem;
import com.foodsystem.models.Restaurant;
import com.foodsystem.service.CartManager;
import com.foodsystem.service.OrderService;
import com.foodsystem.ui.components.CardPanel;
import com.foodsystem.ui.components.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

public class BrowseRestaurantsPanel extends JPanel {

    private final OrderService orderService = OrderService.getInstance();
    private final CartManager cartManager = CartManager.getInstance();

    private JTextField searchField;
    private JComboBox<String> cuisineComboBox;
    private JPanel restaurantListPanel;
    private JPanel menuItemsContainer;
    private JPanel restaurantHeaderPanel;
    private JComboBox<String> categoryComboBox;

    private Restaurant selectedRestaurant;
    private String selectedCategory = "All Categories";

    private Runnable onNavigateToCart;

    public BrowseRestaurantsPanel(Runnable onNavigateToCart) {
        this.onNavigateToCart = onNavigateToCart;
        setLayout(new BorderLayout(12, 12));
        setBackground(UITheme.BG_LIGHT);
        setBorder(new EmptyBorder(12, 16, 16, 16));

        initUI();
        loadRestaurants();
    }

    private void initUI() {
        // Main split pane: Left is Restaurants list, Right is Menu Items
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(340);
        splitPane.setDividerSize(4);
        splitPane.setContinuousLayout(true);
        splitPane.setBorder(null);

        // Left Panel: Search & Restaurants
        JPanel leftPanel = new JPanel(new BorderLayout(8, 8));
        leftPanel.setBackground(UITheme.BG_LIGHT);

        JPanel searchHeader = new JPanel();
        searchHeader.setLayout(new BoxLayout(searchHeader, BoxLayout.Y_AXIS));
        searchHeader.setBackground(UITheme.BG_LIGHT);

        JLabel titleLabel = new JLabel("Explore Restaurants");
        titleLabel.setFont(UITheme.FONT_SUBHEADER);
        titleLabel.setForeground(UITheme.TEXT_DARK);
        searchHeader.add(titleLabel);
        searchHeader.add(Box.createVerticalStrut(8));

        // Search Bar
        JPanel searchBoxPanel = new JPanel(new BorderLayout(6, 0));
        searchBoxPanel.setBackground(Color.WHITE);
        searchBoxPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1),
            new EmptyBorder(6, 10, 6, 10)
        ));

        searchField = new JTextField();
        searchField.setBorder(null);
        searchField.setFont(UITheme.FONT_BODY);
        searchField.putClientProperty("JTextField.placeholderText", "Search name, cuisine, address...");
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                loadRestaurants();
            }
        });

        JLabel searchIcon = new JLabel("🔍");
        searchBoxPanel.add(searchIcon, BorderLayout.WEST);
        searchBoxPanel.add(searchField, BorderLayout.CENTER);
        searchHeader.add(searchBoxPanel);
        searchHeader.add(Box.createVerticalStrut(8));

        // Cuisine filter dropdown
        JPanel filterRow = new JPanel(new BorderLayout(6, 0));
        filterRow.setOpaque(false);
        JLabel cuisineLbl = new JLabel("Cuisine:");
        cuisineLbl.setFont(UITheme.FONT_BODY_BOLD);
        cuisineComboBox = new JComboBox<>();
        cuisineComboBox.setFont(UITheme.FONT_BODY);
        cuisineComboBox.addActionListener(e -> loadRestaurants());

        filterRow.add(cuisineLbl, BorderLayout.WEST);
        filterRow.add(cuisineComboBox, BorderLayout.CENTER);
        searchHeader.add(filterRow);
        searchHeader.add(Box.createVerticalStrut(6));

        leftPanel.add(searchHeader, BorderLayout.NORTH);

        // Scrollable Restaurant cards list
        restaurantListPanel = new JPanel();
        restaurantListPanel.setLayout(new BoxLayout(restaurantListPanel, BoxLayout.Y_AXIS));
        restaurantListPanel.setBackground(UITheme.BG_LIGHT);

        JScrollPane leftScroll = new JScrollPane(restaurantListPanel);
        leftScroll.setBorder(null);
        leftScroll.getVerticalScrollBar().setUnitIncrement(16);
        leftPanel.add(leftScroll, BorderLayout.CENTER);

        splitPane.setLeftComponent(leftPanel);

        // Right Panel: Restaurant Menu & Items
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setBackground(UITheme.BG_LIGHT);

        // Restaurant Header info
        restaurantHeaderPanel = new JPanel(new BorderLayout(10, 8));
        restaurantHeaderPanel.setBackground(UITheme.CARD_BG);
        restaurantHeaderPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1),
            new EmptyBorder(12, 16, 12, 16)
        ));
        rightPanel.add(restaurantHeaderPanel, BorderLayout.NORTH);

        // Menu items scrollable area
        menuItemsContainer = new JPanel();
        menuItemsContainer.setLayout(new BoxLayout(menuItemsContainer, BoxLayout.Y_AXIS));
        menuItemsContainer.setBackground(UITheme.BG_LIGHT);

        JScrollPane rightScroll = new JScrollPane(menuItemsContainer);
        rightScroll.setBorder(null);
        rightScroll.getVerticalScrollBar().setUnitIncrement(16);
        rightPanel.add(rightScroll, BorderLayout.CENTER);

        splitPane.setRightComponent(rightPanel);
        add(splitPane, BorderLayout.CENTER);

        // Populate cuisines
        List<String> cuisines = orderService.getAllCuisines();
        for (String c : cuisines) {
            cuisineComboBox.addItem(c);
        }
    }

    public void loadRestaurants() {
        String query = searchField != null ? searchField.getText().trim() : "";
        String cuisine = cuisineComboBox != null ? (String) cuisineComboBox.getSelectedItem() : "All Cuisines";

        List<Restaurant> restaurants = orderService.searchRestaurants(query, cuisine);

        restaurantListPanel.removeAll();
        if (restaurants.isEmpty()) {
            JLabel emptyLabel = new JLabel("No restaurants found matching criteria.");
            emptyLabel.setFont(UITheme.FONT_BODY);
            emptyLabel.setForeground(UITheme.TEXT_MUTED);
            emptyLabel.setBorder(new EmptyBorder(20, 10, 10, 10));
            restaurantListPanel.add(emptyLabel);
        } else {
            for (Restaurant r : restaurants) {
                JPanel card = createRestaurantCard(r);
                restaurantListPanel.add(card);
                restaurantListPanel.add(Box.createVerticalStrut(8));
            }

            // Auto-select first restaurant if none selected or previously selected is not in list
            if (selectedRestaurant == null || restaurants.stream().noneMatch(r -> r.getRestaurantId() == selectedRestaurant.getRestaurantId())) {
                selectRestaurant(restaurants.get(0));
            }
        }

        restaurantListPanel.revalidate();
        restaurantListPanel.repaint();
    }

    private JPanel createRestaurantCard(Restaurant r) {
        boolean isSelected = (selectedRestaurant != null && selectedRestaurant.getRestaurantId() == r.getRestaurantId());

        CardPanel card = new CardPanel(new BorderLayout(8, 6), 10);
        if (isSelected) {
            card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.PRIMARY, 2),
                new EmptyBorder(9, 9, 9, 9)
            ));
            card.setBackground(new Color(255, 247, 237));
        }

        JPanel topRow = new JPanel(new BorderLayout(6, 0));
        topRow.setOpaque(false);

        JLabel nameLabel = new JLabel(r.getName());
        nameLabel.setFont(UITheme.FONT_BODY_BOLD);
        nameLabel.setForeground(UITheme.TEXT_DARK);

        JLabel ratingLabel = new JLabel("★ " + String.format("%.1f", r.getRating()));
        ratingLabel.setFont(UITheme.FONT_BODY_BOLD);
        ratingLabel.setForeground(new Color(217, 119, 6)); // Amber

        topRow.add(nameLabel, BorderLayout.CENTER);
        topRow.add(ratingLabel, BorderLayout.EAST);

        JPanel middleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 4));
        middleRow.setOpaque(false);
        JLabel cuisineBadge = UITheme.createBadge(r.getCuisineType(), new Color(241, 245, 249), UITheme.TEXT_DARK);
        middleRow.add(cuisineBadge);

        JLabel addressLabel = new JLabel("📍 " + r.getAddress());
        addressLabel.setFont(UITheme.FONT_SMALL);
        addressLabel.setForeground(UITheme.TEXT_MUTED);

        card.add(topRow, BorderLayout.NORTH);
        card.add(middleRow, BorderLayout.CENTER);
        card.add(addressLabel, BorderLayout.SOUTH);

        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                selectRestaurant(r);
                loadRestaurants(); // Refresh selection borders
            }
        });

        return card;
    }

    public void selectRestaurant(Restaurant r) {
        this.selectedRestaurant = r;
        this.selectedCategory = "All Categories";
        renderRestaurantHeader();
        loadMenuItems();
    }

    private void renderRestaurantHeader() {
        restaurantHeaderPanel.removeAll();
        if (selectedRestaurant == null) return;

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleRow.setOpaque(false);

        JLabel nameLabel = new JLabel(selectedRestaurant.getName());
        nameLabel.setFont(UITheme.FONT_HEADER);
        nameLabel.setForeground(UITheme.TEXT_DARK);

        JLabel cuisineBadge = UITheme.createBadge(selectedRestaurant.getCuisineType(), UITheme.PRIMARY_LIGHT, UITheme.PRIMARY_DARK);
        JLabel ratingBadge = UITheme.createBadge("★ " + String.format("%.1f", selectedRestaurant.getRating()), new Color(254, 243, 199), new Color(180, 83, 9));
        JLabel openBadge = UITheme.createBadge("● Open Now", new Color(220, 252, 231), UITheme.SUCCESS);

        titleRow.add(nameLabel);
        titleRow.add(cuisineBadge);
        titleRow.add(ratingBadge);
        titleRow.add(openBadge);
        infoPanel.add(titleRow);
        infoPanel.add(Box.createVerticalStrut(4));

        JLabel detailsLabel = new JLabel("📍 " + selectedRestaurant.getAddress() + "  |  📞 " + selectedRestaurant.getPhone());
        detailsLabel.setFont(UITheme.FONT_BODY);
        detailsLabel.setForeground(UITheme.TEXT_MUTED);
        infoPanel.add(detailsLabel);

        restaurantHeaderPanel.add(infoPanel, BorderLayout.CENTER);

        // Category filter in header
        List<String> categories = orderService.getMenuCategories(selectedRestaurant.getRestaurantId());
        JPanel catPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        catPanel.setOpaque(false);

        JLabel catLbl = new JLabel("Category:");
        catLbl.setFont(UITheme.FONT_BODY_BOLD);
        categoryComboBox = new JComboBox<>(categories.toArray(new String[0]));
        categoryComboBox.setFont(UITheme.FONT_BODY);
        categoryComboBox.addActionListener(e -> {
            selectedCategory = (String) categoryComboBox.getSelectedItem();
            loadMenuItems();
        });

        catPanel.add(catLbl);
        catPanel.add(categoryComboBox);
        restaurantHeaderPanel.add(catPanel, BorderLayout.EAST);

        restaurantHeaderPanel.revalidate();
        restaurantHeaderPanel.repaint();
    }

    public void loadMenuItems() {
        menuItemsContainer.removeAll();
        if (selectedRestaurant == null) return;

        List<MenuItem> items = orderService.getMenuItems(selectedRestaurant.getRestaurantId());

        boolean anyDisplayed = false;
        for (MenuItem item : items) {
            if (selectedCategory != null && !selectedCategory.equals("All Categories") && !item.getCategory().equalsIgnoreCase(selectedCategory)) {
                continue;
            }
            anyDisplayed = true;
            menuItemsContainer.add(createMenuItemCard(item));
            menuItemsContainer.add(Box.createVerticalStrut(10));
        }

        if (!anyDisplayed) {
            JLabel emptyLbl = new JLabel("No menu items found for this category.");
            emptyLbl.setFont(UITheme.FONT_BODY);
            emptyLbl.setForeground(UITheme.TEXT_MUTED);
            emptyLbl.setBorder(new EmptyBorder(30, 20, 20, 20));
            menuItemsContainer.add(emptyLbl);
        }

        menuItemsContainer.revalidate();
        menuItemsContainer.repaint();
    }

    private JPanel createMenuItemCard(MenuItem item) {
        CardPanel card = new CardPanel(new BorderLayout(12, 8), 14);

        // Left info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleRow.setOpaque(false);

        JLabel nameLabel = new JLabel(item.getName());
        nameLabel.setFont(UITheme.FONT_SUBHEADER);
        nameLabel.setForeground(UITheme.TEXT_DARK);

        JLabel catBadge = UITheme.createBadge(item.getCategory(), new Color(241, 245, 249), UITheme.TEXT_MUTED);

        // Stock status badge
        JLabel stockBadge;
        if (!item.isAvailable() || item.getStockQuantity() <= 0) {
            stockBadge = UITheme.createBadge("Out of Stock", new Color(254, 226, 226), UITheme.DANGER);
        } else if (item.getStockQuantity() <= 5) {
            stockBadge = UITheme.createBadge("Only " + item.getStockQuantity() + " left!", new Color(254, 243, 199), UITheme.WARNING);
        } else {
            stockBadge = UITheme.createBadge("In Stock (" + item.getStockQuantity() + ")", new Color(220, 252, 231), UITheme.SUCCESS);
        }

        titleRow.add(nameLabel);
        titleRow.add(catBadge);
        titleRow.add(stockBadge);
        infoPanel.add(titleRow);
        infoPanel.add(Box.createVerticalStrut(4));

        JLabel descLabel = new JLabel("<html><body style='width: 380px;'>" + item.getDescription() + "</body></html>");
        descLabel.setFont(UITheme.FONT_BODY);
        descLabel.setForeground(UITheme.TEXT_MUTED);
        infoPanel.add(descLabel);

        card.add(infoPanel, BorderLayout.CENTER);

        // Right action panel: Price, Quantity Spinner, Add Button
        JPanel actionPanel = new JPanel();
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
        actionPanel.setOpaque(false);
        actionPanel.setPreferredSize(new Dimension(170, 70));

        JLabel priceLabel = new JLabel(UITheme.formatCurrency(item.getPrice()));
        priceLabel.setFont(UITheme.FONT_PRICE);
        priceLabel.setForeground(UITheme.PRIMARY_DARK);
        priceLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JPanel controlRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        controlRow.setOpaque(false);
        controlRow.setAlignmentX(Component.RIGHT_ALIGNMENT);

        int maxStock = Math.max(1, item.getStockQuantity());
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(1, 1, maxStock, 1);
        JSpinner qtySpinner = new JSpinner(spinnerModel);
        qtySpinner.setFont(UITheme.FONT_BODY);
        qtySpinner.setPreferredSize(new Dimension(50, 32));
        qtySpinner.setEnabled(item.isAvailable() && item.getStockQuantity() > 0);

        JButton addBtn = UITheme.createPrimaryButton("+ Add");
        addBtn.setPreferredSize(new Dimension(90, 32));
        addBtn.setEnabled(item.isAvailable() && item.getStockQuantity() > 0);

        addBtn.addActionListener(e -> {
            int qty = (Integer) qtySpinner.getValue();
            boolean added = cartManager.addItem(selectedRestaurant, item, qty);
            if (!added) {
                // Cart has items from another restaurant
                int option = JOptionPane.showConfirmDialog(
                    this,
                    "Your cart contains items from '" + cartManager.getCurrentRestaurant().getName() + "'.\n" +
                    "Ordering from multiple restaurants in a single transaction is disallowed to maintain\n" +
                    "delivery integrity and prevent invalid orders.\n\n" +
                    "Would you like to clear your cart and start a new order with '" + selectedRestaurant.getName() + "'?",
                    "Clear Cart Confirmation",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
                );

                if (option == JOptionPane.YES_OPTION) {
                    cartManager.clearCart();
                    cartManager.addItem(selectedRestaurant, item, qty);
                    showToast("Added " + qty + "x " + item.getName() + " to cart!");
                }
            } else {
                showToast("Added " + qty + "x " + item.getName() + " to cart!");
            }
        });

        controlRow.add(qtySpinner);
        controlRow.add(addBtn);

        actionPanel.add(priceLabel);
        actionPanel.add(Box.createVerticalStrut(6));
        actionPanel.add(controlRow);

        card.add(actionPanel, BorderLayout.EAST);
        return card;
    }

    private void showToast(String message) {
        JOptionPane.showMessageDialog(this, message, "Cart Updated", JOptionPane.INFORMATION_MESSAGE);
    }
}
