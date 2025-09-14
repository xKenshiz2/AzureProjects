import java.util.*;

public class UserData {
    private double income;
    private Map<String, Double> budget; // category -> percent
    private List<Transaction> transactions;
    private List<String> categories;

    public UserData() {
        income = 0;
        budget = new LinkedHashMap<>();
        transactions = new ArrayList<>();
        categories = new ArrayList<>();
    }

    // Getters / setters
    public double getIncome() { return income; }
    public void setIncome(double income) { this.income = income; }

    public Map<String, Double> getBudget() { return budget; }
    public void setBudget(Map<String, Double> budget) { this.budget = budget; }

    public List<Transaction> getTransactions() { return transactions; }
    public void setTransactions(List<Transaction> transactions) { this.transactions = transactions; }

    public List<String> getCategories() { return categories; }
    public void setCategories(List<String> categories) { this.categories = categories; }
}
