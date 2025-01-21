import java.awt.*;
import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class WartegApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("WartegApp");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new LoginPanel());
            frame.setSize(600, 800);
            frame.setVisible(true);
        });
    }

    // Menu Item abstract class
    static abstract class MenuItem {
        protected String name;
        protected int price;
        protected String imagePath;

        public MenuItem(String name, int price, String imagePath) {
            this.name = name;
            this.price = price;
            this.imagePath = imagePath;
        }

        public String getName() { return name; }
        public int getPrice() { return price; }
        public String getImagePath() { return imagePath; }
    }

    // Food class
    static class Food extends MenuItem {
        public Food(String name, int price, String imagePath) {
            super(name, price, imagePath);
        }
    }

    // Drink class
    static class Drink extends MenuItem {
        public Drink(String name, int price, String imagePath) {
            super(name, price, imagePath);
        }
    }

    // Menu Manager class
    static class MenuManager {
        private static ArrayList<Food> foods = new ArrayList<>();
        private static ArrayList<Drink> drinks = new ArrayList<>();

        static {
            // Initialize foods
            foods.add(new Food("Ayam", 15000, "ayam.jpeg"));
            foods.add(new Food("Ikan", 20000, "ikan.jpeg"));
            foods.add(new Food("Nasi", 5000, "nasi.jpeg"));
            foods.add(new Food("Sambel", 3000, "sambel.jpeg"));
            foods.add(new Food("Sayur Asem", 8000, "sayur_asem.jpeg"));
            foods.add(new Food("Sayur Lodeh", 9000, "sayur_lodeh.jpeg"));
            foods.add(new Food("Tahu Tempe", 4000, "tahu_tempe.jpeg"));

            // Initialize drinks
            drinks.add(new Drink("Es Jeruk", 7000, "es_jeruk.jpg"));
            drinks.add(new Drink("Air Putih", 2000, "air_putih.jpg"));
        }

        public static ArrayList<Food> getFoods() { return foods; }
        public static ArrayList<Drink> getDrinks() { return drinks; }
        
        public static void addFood(Food food) {
            foods.add(food);
        }
        
        public static void addDrink(Drink drink) {
            drinks.add(drink);
        }
        
        public static void removeFood(String name) {
            foods.removeIf(food -> food.getName().equals(name));
        }
        
        public static void removeDrink(String name) {
            drinks.removeIf(drink -> drink.getName().equals(name));
        }
    }

    // Panel Login
    static class LoginPanel extends JPanel {
        private JTextField usernameField;
        private JPasswordField passwordField;
        private JLabel errorLabel;

        public LoginPanel() {
            setLayout(new GridBagLayout());
            setBorder(new EmptyBorder(20, 20, 20, 20));
            GridBagConstraints gbc = new GridBagConstraints();

            JLabel titleLabel = new JLabel("Login WartegApp");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 24));

            errorLabel = new JLabel(" ");
            errorLabel.setForeground(Color.RED);

            usernameField = new JTextField(20);
            passwordField = new JPasswordField(20);

            JButton loginButton = new JButton("Login");
            JButton registerButton = new JButton("Daftar Baru");

            loginButton.addActionListener(e -> doLogin());
            registerButton.addActionListener(e -> {
                clearFields();
                JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
                frame.setContentPane(new RegisterPanel());
                frame.revalidate();
            });

            // Layout
            gbc.gridwidth = 2;
            gbc.insets = new Insets(5, 5, 5, 5);

            gbc.gridx = 0; gbc.gridy = 0;
            add(titleLabel, gbc);

            gbc.gridy = 1;
            gbc.gridy = 1;
            add(errorLabel, gbc);

            gbc.gridwidth = 1;
            gbc.gridy = 2;
            add(new JLabel("Username:"), gbc);
            gbc.gridx = 1;
            add(usernameField, gbc);

            gbc.gridx = 0; gbc.gridy = 3;
            add(new JLabel("Password:"), gbc);
            gbc.gridx = 1;
            add(passwordField, gbc);

            gbc.gridx = 0; gbc.gridy = 4;
            add(loginButton, gbc);
            gbc.gridx = 1;
            add(registerButton, gbc);
        }

        private void doLogin() {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Username dan password harus diisi!");
                return;
            }

            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3307/warteg_app_new", "root", "")) {
                PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT * FROM users WHERE username = ? AND password = ?");
                pstmt.setString(1, username);
                pstmt.setString(2, password);

                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    String role = rs.getString("role"); // Tambahkan kolom role di database
                    clearFields();
                    JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
                    if (role != null && role.equals("admin")) { // Cek role admin
                        frame.setContentPane(new AdminPanel());
                    } else {
                        frame.setContentPane(new MenuPanel());
                    }
                    frame.revalidate();
                } else {
                    errorLabel.setText("Username atau password salah!");
                }
            } catch (SQLException e) {
                errorLabel.setText("Error: " + e.getMessage());
            }
        }

        private void clearFields() {
            usernameField.setText("");
            passwordField.setText("");
            errorLabel.setText(" ");
        }
    }

    // Panel Register
    static class RegisterPanel extends JPanel {
        private JTextField usernameField;
        private JPasswordField passwordField;
        private JPasswordField confirmPasswordField;
        private JLabel errorLabel;

        public RegisterPanel() {
            setLayout(new GridBagLayout());
            setBorder(new EmptyBorder(20, 20, 20, 20));
            GridBagConstraints gbc = new GridBagConstraints();

            JLabel titleLabel = new JLabel("Registrasi User Baru");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 24));

            errorLabel = new JLabel(" ");
            errorLabel.setForeground(Color.RED);

            usernameField = new JTextField(20);
            passwordField = new JPasswordField(20);
            confirmPasswordField = new JPasswordField(20);

            JButton registerButton = new JButton("Daftar");
            JButton backButton = new JButton("Kembali ke Login");

            registerButton.addActionListener(e -> doRegister());
            backButton.addActionListener(e -> {
                clearFields();
                JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
                frame.setContentPane(new LoginPanel());
                frame.revalidate();
            });

            // Layout
            gbc.gridwidth = 2;
            gbc.insets = new Insets(5, 5, 5, 5);

            gbc.gridx = 0; gbc.gridy = 0;
            add(titleLabel, gbc);

            gbc.gridy = 1;
            add(errorLabel, gbc);

            gbc.gridwidth = 1;
            gbc.gridy = 2;
            add(new JLabel("Username:"), gbc);
            gbc.gridx = 1;
            add(usernameField, gbc);

            gbc.gridx = 0; gbc.gridy = 3;
            add(new JLabel("Password:"), gbc);
            gbc.gridx = 1;
            add(passwordField, gbc);

            gbc.gridx = 0; gbc.gridy = 4;
            add(new JLabel("Konfirmasi Password:"), gbc);
            gbc.gridx = 1;
            add(confirmPasswordField, gbc);

            gbc.gridx = 0; gbc.gridy = 5;
            add(registerButton, gbc);
            gbc.gridx = 1;
            add(backButton, gbc);
        }

        private void doRegister() {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String confirmPass = new String(confirmPasswordField.getPassword());

            if (username.isEmpty() || password.isEmpty() || confirmPass.isEmpty()) {
                errorLabel.setText("Semua field harus diisi!");
                return;
            }

            if (!password.equals(confirmPass)) {
                errorLabel.setText("Password tidak cocok!");
                return;
            }

            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3307/warteg_app_new", "root", "")) {
                PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO users (username, password) VALUES (?, ?)");
                pstmt.setString(1, username);
                pstmt.setString(2, password);

                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Registrasi berhasil! Silakan login.");
                clearFields();
                JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
                frame.setContentPane(new LoginPanel());
                frame.revalidate();
            } catch (SQLException e) {
                if (e.getMessage().contains("Duplicate entry")) {
                    errorLabel.setText("Username sudah digunakan!");
                } else {
                    errorLabel.setText("Error: " + e.getMessage());
                }
            }
        }

        private void clearFields() {
            usernameField.setText("");
            passwordField.setText("");
            confirmPasswordField.setText("");
            errorLabel.setText(" ");
        }
    }

    // Panel Menu
    static class MenuPanel extends JPanel {
        private ArrayList<JSpinner> foodSpinners = new ArrayList<>();
        private ArrayList<JSpinner> drinkSpinners = new ArrayList<>();
    
        public MenuPanel() {
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(20, 20, 20, 20));
    
            JLabel titleLabel = new JLabel("Menu Warteg", SwingConstants.CENTER);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
    
            // Create main panel with BoxLayout for better control of sections
            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
            
            // Food section
            JPanel foodSection = new JPanel(new BorderLayout());
            JLabel foodTitle = new JLabel("Makanan", SwingConstants.CENTER);
            foodTitle.setFont(new Font("Arial", Font.BOLD, 20));
            
            // Use GridLayout with fixed height for food items
            JPanel foodPanel = new JPanel(new GridLayout(0, 1, 5, 5));
            foodPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
            
            for (Food food : MenuManager.getFoods()) {
                JPanel itemPanel = createItemPanel(food);
                foodPanel.add(itemPanel);
            }
            
            foodSection.add(foodTitle, BorderLayout.NORTH);
            foodSection.add(new JScrollPane(foodPanel), BorderLayout.CENTER);
            
            // Drink section
            JPanel drinkSection = new JPanel(new BorderLayout());
            JLabel drinkTitle = new JLabel("Minuman", SwingConstants.CENTER);
            drinkTitle.setFont(new Font("Arial", Font.BOLD, 20));
            
            // Use GridLayout with fixed height for drink items
            JPanel drinkPanel = new JPanel(new GridLayout(0, 1, 5, 5));
            drinkPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
            
            for (Drink drink : MenuManager.getDrinks()) {
                JPanel itemPanel = createItemPanel(drink);
                drinkPanel.add(itemPanel);
            }
            
            drinkSection.add(drinkTitle, BorderLayout.NORTH);
            drinkSection.add(new JScrollPane(drinkPanel), BorderLayout.CENTER);
    
            // Add sections to main panel with consistent spacing
            mainPanel.add(foodSection);
            mainPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Consistent spacing
            mainPanel.add(drinkSection);
    
            // Wrap in scroll pane
            JScrollPane mainScrollPane = new JScrollPane(mainPanel);
            mainScrollPane.getVerticalScrollBar().setUnitIncrement(16);
    
            JButton orderButton = new JButton("Pesan");
            orderButton.addActionListener(e -> showOrderConfirmation());
    
            add(titleLabel, BorderLayout.NORTH);
            add(mainScrollPane, BorderLayout.CENTER);
            add(orderButton, BorderLayout.SOUTH);
        }
    
        private JPanel createItemPanel(MenuItem item) {
            JPanel itemPanel = new JPanel();
            itemPanel.setLayout(new BorderLayout(10, 0));
            itemPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ));
            
            // Fixed height for consistent spacing
            itemPanel.setPreferredSize(new Dimension(itemPanel.getPreferredSize().width, 70));
            itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
    
            // Left panel for image
            JPanel imagePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            ImageIcon menuImage = ImageHandler.loadImageForItem(item.getName());
            JLabel imageLabel = new JLabel(menuImage);
            imageLabel.setPreferredSize(new Dimension(60, 60));
            imagePanel.add(imageLabel);
    
            // Center panel for name and price
            JPanel infoPanel = new JPanel();
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
            JLabel nameLabel = new JLabel(item.getName());
            nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
            nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel priceLabel = new JLabel("Rp. " + item.getPrice());
            priceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            infoPanel.add(nameLabel);
            infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            infoPanel.add(priceLabel);
            infoPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    
            // Right panel for spinner
            JPanel spinnerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JSpinner spinner = new JSpinner(new SpinnerNumberModel(0, 0, 10, 1));
            spinner.setPreferredSize(new Dimension(60, 25));
            if (item instanceof Food) {
                foodSpinners.add(spinner);
            } else {
                drinkSpinners.add(spinner);
            }
            spinnerPanel.add(spinner);
    
            itemPanel.add(imagePanel, BorderLayout.WEST);
            itemPanel.add(infoPanel, BorderLayout.CENTER);
            itemPanel.add(spinnerPanel, BorderLayout.EAST);
    
            return itemPanel;
        }

        private void showOrderConfirmation() {
            StringBuilder orderSummary = new StringBuilder("Pesanan Anda:\n\nMakanan:\n");
            int total = 0;

            // Process food orders
            ArrayList<Food> foods = MenuManager.getFoods();
            for (int i = 0; i < foods.size(); i++) {
                int quantity = (int) foodSpinners.get(i).getValue();
                if (quantity > 0) {
                    Food food = foods.get(i);
                    orderSummary.append("- ").append(food.getName()).append(" x ").append(quantity)
                               .append(" = Rp. ").append(food.getPrice() * quantity).append("\n");
                    total += food.getPrice() * quantity;
                }
            }

            // Process drink orders
            orderSummary.append("\nMinuman:\n");
            ArrayList<Drink> drinks = MenuManager.getDrinks();
            for (int i = 0; i < drinks.size(); i++) {
                int quantity = (int) drinkSpinners.get(i).getValue();
                if (quantity > 0) {
                    Drink drink = drinks.get(i);
                    orderSummary.append("- ").append(drink.getName()).append(" x ").append(quantity)
                               .append(" = Rp. ").append(drink.getPrice() * quantity).append("\n");
                    total += drink.getPrice() * quantity;
                }
            }

            orderSummary.append("\nTotal: Rp. ").append(total);
            
            if (total > 0) {
                JOptionPane.showMessageDialog(this, orderSummary.toString(),
                        "Konfirmasi Pesanan", JOptionPane.INFORMATION_MESSAGE);

                JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
                frame.setContentPane(new ConfirmationPanel(orderSummary.toString()));
                frame.revalidate();
            } else {
                JOptionPane.showMessageDialog(this, "Silakan pilih menu terlebih dahulu",
                        "Peringatan", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    // Panel Konfirmasi
    static class ConfirmationPanel extends JPanel {
        public ConfirmationPanel(String orderSummary) {
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(20, 20, 20, 20));

            JLabel titleLabel = new JLabel("Konfirmasi Pesanan", SwingConstants.CENTER);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 24));

            JTextArea orderDetails = new JTextArea(10, 40);
            orderDetails.setText(orderSummary);
            orderDetails.setEditable(false);
            orderDetails.setWrapStyleWord(true);
            orderDetails.setLineWrap(true);

            JScrollPane scrollPane = new JScrollPane(orderDetails);

            // Panel for buttons
            JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
            
            JButton cancelButton = new JButton("Batalkan");
            cancelButton.addActionListener(e -> {
                int result = JOptionPane.showConfirmDialog(this,
                    "Apakah Anda yakin ingin membatalkan pesanan?",
                    "Konfirmasi Pembatalan",
                    JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
                    frame.setContentPane(new MenuPanel());
                    frame.revalidate();
                }
            });

            JButton finishButton = new JButton("Selesai");
            finishButton.addActionListener(e -> {
                saveOrder(orderSummary); // Save order to database
                JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
                frame.dispose();
            });

            buttonPanel.add(cancelButton);
            buttonPanel.add(finishButton);

            add(titleLabel, BorderLayout.NORTH);
            add(scrollPane, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.SOUTH);
        }

        private void saveOrder(String orderSummary) {
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3307/warteg_app_new", "root", "")) {
                PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO orders (order_details, order_date) VALUES (?, NOW())");
                pstmt.setString(1, orderSummary);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error saving order: " + e.getMessage());
            }
        }
    }

    // Admin Panel
    static class AdminPanel extends JPanel {
        private JTable orderTable;
        private JTable menuTable;
        private DefaultTableModel orderModel;
        private DefaultTableModel menuModel;
        
        public AdminPanel() {
            setLayout(new BorderLayout(10, 10));
            setBorder(new EmptyBorder(20, 20, 20, 20));
            
            JTabbedPane tabbedPane = new JTabbedPane();
            
            // Orders panel
            JPanel ordersPanel = createOrdersPanel();
            tabbedPane.addTab("Daftar Pesanan", ordersPanel);
            
            // Menu management panel
            JPanel menuPanel = createMenuManagementPanel();
            tabbedPane.addTab("Kelola Menu", menuPanel);
            
            add(tabbedPane, BorderLayout.CENTER);
            
            // Logout button
            JButton logoutButton = new JButton("Logout");
            logoutButton.addActionListener(e -> {
                JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
                frame.setContentPane(new LoginPanel());
                frame.revalidate();
            });
            add(logoutButton, BorderLayout.SOUTH);
            
            // Initial load
            loadOrders();
            loadMenu();
        }
        
        private JPanel createOrdersPanel() {
            JPanel panel = new JPanel(new BorderLayout(5, 5));
            
            // Membuat model tabel dengan kolom yang diperbarui
            String[] columns = {"ID Pesanan", "Username", "Detail Pesanan", "Total", "Tanggal"};
            orderModel = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false; // Membuat semua sel tidak bisa diedit
                }
            };
            orderTable = new JTable(orderModel);
            
            // Mengatur lebar kolom
            orderTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
            orderTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Username
            orderTable.getColumnModel().getColumn(2).setPreferredWidth(300); // Detail
            orderTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Total
            orderTable.getColumnModel().getColumn(4).setPreferredWidth(150); // Tanggal
            
            // Menambahkan fungsi pencarian
            JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JTextField searchField = new JTextField(20);
            JButton searchButton = new JButton("Cari");
            searchButton.addActionListener(e -> searchOrders(searchField.getText()));
            
            searchPanel.add(new JLabel("Cari Pesanan: "));
            searchPanel.add(searchField);
            searchPanel.add(searchButton);
            
            JScrollPane scrollPane = new JScrollPane(orderTable);
            panel.add(searchPanel, BorderLayout.NORTH);
            panel.add(scrollPane, BorderLayout.CENTER);
            
            // Tombol refresh
            JButton refreshButton = new JButton("Refresh");
            refreshButton.addActionListener(e -> loadOrders());
            panel.add(refreshButton, BorderLayout.SOUTH);
            
            return panel;
        }
        
        private JPanel createMenuManagementPanel() {
            JPanel panel = new JPanel(new BorderLayout(5, 5));
            
            String[] columns = {"ID", "Nama", "Harga", "Tipe", "Status"};
            menuModel = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            menuTable = new JTable(menuModel);
            
            JScrollPane scrollPane = new JScrollPane(menuTable);
            panel.add(scrollPane, BorderLayout.CENTER);
            
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton addButton = new JButton("Tambah Menu");
            JButton editButton = new JButton("Edit Menu");
            JButton deleteButton = new JButton("Hapus Menu");
            JButton toggleButton = new JButton("Toggle Status");
            
            addButton.addActionListener(e -> showAddItemDialog());
            editButton.addActionListener(e -> editSelectedItem());
            deleteButton.addActionListener(e -> deleteSelectedItem());
            toggleButton.addActionListener(e -> toggleSelectedItemStatus());
            
            buttonPanel.add(addButton);
            buttonPanel.add(editButton);
            buttonPanel.add(deleteButton);
            buttonPanel.add(toggleButton);
            panel.add(buttonPanel, BorderLayout.SOUTH);
            
            return panel;
        }

        private void loadOrders() {
            orderModel.setRowCount(0);
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3307/warteg_app_new", "root", "")) {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(
                    "SELECT orders.*, users.username " +
                    "FROM orders " +
                    "LEFT JOIN users ON orders.username = users.username " +
                    "ORDER BY order_date DESC"
                );
                
                while (rs.next()) {
                    Object[] row = {
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("menu_item") + " x" + rs.getInt("quantity"),
                        "Rp. " + rs.getInt("total_price"),
                        rs.getTimestamp("order_date")
                    };
                    orderModel.addRow(row);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error loading orders: " + e.getMessage());
            }
        }

        private void loadMenu() {
            menuModel.setRowCount(0);
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3307/warteg_app_new", "root", "")) {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM menu ORDER BY kategori, nama");
                
                while (rs.next()) {
                    Object[] row = {
                        rs.getInt("id"),
                        rs.getString("nama"),
                        "Rp. " + rs.getInt("harga"),
                        rs.getString("kategori"),
                        "Tersedia" // Status availability bisa ditambahkan nanti jika diperlukan
                    };
                    menuModel.addRow(row);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error loading menu: " + e.getMessage());
            }
        }

        private void searchOrders(String query) {
            orderModel.setRowCount(0); // Clear existing rows
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3307/warteg_app_new", "root", "")) {
                PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT orders.*, users.username " +
                    "FROM orders " +
                    "LEFT JOIN users ON orders.username = users.username " +
                    "WHERE users.username LIKE ? OR orders.menu_item LIKE ? " +
                    "ORDER BY order_date DESC"
                );
                pstmt.setString(1, "%" + query + "%");
                pstmt.setString(2, "%" + query + "%");
                
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    Object[] row = {
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("menu_item") + " x" + rs.getInt("quantity"),
                        "Rp. " + rs.getInt("total_price"),
                        rs.getTimestamp("order_date")
                    };
                    orderModel.addRow(row);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error searching orders: " + e.getMessage());
            }
        }

        private void showAddItemDialog() {
            JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Tambah Menu", true);
            dialog.setLayout(new GridLayout(0, 2, 5, 5));
            
            JTextField nameField = new JTextField();
            JTextField priceField = new JTextField();
            String[] categories = {"Makanan", "Minuman"};
            JComboBox<String> categoryCombo = new JComboBox<>(categories);
            JTextField imagePathField = new JTextField();
            JButton browseButton = new JButton("Browse");
            
            dialog.add(new JLabel("Nama:"));
            dialog.add(nameField);
            dialog.add(new JLabel("Harga:"));
            dialog.add(priceField);
            dialog.add(new JLabel("Kategori:"));
            dialog.add(categoryCombo);
            dialog.add(new JLabel("Image Path:"));
            JPanel imagePanel = new JPanel(new BorderLayout());
            imagePanel.add(imagePathField, BorderLayout.CENTER);
            imagePanel.add(browseButton, BorderLayout.EAST);
            dialog.add(imagePanel);
            
            browseButton.addActionListener(e -> {
                JFileChooser fileChooser = new JFileChooser();
                if (fileChooser.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                    imagePathField.setText(fileChooser.getSelectedFile().getPath());
                }
            });
            
            JButton saveButton = new JButton("Simpan");
            saveButton.addActionListener(e -> {
                try {
                    String nama = nameField.getText();
                    int harga = Integer.parseInt(priceField.getText());
                    String kategori = (String) categoryCombo.getSelectedItem();
                    String image = imagePathField.getText();
                    
                    if (nama.isEmpty()) {
                        JOptionPane.showMessageDialog(dialog, "Nama menu harus diisi!");
                        return;
                    }
                    
                    try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3307/warteg_app_new", "root", "")) {
                        PreparedStatement pstmt = conn.prepareStatement(
                            "INSERT INTO menu (nama, harga, kategori, image) VALUES (?, ?, ?, ?)"
                        );
                        pstmt.setString(1, nama);
                        pstmt.setInt(2, harga);
                        pstmt.setString(3, kategori);
                        pstmt.setString(4, image);
                        pstmt.executeUpdate();
                        
                        dialog.dispose();
                        loadMenu();
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(dialog, "Error saving menu: " + ex.getMessage());
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "Harga harus berupa angka!");
                }
            });
            
            dialog.add(new JPanel()); // Spacing
            dialog.add(saveButton);
            
            dialog.pack();
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
        }

        private void editSelectedItem() {
            int row = menuTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Pilih menu yang akan diedit!");
                return;
            }
            
            int menuId = (int) menuTable.getValueAt(row, 0);
            String currentName = (String) menuTable.getValueAt(row, 1);
            int currentPrice = Integer.parseInt(((String) menuTable.getValueAt(row, 2)).replace("Rp. ", ""));
            String currentCategory = (String) menuTable.getValueAt(row, 3);
            
            JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Edit Menu", true);
            dialog.setLayout(new GridLayout(0, 2, 5, 5));
            
            JTextField nameField = new JTextField(currentName);
            JTextField priceField = new JTextField(String.valueOf(currentPrice));
            String[] categories = {"Makanan", "Minuman"};
            JComboBox<String> categoryCombo = new JComboBox<>(categories);
            categoryCombo.setSelectedItem(currentCategory);
            
            dialog.add(new JLabel("Nama:"));
            dialog.add(nameField);
            dialog.add(new JLabel("Harga:"));
            dialog.add(priceField);
            dialog.add(new JLabel("Kategori:"));
            dialog.add(categoryCombo);
            
            JButton saveButton = new JButton("Update");
            saveButton.addActionListener(e -> {
                try {
                    String nama = nameField.getText();
                    int harga = Integer.parseInt(priceField.getText());
                    String kategori = (String) categoryCombo.getSelectedItem();
                    
                    if (nama.isEmpty()) {
                        JOptionPane.showMessageDialog(dialog, "Nama menu harus diisi!");
                        return;
                    }
                    
                    try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3307/warteg_app_new", "root", "")) {
                        PreparedStatement pstmt = conn.prepareStatement(
                            "UPDATE menu SET nama = ?, harga = ?, kategori = ? WHERE id = ?"
                        );
                        pstmt.setString(1, nama);
                        pstmt.setInt(2, harga);
                        pstmt.setString(3, kategori);
                        pstmt.setInt(4, menuId);
                        pstmt.executeUpdate();
                        
                        dialog.dispose();
                        loadMenu();
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(dialog, "Error updating menu: " + ex.getMessage());
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "Harga harus berupa angka!");
                }
            });
            
            dialog.add(new JPanel()); // Spacing
            dialog.add(saveButton);
            
            dialog.pack();
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
        }
        private void deleteSelectedItem() {
            int row = menuTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Pilih menu yang akan dihapus!");
                return;
            }
            
            int menuId = (int) menuTable.getValueAt(row, 0);
            String menuName = (String) menuTable.getValueAt(row, 1);
            
            int confirm = JOptionPane.showConfirmDialog(this,
                "Apakah Anda yakin ingin menghapus menu '" + menuName + "'?",
                "Konfirmasi Hapus",
                JOptionPane.YES_NO_OPTION);
                
            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3307/warteg_app_new", "root", "")) {
                    // Periksa apakah menu masih digunakan dalam orders
                    PreparedStatement checkStmt = conn.prepareStatement(
                        "SELECT COUNT(*) FROM orders WHERE menu_item = ?"
                    );
                    checkStmt.setString(1, menuName);
                    ResultSet rs = checkStmt.executeQuery();
                    rs.next();
                    int count = rs.getInt(1);
                    
                    if (count > 0) {
                        // Jika menu masih ada di orders, tampilkan peringatan
                        JOptionPane.showMessageDialog(this, 
                            "Menu ini tidak dapat dihapus karena masih terdapat dalam riwayat pesanan.",
                            "Peringatan",
                            JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    
                    // Jika aman, lakukan penghapusan
                    PreparedStatement deleteStmt = conn.prepareStatement(
                        "DELETE FROM menu WHERE id = ?"
                    );
                    deleteStmt.setInt(1, menuId);
                    deleteStmt.executeUpdate();
                    
                    JOptionPane.showMessageDialog(this, 
                        "Menu berhasil dihapus!",
                        "Sukses",
                        JOptionPane.INFORMATION_MESSAGE);
                    
                    loadMenu(); // Refresh tabel menu
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this, 
                        "Error menghapus menu: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        private void toggleSelectedItemStatus() {
            int row = menuTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Pilih menu yang akan diubah statusnya!");
                return;
            }

            int menuId = (int) menuTable.getValueAt(row, 0);
            String currentStatus = (String) menuTable.getValueAt(row, 4); // Assuming status is in the 5th column

            String newStatus = currentStatus.equals("Tersedia") ? "Tidak Tersedia" : "Tersedia";

            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3307/warteg_app_new", "root", "")) {
                PreparedStatement pstmt = conn.prepareStatement(
                    "UPDATE menu SET status = ? WHERE id = ?"
                );
                pstmt.setString(1, newStatus);
                pstmt.setInt(2, menuId);
                pstmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "Status menu berhasil diubah menjadi: " + newStatus);
                loadMenu(); // Refresh the menu table
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error toggling item status: " + e.getMessage());
            }
        }
    }

    // Image Handler
    static class ImageHandler {
        private static final String BASE_PATH = "resources/";
        private static final Map<String, String> IMAGE_MAP = new HashMap<>();

        static {
            // Foods
            IMAGE_MAP.put("ayam", "ayam.jpeg");
            IMAGE_MAP.put("ikan", "ikan.jpeg");
            IMAGE_MAP.put("nasi", "nasi.jpeg");
            IMAGE_MAP.put("sambel", "sambel.jpeg");
            IMAGE_MAP.put("sayur asem", "sayur_asem.jpeg");
            IMAGE_MAP.put("sayur lodeh", "sayur_lodeh.jpeg");
            IMAGE_MAP.put("tahu tempe", "tahu_tempe.jpeg");

            // Drinks
            IMAGE_MAP.put("es jeruk", "es_jeruk.jpg");
            IMAGE_MAP.put("air putih", "air_putih.jpg");
        }

        public static ImageIcon loadImageForItem(String menuName) {
            String imageName = IMAGE_MAP.getOrDefault(menuName.toLowerCase(), "placeholder.jpeg");
            String imagePath = BASE_PATH + imageName;

            File imageFile = new File(imagePath);
            if (!imageFile.exists()) {
                return new ImageIcon(BASE_PATH + "placeholder.jpeg");
            }

            Image img = new ImageIcon(imagePath).getImage();
            Image scaledImg = img.getScaledInstance(60, 60, Image.SCALE_SMOOTH);
            return new ImageIcon(scaledImg);
        }
    }
}