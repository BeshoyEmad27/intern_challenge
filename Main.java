import java.time.LocalDate;
import java.util.*;

// ===== Interfaces =====
interface Shippable {
    String getName();
    double getWeight();
}


abstract class Product {
    protected String name;
    protected double price;
    protected int quantity;

    public Product(String name, double price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public boolean isExpired() {
        return false;
    }

    public boolean isShippable() {
        return this instanceof Shippable;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void reduceQuantity(int amount) {
        quantity -= amount;
    }
}

class ExpirableProduct extends Product {
    private LocalDate expiryDate;

    public ExpirableProduct(String name, double price, int quantity, LocalDate expiryDate) {
        super(name, price, quantity);
        this.expiryDate = expiryDate;
    }

    @Override
    public boolean isExpired() {
        return expiryDate.isBefore(LocalDate.now());
    }
}

class Cheese extends ExpirableProduct implements Shippable {
    private double weight;

    public Cheese(String name, double price, int quantity, LocalDate expiryDate, double weight) {
        super(name, price, quantity, expiryDate);
        this.weight = weight;
    }

    public double getWeight() {
        return weight;
    }
}

class Biscuits extends ExpirableProduct implements Shippable {
    private double weight;

    public Biscuits(String name, double price, int quantity, LocalDate expiryDate, double weight) {
        super(name, price, quantity, expiryDate);
        this.weight = weight;
    }

    public double getWeight() {
        return weight;
    }
}

class TV extends Product implements Shippable {
    private double weight;

    public TV(String name, double price, int quantity, double weight) {
        super(name, price, quantity);
        this.weight = weight;
    }

    public double getWeight() {
        return weight;
    }
}

class MobileScratchCard extends Product {
    public MobileScratchCard(String name, double price, int quantity) {
        super(name, price, quantity);
    }
}

// ===== Cart and Checkout =====
class CartItem {
    Product product;
    int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }
}

class Cart {
    private List<CartItem> items = new ArrayList<>();

    public void add(Product product, int quantity) {
        if (quantity > product.getQuantity()) {
            System.out.println("Not enough stock for product: " + product.getName());
            return;
        }
        items.add(new CartItem(product, quantity));
    }

    public List<CartItem> getItems() {
        return items;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}

class Customer {
    private String name;
    private double balance;

    public Customer(String name, double balance) {
        this.name = name;
        this.balance = balance;
    }

    public boolean hasSufficientBalance(double amount) {
        return balance >= amount;
    }

    public void pay(double amount) {
        balance -= amount;
    }

    public double getBalance() {
        return balance;
    }

    public String getName() {
        return name;
    }
}

// ===== Shipping Service =====
class ShippingService {
    public static void shipItems(List<Shippable> items) {
        System.out.println("** Shipment notice **");
        double totalWeight = 0;
        Map<String, Integer> itemCount = new HashMap<>();
        Map<String, Double> itemWeight = new HashMap<>();

        for (Shippable item : items) {
            itemCount.put(item.getName(), itemCount.getOrDefault(item.getName(), 0) + 1);
            itemWeight.put(item.getName(), item.getWeight());
            totalWeight += item.getWeight();
        }

        for (String itemName : itemCount.keySet()) {
            double weight = itemWeight.get(itemName) * itemCount.get(itemName);
            System.out.printf("%dx %s %.0fg\n", itemCount.get(itemName), itemName, weight * 1000);
        }

        System.out.printf("Total package weight %.2fkg\n", totalWeight);
    }
}

// ===== Checkout Logic =====
class Checkout {
    public static void process(Customer customer, Cart cart) {
        if (cart.isEmpty()) {
            throw new RuntimeException("Cart is empty.");
        }

        double subtotal = 0;
        double shipping = 0;
        List<Shippable> shippableItems = new ArrayList<>();

        for (CartItem item : cart.getItems()) {
            Product product = item.product;

            if (product.isExpired()) {
                throw new RuntimeException("Product expired: " + product.getName());
            }

            if (item.quantity > product.getQuantity()) {
                throw new RuntimeException("Not enough stock for product: " + product.getName());
            }

            subtotal += product.getPrice() * item.quantity;

            if (product instanceof Shippable) {
                for (int i = 0; i < item.quantity; i++) {
                    shippableItems.add((Shippable) product);
                }
            }
        }

        for (Shippable ship : shippableItems) {
            shipping += ship.getWeight() * 30; // Assume 30 EGP per kg
        }

        double total = subtotal + shipping;

        if (!customer.hasSufficientBalance(total)) {
            throw new RuntimeException("Insufficient balance.");
        }

        for (CartItem item : cart.getItems()) {
            item.product.reduceQuantity(item.quantity);
        }

        customer.pay(total);

        if (!shippableItems.isEmpty()) {
            ShippingService.shipItems(shippableItems);
        }

        System.out.println("** Checkout receipt **");
        for (CartItem item : cart.getItems()) {
            System.out.printf("%dx %s %.0f\n", item.quantity, item.product.getName(), item.product.getPrice() * item.quantity);
        }

        System.out.println("----------------------");
        System.out.printf("Subtotal %.0f\n", subtotal);
        System.out.printf("Shipping %.0f\n", shipping);
        System.out.printf("Amount %.0f\n", total);
        System.out.printf("Balance left %.0f\n", customer.getBalance());
    }
}


public class Main {
    public static void main(String[] args) {
        Product cheese = new Cheese("Cheese", 100, 10, LocalDate.now().plusDays(2), 0.2);
        Product biscuits = new Biscuits("Biscuits", 150, 5, LocalDate.now().plusDays(5), 0.7);
        Product tv = new TV("TV", 1000, 3, 5.0);
        Product scratchCard = new MobileScratchCard("Scratch Card", 50, 100);

        Customer customer = new Customer("John", 1000);
        Cart cart = new Cart();

        cart.add(cheese, 2);
        cart.add(biscuits, 1);
        cart.add(scratchCard, 1);

        try {
            Checkout.process(customer, cart);
        } catch (RuntimeException ex) {
            System.out.println("ERROR: " + ex.getMessage());
        }
    }
}
