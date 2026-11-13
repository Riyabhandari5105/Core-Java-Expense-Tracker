
import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

public class ExpenseTracker {

    static final String FILE = "expenses.csv";
    static ArrayList<Expense> expenses = new ArrayList<>();
    static Scanner sc = new Scanner(System.in);
    static int nextId = 1;

    static class Expense {
        int id;
        LocalDate date;
        BigDecimal amount;
        String category;
        String description;

        Expense(int id, LocalDate date, BigDecimal amount,
                String category, String description) {
            this.id = id;
            this.date = date;
            this.amount = amount;
            this.category = category;
            this.description = description;
        }
    }

    public static void main(String[] args) {
        loadExpenses();

        while (true) {
            System.out.println("\n===== EXPENSE TRACKER =====");
            System.out.println("1. Add Expense");
            System.out.println("2. View Expenses");
            System.out.println("3. Update Expense");
            System.out.println("4. Delete Expense");
            System.out.println("5. Total Expenses");
            System.out.println("6. Category-wise Summary");
            System.out.println("7. Exit");

            System.out.print("Enter your choice: ");
            String choice = sc.nextLine();

            switch (choice) {
                case "1": addExpense(); break;
                case "2": viewExpenses(); break;
                case "3": updateExpense(); break;
                case "4": deleteExpense(); break;
                case "5": showTotal(); break;
                case "6": categorySummary(); break;
                case "7":
                    saveExpenses();
                    System.out.println("Thank you for using Expense Tracker!");
                    return;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    static void addExpense() {
        try {
            System.out.print("Enter date (YYYY-MM-DD): ");
            LocalDate date = LocalDate.parse(sc.nextLine());

            System.out.print("Enter amount: ");
            BigDecimal amount = new BigDecimal(sc.nextLine());

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                System.out.println("Amount must be greater than zero.");
                return;
            }

            System.out.print("Enter category: ");
            String category = sc.nextLine().trim();

            System.out.print("Enter description: ");
            String description = sc.nextLine().trim();

            if (category.isEmpty() || description.isEmpty()) {
                System.out.println("Category and description are required.");
                return;
            }

            expenses.add(new Expense(nextId++, date, amount,
                    category, description));

            saveExpenses();
            System.out.println("Expense added successfully!");

        } catch (Exception e) {
            System.out.println("Invalid input. Please try again.");
        }
    }

    static void viewExpenses() {
        if (expenses.isEmpty()) {
            System.out.println("No expenses found.");
            return;
        }

        System.out.println("\nID | Date | Amount | Category | Description");

        for (Expense e : expenses) {
            System.out.println(e.id + " | " + e.date + " | "
                    + e.amount + " | " + e.category + " | "
                    + e.description);
        }
    }

    static void updateExpense() {
        System.out.print("Enter expense ID to update: ");
        int id = readId();

        Expense e = findExpense(id);

        if (e == null) {
            System.out.println("Expense not found.");
            return;
        }

        try {
            System.out.print("Enter new date (YYYY-MM-DD): ");
            LocalDate date = LocalDate.parse(sc.nextLine());

            System.out.print("Enter new amount: ");
            BigDecimal amount = new BigDecimal(sc.nextLine());

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                System.out.println("Amount must be positive.");
                return;
            }

            System.out.print("Enter new category: ");
            String category = sc.nextLine().trim();

            System.out.print("Enter new description: ");
            String description = sc.nextLine().trim();

            if (category.isEmpty() || description.isEmpty()) {
                System.out.println("Fields cannot be empty.");
                return;
            }

            e.date = date;
            e.amount = amount;
            e.category = category;
            e.description = description;

            saveExpenses();
            System.out.println("Expense updated successfully!");

        } catch (Exception ex) {
            System.out.println("Invalid input. Update cancelled.");
        }
    }

    static void deleteExpense() {
        System.out.print("Enter expense ID to delete: ");
        int id = readId();

        Expense e = findExpense(id);

        if (e == null) {
            System.out.println("Expense not found.");
            return;
        }

        expenses.remove(e);
        saveExpenses();
        System.out.println("Expense deleted successfully!");
    }

    static void showTotal() {
        BigDecimal total = BigDecimal.ZERO;

        for (Expense e : expenses) {
            total = total.add(e.amount);
        }

        System.out.println("Total Expenses: " + total);
    }

    static void categorySummary() {
        Map<String, BigDecimal> summary = new TreeMap<>();

        for (Expense e : expenses) {
            summary.merge(e.category, e.amount, BigDecimal::add);
        }

        if (summary.isEmpty()) {
            System.out.println("No expense records available.");
            return;
        }

        System.out.println("\nCategory-wise Expenses:");

        for (Map.Entry<String, BigDecimal> entry : summary.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }
    }

    static Expense findExpense(int id) {
        for (Expense e : expenses) {
            if (e.id == id) return e;
        }
        return null;
    }

    static int readId() {
        try {
            return Integer.parseInt(sc.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    static void saveExpenses() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE))) {
            for (Expense e : expenses) {
                writer.println(e.id + "," + e.date + ","
                        + e.amount + ","
                        + encode(e.category) + ","
                        + encode(e.description));
            }
        } catch (IOException e) {
            System.out.println("Error saving expenses.");
        }
    }

    static void loadExpenses() {
        File file = new File(FILE);

        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",", -1);

                if (data.length == 5) {
                    int id = Integer.parseInt(data[0]);

                    expenses.add(new Expense(
                            id,
                            LocalDate.parse(data[1]),
                            new BigDecimal(data[2]),
                            decode(data[3]),
                            decode(data[4])
                    ));

                    nextId = Math.max(nextId, id + 1);
                }
            }
        } catch (Exception e) {
            System.out.println("Error loading expense records.");
        }
    }

    static String encode(String value) {
        return Base64.getEncoder().encodeToString(
                value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    static String decode(String value) {
        return new String(Base64.getDecoder().decode(value),
                java.nio.charset.StandardCharsets.UTF_8);
    }
}
