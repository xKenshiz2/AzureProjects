import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction {
    private LocalDateTime dateTime;
    private String category;
    private String description;
    private double amount;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public Transaction(LocalDateTime dateTime, String category, String description, double amount) {
        this.dateTime = dateTime;
        this.category = category;
        this.description = description;
        this.amount = amount;
    }

    public LocalDateTime getDateTime() { return dateTime; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public double getAmount() { return amount; }

    private static String sanitize(String s) {
        if (s == null) return "";
        return s.replace(",", ";"); // avoid CSV split issues
    }

    public String toCSV() {
        return dateTime.format(FORMATTER) + "," + sanitize(category) + "," + sanitize(description) + "," + amount;
    }

    public static Transaction fromCSV(String line) {
        String[] parts = line.split(",", 4);
        LocalDateTime dt = LocalDateTime.parse(parts[0], FORMATTER);
        String cat = parts[1];
        String desc = parts[2];
        double amt = Double.parseDouble(parts[3]);
        return new Transaction(dt, cat, desc, amt);
    }
}
