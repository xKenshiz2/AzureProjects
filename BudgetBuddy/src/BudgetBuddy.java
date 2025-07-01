
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// MAIN CLASS
public class BudgetBuddy {
    // Scanner object for various methods throughout the program
    private static Scanner scan = new Scanner(System.in);
    
    public static void main (String[] args) throws IOException {

// Initializing Variables / Essentials
        // Creates user's file and declare variables
        File userData = new File("userData.txt");
        File expenseFile = new File("expenseFile.txt");
        int userChoice = 0;
        String name = "";
        double income = 0.0;
        boolean firstRun = false;
        
// File Creation Portion:
        // Check if a saved file exists, otherwise creates a new one for the user
        if (!(userData.exists() && userData.isFile())) {
            try {
                // create file writer object
                FileWriter writer = new FileWriter(userData);
                FileWriter expenseWriter = new FileWriter(expenseFile);

                firstRun = true;

                if (userData.length() == 0) {
                    // for user data file
                    System.out.println("This is your first time using BudgetBuddy.\nCreating file...");
                    System.out.print("Please enter your name: ");
                    name = scan.next();
                    System.out.println("\n``````````````````````` Start Help Guide ``````````````````````");
                    System.out.println("\nHello " + name + "! Welcome to BudgetBuddy! Your personal budget tracking application for all your budgeting needs!");
                    writer.write("Name: " + name);
                    System.out.println("""
                                       BudgetBuddy comes with pre-made categories for budgeting your income. These categories are...
                                       \t1. Essentials (money dedicated towards needs)
                                       \t2. Savings (money you save for future expenses) 
                                       \t3. Free Spending (money you can spend on wants)
                                       """);
                    System.out.println("```````````````````````````````````````````````````````````````");
                    System.out.println("\nTo start, please go into the menu and select '1' to set your monthly income!");
                    writer.write("\n\nMonthly Income: 0\n\nBalance: 0\n\nBudget:\n\tEssentials: 0\n\tSavings: 0\n\tFree Spending: 0");
                    writer.close();
                }
            } catch (IOException e) {
                System.out.println("An error occurred when creating your file.");
            }
        }

// File Reading Portion:
        // Update variables using file reading
        try {
            Scanner fileReader = new Scanner(userData);
            while (fileReader.hasNextLine()) {
                String data = fileReader.nextLine();
                if (data.startsWith("Name:")) {
                    name = data.substring("Name:".length()).trim();
                } else if (data.startsWith("Monthly Income:")) {
                        String incomeString = data.substring("Monthly Income:".length()).trim().replace("$", "").replace("%", "");
                    try {
                        income = Double.parseDouble(incomeString);

                        // check pay day
                        if (income > 0) {
                            checkPayDay(userData, income);
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Error: Coudln't parse income from file.");
                    }
                }
            }
            fileReader.close();
        } catch (FileNotFoundException e) {
            System.out.print("An error occured when reading the file");
        }

// User Interface:
        System.out.println("Welcome back " + name + "!");

        // needed this only for when the program runs the first time.
        if (firstRun && scan.hasNextLine()) {
            scan.nextLine();
        }

        do {
            System.out.println("""
                            ---- BudgetBuddy ----\r
                            1. Set Income\r
                            2. Allocate Budget\r
                            3. Add Expense\r
                            4. View Summary\r
                            5. Save & Exit"""
            );
            
            System.out.println("Enter your choice (1-5): ");
            String userInput = scan.nextLine();
            try {
                userChoice = Integer.parseInt(userInput);
            } catch (NumberFormatException e) {
                System.out.println("Invalid selection. Please enter a number between 1 and 5.");
                userChoice = -1; // forces the loop to repeat
            }

            switch (userChoice) {
                case 1 -> setIncome(userData);
                case 2 -> allocateBudget(userData);
                case 3 -> addExpense(expenseFile, userData);
                case 4 -> viewSummary(userData, expenseFile);
                case 5 -> System.out.println("Have a good day!");
                default -> System.out.println("Invalid choice. Please try again.");
            }

        } while (userChoice != 5);
    }

// Various Methods within the menu:
    // sets user's income
    public static void setIncome(File userData) throws IOException {
        System.out.print("Enter your monthly income: $");
        double income = scan.nextDouble();
        scan.nextLine();
        UserDataUpdater.updateIncome(userData, "Monthly Income", income);
    }

    // sets the percentage of income that will go towards essentials, savings, and free spending
    public static void allocateBudget(File userData) throws IOException {
        System.out.println("\n--- Welcome to budget allocations! ---");
        int choice = 2;
        double essentials = 0, savings = 0, freeSpending = 0;
        do {
            boolean validInput = false;

            // get essentials percentage
            while (!validInput) {
                System.out.println("What percentage of your income will you dedicate towards your Essentials?");
                String input = scan.nextLine();
                try {
                    essentials = Double.parseDouble(input);
                    if (essentials < 0 || essentials > 100) {
                        System.out.println("Please enter a number between 0 and 100.");
                    } else {
                        validInput = true;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter a number.");
                }
            }
            validInput = false;
            
            // Get Savings percentage
            while (!validInput) {
                System.out.println("Enter percentage for Savings:");
                String input = scan.nextLine();
                try {
                    savings = Double.parseDouble(input);
                    if (savings < 0 || savings > 100) {
                        System.out.println("Please enter a number between 0 and 100.");
                    } else {
                        validInput = true;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter a number.");
                }
            }

            freeSpending = 100 - (essentials + savings);
            if (freeSpending < 0) {
                System.out.println("Your total exceeds 100%. Please re-enter your values");
                continue;
            }
            System.out.println("This will leave " + freeSpending + " % for free spending");
            validInput = false;

            while (!validInput) {
                System.out.println("Is this okay? (1. Yes / 2. No)");
                String input = scan.nextLine();
                try {
                    choice = Integer.parseInt(input);
                    if (choice == 1 || choice == 2) {
                        validInput = true;
                    } else {
                        System.out.println("Please enter 1 for Yes or 2 for No.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter a number.");
                }
            }
        } while (choice != 1);
        UserDataUpdater.updateBudget(userData, "Essentials", essentials);
        UserDataUpdater.updateBudget(userData, "Savings", savings);
        UserDataUpdater.updateBudget(userData, "Free Spending", freeSpending);
    }

    // user can enter an expense
    public static void addExpense(File expenseFile, File userData) throws IOException {

        // amount of the expense
        double amount = 0.0;
        while (true) {
            System.out.print("Enter expense amount: $");
            String input = scan.nextLine();
            try {
                amount = Double.parseDouble(input);
                if (amount <= 0) {
                    System.out.println("Amount must be greater than 0.");
                    return;
                } else {
                    break;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number");
            }
        }

        // category of the expense
        int choice = -1;
        String category = "";
        while (choice < 1 || choice > 3) {
            System.out.print("Enter category (1. Essentials, 2. Savings, 3. Free Spending): ");
            System.out.print("Enter your choice (1-3): ");
            String userInput = scan.nextLine();
            try {
                choice = Integer.parseInt(userInput);
                switch (choice) {
                    case 1 -> category = "Essentials";
                    case 2 -> category = "Savings";
                    case 3 -> category = "Free Spending";
                    default -> {
                        System.out.println("Invalid selection. Please enter a number between 1 and 3.");
                        choice = -1;
                    }
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid selection. Please enter a number between 1 and 3.");
            }
        }

        // description of the expense
        System.out.println("Enter a short description of the expense: ");
        String description = scan.nextLine();
        if (description.isEmpty()) {
            description = "(no description)";
        }
        
        // get today's date (used for the expense file)
        LocalDate today = LocalDate.now();
        String date = today.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));

        // open the file in append mode and format the text file to have a structured look for the expense file
        FileWriter writer = new FileWriter(expenseFile, true);
        writer.write(String.format("%-12s %-15s %-25s $%.2f%n", date, category, description, amount));
        writer.close();
        UserDataUpdater.updateBalance(userData, -amount);
        System.out.println("Expense has been logged.");
    }

    // view summary of everything
    public static void viewSummary(File userData, File expenseFile) throws IOException {
        
        // user data
        double income = 0;
        double essentialsPercent = 0;
        double savingsPercent = 0;
        double freeSpendingPercent = 0;

        Scanner scan = new Scanner(userData);
        while (scan.hasNextLine()) {
            String line = scan.nextLine().trim();
            if (line.startsWith("Monthly Income:")) {
                String val = line.substring("Monthly Income:".length()).trim();
                val = val.replace("$", "").replace("%", "");
                try {
                    income = Double.parseDouble(val);
                } catch (NumberFormatException e) {
                    System.out.println("Error: Couldn't parse income from -> " + val + ".");
                }
            } else if (line.startsWith("Essentials:")) {
                String val = line.substring("Essentials:".length()).trim();
                val = val.replace("$", "").replace("%", "");
                try {
                    essentialsPercent = Double.parseDouble(val);
                } catch (NumberFormatException e) {
                    System.out.println("Error: Couldn't parse essentials from -> " + val + ".");
                }
            } else if (line.startsWith("Savings:")) {
                String val = line.substring("Savings:".length()).trim();
                val = val.replace("$", "").replace("%", "");
                try {
                    savingsPercent = Double.parseDouble(val);
                } catch (NumberFormatException e) {
                    System.out.println("Error: Couldn't parse savings from -> " + val + ".");
                }
            } else if (line.startsWith("Free Spending:")) {
                String val = line.substring("Free Spending:".length()).trim();
                val = val.replace("$", "").replace("%", "");
                try {
                    freeSpendingPercent = Double.parseDouble(val);
                } catch (NumberFormatException e) {
                    System.out.println("Error: Couldn't parse free spending from -> " + val + ".");
                }
            }
        }
        scan.close();

        // check if income is 0
        if (income == 0) {
            System.out.println("Monthly income has not been set. Please set your income before viewing the summary.");
            return;
        }


        // expense data
        double essentialsSpent = 0;
        double savingsSpent = 0;
        double freeSpent = 0;

        List<String> expenseLog = new ArrayList<>();
        if (expenseFile.exists()) {
            Scanner expenseScan = new Scanner(expenseFile);
            while (expenseScan.hasNextLine()) {
                String line = expenseScan.nextLine();
                expenseLog.add(line);
                if (line.contains("Essentials")) {
                    essentialsSpent += getAmount(line);
                } else if (line.contains("Savings")) {
                    savingsSpent += getAmount(line);
                } else if (line.contains("Free Spending")) {
                    freeSpent += getAmount(line);
                }
            }
            expenseScan.close();
        }

        // check if expenseLog is empty
        if (expenseLog.isEmpty()) {
            System.out.println("No expenses were found for this month");
            return;
        }

        // print
        double essentialsBudget = income * (essentialsPercent / 100);
        double savingsBudget = income * (savingsPercent / 100);
        double freeBudget = income * (freeSpendingPercent / 100);

        System.out.println("\n_____ Budget Summary _____");
        printBudgetCategory("Essentials", essentialsSpent, essentialsBudget);
        printBudgetCategory("Savings", savingsSpent, savingsBudget);
        printBudgetCategory("Free Spending", freeSpent, freeBudget);
        System.out.println("\n_____ Expense Log _____");
        for (int i = 0; i < expenseLog.size(); i++) {
            System.out.println(expenseLog.get(i));
        }

        // Create a summary file for the user's records
        LocalDate today = LocalDate.now();
        File savedReceipts = new File("receipts");
        if (!savedReceipts.exists()) {
            savedReceipts.mkdir();  // create a directory for the summaries
        }
        String fileName = String.format("BudgetBuddy_Summary_%d-$02d.txt", today.getYear(), today.getMonthValue());
        File summaryFile = new File(savedReceipts, fileName);
        FileWriter writer = new FileWriter(summaryFile);

        double essentialsUsed = 0.0;
        double savingsUsed = 0.0;
        double freeUsed = 0.0;
        writer.write("--- Budget Summary ---\n");
        if (essentialsBudget > 0) {
            essentialsUsed = (essentialsSpent / essentialsBudget) * 100;
        }
        if (savingsBudget > 0) {
            savingsUsed = (savingsSpent / savingsBudget) * 100;
        }
        if (freeBudget > 0) {
            freeUsed = (freeSpent / freeBudget) * 100;
        }

        writer.write(String.format("Essentials Spent: $%.2f / $%.2f (%.1f%% used)%n", essentialsSpent, essentialsBudget, essentialsUsed));
        writer.write(String.format("Savings Spent: $%.2f / $%.2f (%.1f%% used)%n", savingsSpent, savingsBudget, savingsUsed));
        writer.write(String.format("Free Spending Spent: $%.2f / $%.2f (%.1f%% used)%n", freeSpent, freeBudget, freeUsed));
        writer.write("\n--- Expense Log ---\n");
        for (int i = 0; i < expenseLog.size(); i++) {
            writer.write(expenseLog.get(i) + "\n");
        }
    }

    // Helper Methods:
    public static void checkPayDay(File userData, double income) throws IOException {
        LocalDate today = LocalDate.now();
        LocalDate lastPayday = UserDataUpdater.readLastPayday(userData);

        if (lastPayday == null || today.getMonthValue() != lastPayday.getMonthValue()) {
            // every payday the balance will be updated
            System.out.println("Payday! Updating Balance now...");
            UserDataUpdater.updateBalance(userData, income);
            UserDataUpdater.updateLastPayday(userData, today);
        } else {
            System.out.println("No Payday at the moment.");
        }
    }

    public static double getAmount(String line) {
        int dollarIndex = line.lastIndexOf('$');
        if (dollarIndex != -1) {
            String amountLine = line.substring(dollarIndex + 1).trim();
            try {
                return Double.parseDouble(amountLine);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    public static void printBudgetCategory (String name, double spent, double budget) {
        double percentUsed = 0.0;
        if (budget > 0) {
            percentUsed = (spent/budget) * 100;
        }

        System.out.printf("%-15s Spent: $%.2f / $%.2f (%.1f%% used)%n", name, spent, budget, percentUsed);
    }
}
