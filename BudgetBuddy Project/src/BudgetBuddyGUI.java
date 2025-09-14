import java.awt.*;
import java.time.LocalDate;
import java.util.*;
import javax.swing.*;

public class BudgetBuddyGUI extends JFrame {

    private JTextField incomeField;
    private DefaultListModel<String> categoryListModel;
    private JList<String> categoryList;
    private Map<String, Double> categoryPercentages;
    private java.util.List<Transaction> transactions;
    private double income;
    private double balance;

    public BudgetBuddyGUI() {
        setTitle("BudgetBuddy");
        setSize(700, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        categoryPercentages = new LinkedHashMap<>();
        transactions = new ArrayList<>();
        categoryListModel = new DefaultListModel<>();

        loadUserData();
        loadCategories();
        loadTransactions();
        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel topPanel = new JPanel(new FlowLayout());
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        JPanel bottomPanel = new JPanel(new FlowLayout());

        // Income
        topPanel.add(new JLabel("Monthly Income:"));
        incomeField = new JTextField(String.valueOf(income), 10);
        topPanel.add(incomeField);
        JButton saveIncomeBtn = new JButton("Save Income");
        saveIncomeBtn.addActionListener(e -> saveIncome());
        topPanel.add(saveIncomeBtn);

        // Category List
        categoryList = new JList<>(categoryListModel);
        JScrollPane categoryScroll = new JScrollPane(categoryList);
        centerPanel.add(categoryScroll);

        JPanel categoryButtons = new JPanel(new GridLayout(0, 1, 5, 5));
        JButton addCategoryBtn = new JButton("Add Category");
        addCategoryBtn.addActionListener(e -> addCategory());
        JButton removeCategoryBtn = new JButton("Remove Category");
        removeCategoryBtn.addActionListener(e -> removeCategory());
        categoryButtons.add(addCategoryBtn);
        categoryButtons.add(removeCategoryBtn);
        centerPanel.add(categoryButtons);

        // Bottom buttons
        JButton allocateBtn = new JButton("Allocate Budget");
        allocateBtn.addActionListener(e -> allocateBudget());
        JButton addExpenseBtn = new JButton("Add Expense");
        addExpenseBtn.addActionListener(e -> addExpense());
        JButton summaryBtn = new JButton("View Summary");
        summaryBtn.addActionListener(e -> openSummaryDialog());
        bottomPanel.add(allocateBtn);
        bottomPanel.add(addExpenseBtn);
        bottomPanel.add(summaryBtn);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void loadUserData() {
        try {
            double[] data = StorageManager.loadUser();
            income = data[0];
            balance = data[1];
        } catch (Exception e) {
            income = 0;
            balance = 0;
        }
    }

    private void loadCategories() {
        try {
            categoryListModel = StorageManager.loadCategories();
            for (int i = 0; i < categoryListModel.size(); i++) {
                categoryPercentages.put(categoryListModel.get(i), 0.0);
            }
        } catch (Exception e) {
            categoryListModel = new DefaultListModel<>();
        }
    }

    private void loadTransactions() {
        try {
            transactions = StorageManager.loadTransactions();
        } catch (Exception e) {
            transactions = new ArrayList<>();
        }
    }

    private void saveIncome() {
        try {
            income = Double.parseDouble(incomeField.getText());
            balance = income - transactions.stream().mapToDouble(Transaction::getAmount).sum();
            StorageManager.saveUser(income, balance, categoryPercentages);
            JOptionPane.showMessageDialog(this, "Income saved!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid input!");
        }
    }

    private void addCategory() {
        String name = JOptionPane.showInputDialog(this, "Enter category name:");
        if (name != null && !name.trim().isEmpty()) {
            categoryListModel.addElement(name);
            categoryPercentages.put(name, 0.0);
            try {
                StorageManager.saveCategories(categoryListModel);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Failed to save categories!");
            }
        }
    }

    private void removeCategory() {
        int idx = categoryList.getSelectedIndex();
        if (idx >= 0) {
            String cat = categoryListModel.get(idx);
            categoryListModel.remove(idx);
            categoryPercentages.remove(cat);
            try {
                StorageManager.saveCategories(categoryListModel);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Failed to save categories!");
            }
        }
    }

    private void allocateBudget() {
        double total = 0.0;
        for (int i = 0; i < categoryListModel.size(); i++) {
            String cat = categoryListModel.get(i);
            if (i == categoryListModel.size() - 1) {
                categoryPercentages.put(cat, 100.0 - total);
            } else {
                String input = JOptionPane.showInputDialog(this, "Enter % for " + cat + " (Remaining: " + (100 - total) + "%):");
                double val = Double.parseDouble(input);
                if (val + total > 100) val = 100 - total;
                categoryPercentages.put(cat, val);
                total += val;
            }
        }
        try {
            StorageManager.saveUser(income, balance, categoryPercentages);
            JOptionPane.showMessageDialog(this, "Budget Allocated!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to save budget!");
        }
    }

    private void addExpense() {
        if (categoryListModel.size() == 0) {
            JOptionPane.showMessageDialog(this, "Add categories first!");
            return;
        }
        String[] cats = new String[categoryListModel.size()];
        for (int i = 0; i < cats.length; i++) cats[i] = categoryListModel.get(i);

        String category = (String) JOptionPane.showInputDialog(this, "Select category:", "Category", JOptionPane.QUESTION_MESSAGE, null, cats, cats[0]);
        if (category == null) return;

        String desc = JOptionPane.showInputDialog(this, "Enter description:");
        if (desc == null) return;

        String amtStr = JOptionPane.showInputDialog(this, "Enter amount:");
        if (amtStr == null) return;

        try {
            double amt = Double.parseDouble(amtStr);
            LocalDate today = LocalDate.now();
            Transaction tx = new Transaction(today, category, desc, amt);
            transactions.add(tx);
            balance -= amt;
            StorageManager.saveTransaction(tx);
            StorageManager.saveUser(income, balance, categoryPercentages);
            JOptionPane.showMessageDialog(this, "Expense added!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid amount!");
        }
    }

    private void openSummaryDialog() {
        StringBuilder sb = new StringBuilder();
        sb.append("Income: $").append(income).append("\n");
        sb.append("Balance: $").append(balance).append("\n\n");

        sb.append("Budget:\n");
        for (String cat : categoryPercentages.keySet()) {
            sb.append(cat).append(": ").append(categoryPercentages.get(cat)).append("%\n");
        }

        sb.append("\nExpenses:\n");
        for (Transaction tx : transactions) {
            sb.append(tx.getDate()).append(" - ").append(tx.getCategory()).append(" - ").append(tx.getDescription()).append(" - $").append(tx.getAmount()).append("\n");
        }

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(600, 400));
        JOptionPane.showMessageDialog(this, scrollPane, "Summary", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BudgetBuddyGUI().setVisible(true));
    }
}
