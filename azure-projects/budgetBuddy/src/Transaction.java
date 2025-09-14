import java.time.LocalDate;

public class Transaction {
    private LocalDate date;
    private String category;
    private String description;
    private double amount;

    public Transaction(LocalDate date, String category, String description, double amount) {
        this.date = date;
        this.category = category;
        this.description = description;
        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }

    public String toCSV() {
        return date + "," + category + "," + description + "," + amount;
    }

    public static Transaction fromCSV(String line) {
        String[] parts = line.split(",", 4);
        LocalDate date = LocalDate.parse(parts[0]);
        String category = parts[1];
        String description = parts[2];
        double amount = Double.parseDouble(parts[3]);
        return new Transaction(date, category, description, amount);
    }
}
