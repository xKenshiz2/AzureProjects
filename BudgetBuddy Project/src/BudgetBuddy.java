import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

// OUTDATED: This is a simple console-based budget management application.
public class BudgetBuddy {

    private static Scanner scan = new Scanner(System.in);

    public static void main(String[] args) throws IOException {
        File userData = new File("userData.txt");
        File expenseFile = new File("expenseFile.txt");

        String name = "";
        double income = 0;
        double balance = 0;
        List<String> categories = new ArrayList<>();
        double[] percentages = null;
        List<Transaction> transactions = new ArrayList<>();

        boolean firstRun = !userData.exists();

        if (firstRun) {
            System.out.println("Welcome to BudgetBuddy! Let's set up your account.");

            System.out.print("Enter your name: ");
            name = scan.nextLine();

            income = readDouble("Enter your monthly income: $", 0, Double.MAX_VALUE);
            balance = income;

            // Category setup
            int numCategories = (int) readDouble("How many budget categories do you want? ", 1, 10);
            percentages = new double[numCategories];
            double remaining = 100;

            for (int i = 0; i < numCategories; i++) {
                System.out.print("Enter name for category #" + (i + 1) + ": ");
                String cat = scan.nextLine();
                categories.add(cat);

                if (i == numCategories - 1) {
                    percentages[i] = remaining; // last category gets leftover
                    System.out.println("Automatically assigning remaining " + remaining + "% to " + cat);
                } else {
                    double pct = readDouble("Enter percentage for " + cat + " (Remaining: " + remaining + "%): ", 0, remaining);
                    percentages[i] = pct;
                    remaining -= pct;
                }
            }

            UserDataUpdater.initializeUser(userData, name, income, categories, percentages);
            System.out.println("Setup complete! Welcome, " + name + "!");
        } else {
            // Load user data
            Map<String, Object> data = UserDataUpdater.loadUserData(userData);
            name = (String) data.get("name");
            income = (double) data.get("income");
            balance = (double) data.get("balance");
            categories = (List<String>) data.get("categories");
            List<Double> pctList = (List<Double>) data.get("percentages");
            percentages = new double[pctList.size()];
            for (int i = 0; i < pctList.size(); i++) percentages[i] = pctList.get(i);

            // Load transactions
            transactions = UserDataUpdater.loadTransactions(expenseFile);
            System.out.println("Welcome back, " + name + "!");
        }

        // Main menu
        boolean running = true;
        while (running) {
            System.out.println("\n--- BudgetBuddy Menu ---");
            System.out.println("1. View Summary");
            System.out.println("2. Add Expense");
            System.out.println("3. Allocate/Update Budget");
            System.out.println("4. Exit");
            System.out.print("Enter your choice: ");
            String choice = scan.nextLine();

            switch (choice) {
                case "1":
                    BudgetCalculator.printSummary(income, balance, categories, percentages, transactions);
                    break;

                case "2":
                    addExpense(balance, transactions, expenseFile, categories);
                    balance = recalcBalance(transactions, income); // recalc balance after expense
                    UserDataUpdater.saveUser(userData, name, income, balance, categories, percentages);
                    break;

                case "3":
                    allocateBudget(categories, percentages);
                    UserDataUpdater.saveUser(userData, name, income, balance, categories, percentages);
                    break;

                case "4":
                    System.out.println("Exiting BudgetBuddy. Have a great day!");
                    running = false;
                    break;

                default:
                    System.out.println("Invalid choice. Please select 1-4.");
            }
        }
    }

    // Helper to read double with error handling
    private static double readDouble(String prompt, double min, double max) {
        while (true) {
            System.out.print(prompt);
            try {
                double val = Double.parseDouble(scan.nextLine());
                if (val < min || val > max) {
                    System.out.println("Please enter a number between " + min + " and " + max);
                } else return val;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Enter a number.");
            }
        }
    }

    // Add an expense
    private static void addExpense(double balance, List<Transaction> transactions, File expenseFile, List<String> categories) throws IOException {
        System.out.println("Enter expense category:");
        for (int i = 0; i < categories.size(); i++) {
            System.out.println((i + 1) + ". " + categories.get(i));
        }

        int catChoice = (int) readDouble("Choice: ", 1, categories.size());
        String category = categories.get(catChoice - 1);

        System.out.print("Enter expense description: ");
        String desc = scan.nextLine();

        double amt = readDouble("Enter amount: $", 0.01, Double.MAX_VALUE);

        LocalDateTime now = LocalDateTime.now();
        Transaction tx = new Transaction(now, category, desc, amt);
        transactions.add(tx);
        UserDataUpdater.saveTransaction(expenseFile, tx);

        System.out.println("Expense added to " + category + ": $" + amt);
    }

    // Recalculate balance based on income and transactions
    private static double recalcBalance(List<Transaction> transactions, double income) {
        double totalSpent = transactions.stream().mapToDouble(Transaction::getAmount).sum();
        return income - totalSpent;
    }

    // Reallocate budget
    private static void allocateBudget(List<String> categories, double[] percentages) {
        System.out.println("=== Allocate/Update Budget ===");
        double remaining = 100;
        for (int i = 0; i < categories.size(); i++) {
            if (i == categories.size() - 1) {
                percentages[i] = remaining;
                System.out.println("Automatically assigning remaining " + remaining + "% to " + categories.get(i));
            } else {
                double pct = readDouble("Enter percentage for " + categories.get(i) + " (Remaining: " + remaining + "%): ", 0, remaining);
                percentages[i] = pct;
                remaining -= pct;
            }
        }
        System.out.println("Budget allocation updated successfully.");
    }
}
