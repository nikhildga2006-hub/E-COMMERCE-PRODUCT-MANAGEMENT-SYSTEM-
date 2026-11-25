
import java.util.*;

class Product {
    int id;
    String name;
    double price;
    int stock;

    Product(int id, String name, double price, int stock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    void display() {
        System.out.printf("%-6d %-22s Rs.%-10.2f %-8d%n",
                id, name, price, stock);
    }
}

class CustomerOrder {
    int orderId;
    int productId;
    String productName;
    int quantity;
    double total;

    CustomerOrder(int orderId, int productId, String productName,
                  int quantity, double total) {
        this.orderId = orderId;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.total = total;
    }

    void display() {
        System.out.println("------------------------------------------");
        System.out.println("Order ID     : " + orderId);
        System.out.println("Product      : " + productName);
        System.out.println("Quantity     : " + quantity);
        System.out.printf("Total        : Rs. %.2f%n", total);
        System.out.println("------------------------------------------");
    }
}

public class ECP {

    static Scanner sc = new Scanner(System.in);

    // ArrayList stores products; HashMap supports fast ID lookup.
    static ArrayList<Product> products = new ArrayList<>();
    static HashMap<Integer, Product> productMap = new HashMap<>();

    // Queue processes orders in FIFO order.
    static Queue<CustomerOrder> pendingOrders = new LinkedList<>();

    // Stack stores the latest activity at the top (LIFO).
    static Stack<String> recentActivities = new Stack<>();

    static int nextOrderId = 5001;

    static String readLine(String message) {
        System.out.print(message);
        return sc.nextLine().trim();
    }

    static int readInt(String message) {
        try {
            return Integer.parseInt(readLine(message));
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Please enter a valid whole number.");
            return -1;
        }
    }

    static double readDouble(String message) {
        try {
            return Double.parseDouble(readLine(message));
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Please enter a valid number.");
            return -1;
        }
    }

    static void addProduct() {
        int id = readInt("Enter product ID: ");

        if (id <= 0 || productMap.containsKey(id)) {
            System.out.println("[ERROR] ID must be positive and unique.");
            return;
        }

        String name = readLine("Enter product name: ");
        double price = readDouble("Enter price (Rs.): ");
        int stock = readInt("Enter stock quantity: ");

        if (name.isEmpty() || price <= 0 || stock < 0) {
            System.out.println("[ERROR] Enter a name, positive price, and non-negative stock.");
            return;
        }

        Product product = new Product(id, name, price, stock);

        products.add(product);
        productMap.put(id, product);

        recentActivities.push("Added product: " + name + " (ID " + id + ")");

        System.out.println("[SUCCESS] Product added.");
    }

    static void displayProducts() {
        if (products.isEmpty()) {
            System.out.println("[INFO] No products available.");
            return;
        }

        System.out.println("\n================ PRODUCT CATALOG ================");
        System.out.printf("%-6s %-22s %-13s %-8s%n",
                "ID", "PRODUCT", "PRICE", "STOCK");
        System.out.println("-------------------------------------------------");

        for (Product p : products) {
            p.display();
        }

        System.out.println("=================================================");
    }

    static void searchProduct() {
        int id = readInt("Enter product ID to search: ");

        Product p = productMap.get(id);

        if (p == null) {
            System.out.println("[NOT FOUND] No product has that ID.");
        } else {
            System.out.println("[PRODUCT FOUND]");
            System.out.printf("%-6s %-22s %-13s %-8s%n",
                    "ID", "PRODUCT", "PRICE", "STOCK");
            p.display();
        }
    }

    static void updateProduct() {
        int id = readInt("Enter product ID to update: ");

        Product p = productMap.get(id);

        if (p == null) {
            System.out.println("[NOT FOUND] Product does not exist.");
            return;
        }

        String newName = readLine(
                "Enter new name (press Enter to keep current): ");

        double newPrice = readDouble(
                "Enter new price (0 to keep current): ");

        int newStock = readInt(
                "Enter new stock (-1 to keep current): ");

        if (!newName.isEmpty()) {
            p.name = newName;
        }

        if (newPrice > 0) {
            p.price = newPrice;
        }

        if (newStock >= 0) {
            p.stock = newStock;
        }

        recentActivities.push(
                "Updated product: " + p.name + " (ID " + id + ")");

        System.out.println("[SUCCESS] Product details updated.");
    }

    static void deleteProduct() {
        int id = readInt("Enter product ID to delete: ");

        Product p = productMap.remove(id);

        if (p == null) {
            System.out.println("[NOT FOUND] Product does not exist.");
            return;
        }

        products.remove(p);

        recentActivities.push(
                "Deleted product: " + p.name + " (ID " + id + ")");

        System.out.println("[SUCCESS] Product deleted.");
    }

    static void sortByPrice() {
        if (products.isEmpty()) {
            System.out.println("[INFO] No products to sort.");
            return;
        }

        products.sort(Comparator.comparingDouble(p -> p.price));

        recentActivities.push("Sorted product catalog by price");

        System.out.println(
                "[SUCCESS] Products sorted from low to high price.");

        displayProducts();
    }

    static void placeOrder() {
        int id = readInt("Enter product ID to order: ");

        Product p = productMap.get(id);

        if (p == null) {
            System.out.println("[ERROR] Product not found.");
            return;
        }

        int quantity = readInt("Enter quantity: ");

        if (quantity <= 0 || quantity > p.stock) {
            System.out.println(
                    "[ERROR] Quantity must be positive and within available stock.");
            return;
        }

        double total = p.price * quantity;

        p.stock -= quantity;

        CustomerOrder order = new CustomerOrder(
                nextOrderId++, p.id, p.name, quantity, total);

        pendingOrders.add(order);

        recentActivities.push(
                "Placed order " + order.orderId + " for " + p.name);

        System.out.println("\n[ORDER PLACED SUCCESSFULLY]");

        order.display();

        System.out.println("Remaining stock: " + p.stock);
    }

    static void viewPendingOrders() {
        if (pendingOrders.isEmpty()) {
            System.out.println("[INFO] No pending orders.");
            return;
        }

        System.out.println("\n========== PENDING ORDERS (FIFO) ==========");

        for (CustomerOrder order : pendingOrders) {
            order.display();
        }
    }

    static void processNextOrder() {
        if (pendingOrders.isEmpty()) {
            System.out.println("[INFO] No order is waiting.");
            return;
        }

        CustomerOrder order = pendingOrders.poll();

        System.out.println("[SUCCESS] The next order has been processed.");

        order.display();

        recentActivities.push("Processed order " + order.orderId);
    }

    static void viewRecentActivity() {
        if (recentActivities.isEmpty()) {
            System.out.println("[INFO] No activity recorded.");
            return;
        }

        System.out.println("\n======= RECENT ACTIVITY (LATEST FIRST) =======");

        for (int i = recentActivities.size() - 1; i >= 0; i--) {
            System.out.println((recentActivities.size() - i) + ". "
                    + recentActivities.get(i));
        }
    }

    static void loadSampleProducts() {
        addSample(new Product(101, "Wireless Mouse", 599.00, 20));
        addSample(new Product(102, "Keyboard", 899.00, 15));
        addSample(new Product(103, "Headphones", 1299.00, 12));
        addSample(new Product(104, "USB Drive", 499.00, 25));
        addSample(new Product(105, "Smart Watch", 2499.00, 8));
    }

    static void addSample(Product p) {
        products.add(p);
        productMap.put(p.id, p);
    }

    static void showMenu() {
        System.out.println("\n============================================");
        System.out.println("       E-COMMERCE PRODUCT MANAGEMENT SYSTEM ");
        System.out.println("             MANAGE PRODUCTS SMARTLY");
        System.out.println("============================================");

        System.out.println("1.  Add Product");
        System.out.println("2.  Display All Products");
        System.out.println("3.  Search Product");
        System.out.println("4.  Update Product");
        System.out.println("5.  Delete Product");
        System.out.println("6.  Sort Products by Price");
        System.out.println("7.  Place Customer Order");
        System.out.println("8.  View Pending Orders");
        System.out.println("9.  Process Next Order");
        System.out.println("10. View Recent Activities");
        System.out.println("11. Exit");

        System.out.println("============================================");
    }

    public static void main(String[] args) {

        loadSampleProducts();

        System.out.println(
                "Welcome to the E-Commerce Product Management System!");

        while (true) {

            showMenu();

            int choice = readInt("Select an option (1-11): ");

            switch (choice) {

                case 1:
                    addProduct();
                    break;

                case 2:
                    displayProducts();
                    break;

                case 3:
                    searchProduct();
                    break;

                case 4:
                    updateProduct();
                    break;

                case 5:
                    deleteProduct();
                    break;

                case 6:
                    sortByPrice();
                    break;

                case 7:
                    placeOrder();
                    break;

                case 8:
                    viewPendingOrders();
                    break;

                case 9:
                    processNextOrder();
                    break;

                case 10:
                    viewRecentActivity();
                    break;

                case 11:
                    System.out.println(
                            "Thank you for using the system!");
                    sc.close();
                    return;

                default:
                    System.out.println(
                            "[ERROR] Choose a number from 1 to 11.");
            }
        }
    }
}