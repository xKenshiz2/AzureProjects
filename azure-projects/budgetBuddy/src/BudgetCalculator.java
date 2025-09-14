import java.time.format.DateTimeFormatter;
import java.util.List;

public class BudgetCalculator {

    
    public static void printSummary(double income, double balance, List<String> categories, double[] percentages, List<Transaction> transactions) {
        System.out.println("\n===== Budget Summary =====");

        for (int i = 0; i < categories.size(); i++) {
            String cat = categories.get(i);
            double pct = percentages[i];
            double spent = transactions.stream()
                    .filter(tx -> tx.getCategory().equals(cat))
                    .mapToDouble(Transaction::getAmount)
                    .sum();
            double budgetAmt = income * (pct / 100);
            double percentUsed = (budgetAmt == 0) ? 0 : (spent / budgetAmt) * 100;

            System.out.printf("%-15s Spent: $%.2f / $%.2f (%.1f%% used)\n", cat, spent, budgetAmt, percentUsed);
        }

        System.out.printf("\nTotal Income: $%.2f | Current Balance: $%.2f\n", income, balance);

        if (transactions.isEmpty()) {
            System.out.println("\nNo expenses logged yet.");
            return;
        }

        System.out.println("\n----- Expense Log -----");
        System.out.printf("%-20s | %-15s | %-25s | %-10s\n", "Date & Time", "Category", "Description", "Amount");
        System.out.println("----------------------------------------------------------------------");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
        for (Transaction tx : transactions) {
            System.out.printf("%-20s | %-15s | %-25s | $%-10.2f\n",
                    tx.getDate().format(formatter),
                    tx.getCategory(),
                    tx.getDescription(),
                    tx.getAmount());
        }
    }

}
