import java.io.*;
import java.util.*;

public class StorageManager {
    private static final String CATEGORY_FILE = "categories.txt";
    private static final String TRANSACTION_FILE = "transactions.txt";
    private static final String INCOME_FILE = "income.txt";

    // Load categories as a LinkedHashMap (preserves insertion order)
    public Map<String, Double> loadCategories() {
        Map<String, Double> map = new LinkedHashMap<>();
        File f = new File(CATEGORY_FILE);
        if (!f.exists()) return map;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] parts = line.split(",", 2);
                if (parts.length == 2) {
                    try {
                        double pct = Double.parseDouble(parts[1]);
                        map.put(parts[0], pct);
                    } catch (NumberFormatException ignored) { }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    public void saveCategories(Map<String, Double> categories) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(CATEGORY_FILE))) {
            for (Map.Entry<String, Double> e : categories.entrySet()) {
                bw.write(e.getKey() + "," + e.getValue());
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Transactions (overwrite file with full list)
    public List<Transaction> loadTransactions() {
        List<Transaction> list = new ArrayList<>();
        File f = new File(TRANSACTION_FILE);
        if (!f.exists()) return list;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                try {
                    list.add(Transaction.fromCSV(line));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void saveTransactions(List<Transaction> transactions) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(TRANSACTION_FILE))) {
            for (Transaction t : transactions) {
                bw.write(t.toCSV());
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Append single transaction (optional convenience)
    public void appendTransaction(Transaction tx) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(TRANSACTION_FILE, true))) {
            bw.write(tx.toCSV());
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Income persistence (simple single-number file)
    public double loadIncome() {
        File f = new File(INCOME_FILE);
        if (!f.exists()) return 0.0;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line = br.readLine();
            if (line != null && !line.isBlank()) {
                return Double.parseDouble(line.trim());
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public void saveIncome(double income) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(INCOME_FILE))) {
            bw.write(Double.toString(income));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
