import java.io.*;
import java.util.*;
import javax.swing.DefaultListModel;

public class StorageManager {
    private static final String USER_FILE = "userData.txt";
    private static final String TRANSACTION_FILE = "transactions.txt";
    private static final String CATEGORY_FILE = "categories.txt";

    // Load income and balance
    public static double[] loadUser() throws IOException {
        double income = 0.0;
        double balance = 0.0;
        Map<String, Double> categories = new LinkedHashMap<>();
        File file = new File(USER_FILE);
        if (!file.exists()) {
            saveUser(0, 0, categories);
        }
        try (BufferedReader br = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("Income:")) income = Double.parseDouble(line.split(":")[1]);
                else if (line.startsWith("Balance:")) balance = Double.parseDouble(line.split(":")[1]);
            }
        }
        return new double[]{income, balance};
    }

    public static void saveUser(double income, double balance, Map<String, Double> categories) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(USER_FILE))) {
            bw.write("Income:" + income + "\n");
            bw.write("Balance:" + balance + "\n");
            for (String cat : categories.keySet()) {
                bw.write(cat + ":" + categories.get(cat) + "\n");
            }
        }
    }

    // Transactions
    public static List<Transaction> loadTransactions() throws IOException {
        List<Transaction> list = new ArrayList<>();
        File file = new File(TRANSACTION_FILE);
        if (!file.exists()) file.createNewFile();
        try (BufferedReader br = new BufferedReader(new FileReader(TRANSACTION_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                list.add(Transaction.fromCSV(line));
            }
        }
        return list;
    }

    public static void saveTransaction(Transaction tx) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(TRANSACTION_FILE, true))) {
            bw.write(tx.toCSV() + "\n");
        }
    }

    // Categories
    public static DefaultListModel<String> loadCategories() throws IOException {
        DefaultListModel<String> model = new DefaultListModel<>();
        File file = new File(CATEGORY_FILE);
        if (!file.exists()) file.createNewFile();
        try (BufferedReader br = new BufferedReader(new FileReader(CATEGORY_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                model.addElement(line);
            }
        }
        return model;
    }

    public static void saveCategories(DefaultListModel<String> model) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(CATEGORY_FILE))) {
            for (int i = 0; i < model.size(); i++) {
                bw.write(model.get(i) + "\n");
            }
        }
    }
}
