import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class GroceryManagementSystem extends JFrame {
    // JDBC Connection variables
    private Connection connection;
    private CardLayout cardLayout;
    private JPanel mainPanel;

    // Login Panel Components
    private JTextField usernameField;
    private JPasswordField passwordField;

    // Product Panel Components
    private JTextField nameField, mrpField, discountField, stockField, expiryField;
    private JTable productTable;

    public static void main(String[] args) {
        // Setup the frame
        GroceryManagementSystem frame = new GroceryManagementSystem();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setVisible(true);
    }

    public GroceryManagementSystem() {
        try {
            // Initialize JDBC connection
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/grocerydb", "root", "Sanjay1234%");

            // Setup CardLayout and main panel
            cardLayout = new CardLayout();
            mainPanel = new JPanel(cardLayout);
            add(mainPanel);

            // Add Login Panel
            mainPanel.add(createLoginPanel(), "Login");

            // Add Product Display Panel
            mainPanel.add(createProductDisplayPanel(), "ProductDisplay");

            // Add Add Product Panel
            mainPanel.add(createAddProductPanel(), "AddProduct");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database connection failed", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Login Panel
    private JPanel createLoginPanel() {
        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new GridLayout(3, 2));

        // Username and Password fields
        loginPanel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        loginPanel.add(usernameField);

        loginPanel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        loginPanel.add(passwordField);

        // Login Button
        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

                // Simulate login check (you can replace this with actual authentication logic)
                if ("owner".equals(username) && "password".equals(password)) {
                    cardLayout.show(mainPanel, "ProductDisplay");
                } else {
                    JOptionPane.showMessageDialog(loginPanel, "Invalid credentials", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        loginPanel.add(loginButton);
        return loginPanel;
    }

    // Product Display Panel
    private JPanel createProductDisplayPanel() {
        JPanel productDisplayPanel = new JPanel();
        productDisplayPanel.setLayout(new BorderLayout());

        // Product Table
        productTable = new JTable(new DefaultTableModel(new Object[]{"ID", "Product Name", "MRP", "Discount", "Stock", "Expiry Date"}, 0));
        JScrollPane scrollPane = new JScrollPane(productTable);
        productDisplayPanel.add(scrollPane, BorderLayout.CENTER);

        // Add New Product Button
        JButton addProductButton = new JButton("Add New Product");
        addProductButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(mainPanel, "AddProduct");
            }
        });
        productDisplayPanel.add(addProductButton, BorderLayout.SOUTH);

        // Refresh table on panel load
        refreshProductTable(productTable);

        return productDisplayPanel;
    }

    // Add Product Panel
    private JPanel createAddProductPanel() {
        JPanel addProductPanel = new JPanel();
        addProductPanel.setLayout(new GridLayout(6, 2));

        // Input Fields
        addProductPanel.add(new JLabel("Product Name:"));
        nameField = new JTextField();
        addProductPanel.add(nameField);

        addProductPanel.add(new JLabel("MRP:"));
        mrpField = new JTextField();
        addProductPanel.add(mrpField);

        addProductPanel.add(new JLabel("Discount:"));
        discountField = new JTextField();
        addProductPanel.add(discountField);

        addProductPanel.add(new JLabel("Stock:"));
        stockField = new JTextField();
        addProductPanel.add(stockField);

        addProductPanel.add(new JLabel("Expiry Date (yyyy-mm-dd):"));
        expiryField = new JTextField();
        addProductPanel.add(expiryField);

        // Add Button
        JButton addButton = new JButton("Add Product");
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    // Get input values
                    String name = nameField.getText();
                    double mrp = Double.parseDouble(mrpField.getText());
                    double discount = Double.parseDouble(discountField.getText());
                    int stock = Integer.parseInt(stockField.getText());
                    String expiryDate = expiryField.getText();

                    // Insert product into database
                    String insertQuery = "INSERT INTO products (name, mrp, discount, stock, expiry_date) VALUES (?, ?, ?, ?, ?)";
                    PreparedStatement ps = connection.prepareStatement(insertQuery);
                    ps.setString(1, name);
                    ps.setDouble(2, mrp);
                    ps.setDouble(3, discount);
                    ps.setInt(4, stock);
                    ps.setString(5, expiryDate);
                    ps.executeUpdate();

                    JOptionPane.showMessageDialog(addProductPanel, "Product added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

                    // Refresh the product table
                    refreshProductTable(productTable);

                    // Switch back to the product display panel
                    cardLayout.show(mainPanel, "ProductDisplay");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(addProductPanel, "Error adding product!", "Error", JOptionPane.ERROR_MESSAGE);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(addProductPanel, "Please enter valid numeric values!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        addProductPanel.add(addButton);

        return addProductPanel;
    }

    // Method to refresh product table
    private void refreshProductTable(JTable table) {
        try {
            String query = "SELECT * FROM products";
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            // Get table model and clear existing rows
            DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
            tableModel.setRowCount(0);

            // Add rows to the table
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getDouble("mrp"),
                    rs.getDouble("discount"),
                    rs.getInt("stock"),
                    rs.getDate("expiry_date")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error refreshing product table", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
