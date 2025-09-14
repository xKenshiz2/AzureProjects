import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class BudgetBuddyGUI extends JFrame {

    // Data
    private Map<String, Double> categories = new LinkedHashMap<>();
    private List<Transaction> transactions = new ArrayList<>();
    private StorageManager storageManager = new StorageManager();
    private double income = 0.0;

    // UI components (instance-level)
    private DefaultListModel<String> categoryListModel = new DefaultListModel<>();
    private JList<String> categoryList = new JList<>(categoryListModel);
    private JLabel remainingLabel = new JLabel("Remaining: 100%");
    private JComboBox<String> categoryCombo = new JComboBox<>();
    private DefaultTableModel expenseTableModel;
    private JTable expenseTable;
    private JTextField incomeField;

    private static final DateTimeFormatter DISPLAY_DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public BudgetBuddyGUI() {
        setTitle("BudgetBuddy");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 650);
        setLocationRelativeTo(null);

        initComponents();
        loadData();
    }

    private void initComponents() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Categories", buildCategoriesPanel());
        tabs.addTab("Expenses", buildExpensesPanel());
        tabs.addTab("Summary", buildSummaryPanel());
        add(tabs, BorderLayout.CENTER);
    }

    private JPanel buildCategoriesPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));

        // top: income controls
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Monthly Income: "));
        incomeField = new JTextField(String.valueOf(income), 10);
        top.add(incomeField);
        JButton saveIncomeBtn = new JButton("Save Income");
        saveIncomeBtn.addActionListener(e -> {
            try {
                income = Double.parseDouble(incomeField.getText().trim());
                storageManager.saveIncome(income);
                JOptionPane.showMessageDialog(this, "Income saved.");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid income value.");
            }
        });
        top.add(saveIncomeBtn);
        panel.add(top, BorderLayout.NORTH);

        // center: category list
        JScrollPane scroll = new JScrollPane(categoryList);
        scroll.setPreferredSize(new Dimension(420, 300));
        panel.add(scroll, BorderLayout.CENTER);

        // right: controls
        JPanel controls = new JPanel(new GridLayout(0, 1, 6, 6));
        JTextField catName = new JTextField();
        JTextField catPct = new JTextField();
        JButton addBtn = new JButton("Add (with %)");
        JButton removeBtn = new JButton("Remove selected");
        JButton allocateBtn = new JButton("Allocate (edit percentages)");
        controls.add(new JLabel("Name:"));
        controls.add(catName);
        controls.add(new JLabel("Pct (0-100):"));
        controls.add(catPct);
        controls.add(addBtn);
        controls.add(removeBtn);
        controls.add(allocateBtn);
        controls.add(remainingLabel);

        // add behavior
        addBtn.addActionListener(e -> {
            String name = catName.getText().trim();
            if (name.isEmpty()) { JOptionPane.showMessageDialog(this, "Enter name"); return; }
            double entered = 0.0;
            try {
                entered = Double.parseDouble(catPct.getText().trim());
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Enter a number for percent"); return; }
            double total = categories.values().stream().mapToDouble(Double::doubleValue).sum();
            if (total + entered > 100.0) {
                JOptionPane.showMessageDialog(this, "Would exceed 100% — set to remaining instead.");
                entered = Math.max(0.0, 100.0 - total);
            }
            categories.put(name, entered);
            updateCategoryDisplays();
            saveData();
            catName.setText(""); catPct.setText("");
        });

        removeBtn.addActionListener(e -> {
            int idx = categoryList.getSelectedIndex();
            if (idx >= 0) {
                // parse name from "Name - X%"
                String line = categoryListModel.get(idx);
                String name = line.split(" - ")[0];
                categories.remove(name);
                updateCategoryDisplays();
                saveData();
            } else {
                JOptionPane.showMessageDialog(this, "Select a category first.");
            }
        });

        allocateBtn.addActionListener(e -> {
            // ask for percentages in order, last gets remaining
            List<String> keys = new ArrayList<>(categories.keySet());
            double total = 0.0;
            for (int i = 0; i < keys.size(); i++) {
                String key = keys.get(i);
                if (i == keys.size() - 1) {
                    double remaining = Math.max(0.0, 100.0 - total);
                    categories.put(key, remaining);
                } else {
                    String input = JOptionPane.showInputDialog(this, "Enter % for " + key + " (Remaining: " + (100 - total) + "%):", categories.getOrDefault(key, 0.0));
                    if (input == null) return; // cancel
                    double val;
                    try {
                        val = Double.parseDouble(input);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Invalid number; allocation cancelled.");
                        return;
                    }
                    if (val + total > 100.0) val = 100.0 - total;
                    categories.put(key, val);
                    total += val;
                }
            }
            updateCategoryDisplays();
            saveData();
        });

        panel.add(controls, BorderLayout.EAST);
        return panel;
    }

    private JPanel buildExpensesPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));

        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.add(new JLabel("Category:"));
        form.add(categoryCombo);
        form.add(new JLabel("Description:"));
        JTextField descField = new JTextField();
        form.add(descField);
        form.add(new JLabel("Amount:"));
        JTextField amountField = new JTextField();
        form.add(amountField);
        JButton addExpense = new JButton("Add Expense");
        form.add(new JLabel());
        form.add(addExpense);

        String[] cols = {"Date/Time", "Category", "Description", "Amount"};
        expenseTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        expenseTable = new JTable(expenseTableModel);
        JScrollPane tableScroll = new JScrollPane(expenseTable);
        tableScroll.setPreferredSize(new Dimension(800, 350));

        addExpense.addActionListener(e -> {
            if (categoryCombo.getItemCount() == 0) {
                JOptionPane.showMessageDialog(this, "Add categories first.");
                return;
            }
            String cat = (String) categoryCombo.getSelectedItem();
            String desc = descField.getText().trim();
            double amt;
            try {
                amt = Double.parseDouble(amountField.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Enter a valid amount.");
                return;
            }
            LocalDateTime now = LocalDateTime.now();
            Transaction tx = new Transaction(now, cat, desc, amt);
            transactions.add(tx);
            expenseTableModel.addRow(new Object[] { now.format(DISPLAY_DT), cat, desc, String.format("%.2f", amt) });
            saveData();
            descField.setText("");
            amountField.setText("");
        });

        panel.add(form, BorderLayout.NORTH);
        panel.add(tableScroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildSummaryPanel() {
        JPanel panel = new JPanel(new BorderLayout(8,8));
        JTextArea ta = new JTextArea();
        ta.setEditable(false);
        JScrollPane scroll = new JScrollPane(ta);
        scroll.setPreferredSize(new Dimension(800, 450));
        JButton refresh = new JButton("Refresh Summary");
        refresh.addActionListener(e -> {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Income: $%.2f%n%n", income));
            double totalSpent = transactions.stream().mapToDouble(Transaction::getAmount).sum();
            sb.append("Category breakdown:\n");
            for (Map.Entry<String, Double> en : categories.entrySet()) {
                String name = en.getKey();
                double pct = en.getValue();
                double allocatedAmt = income * pct / 100.0;
                double spent = transactions.stream().filter(t -> t.getCategory().equals(name)).mapToDouble(Transaction::getAmount).sum();
                sb.append(String.format("%s — %4.1f%% ($%.2f allocated) — Spent: $%.2f", name, pct, allocatedAmt, spent));
                if (spent > allocatedAmt) sb.append("  <-- Over budget!");
                sb.append("\n");
            }
            sb.append(String.format("%nTotal spent: $%.2f%n", totalSpent));
            ta.setText(sb.toString());
        });
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(refresh, BorderLayout.SOUTH);
        return panel;
    }

    // Update UI displays after categories map changes
    private void updateCategoryDisplays() {
        categoryListModel.clear();
        categoryCombo.removeAllItems();
        double total = 0.0;
        for (Map.Entry<String, Double> e : categories.entrySet()) {
            categoryListModel.addElement(e.getKey() + " - " + String.format("%.1f", e.getValue()) + "%");
            categoryCombo.addItem(e.getKey());
            total += e.getValue();
        }
        double remaining = Math.max(0.0, 100.0 - total);
        remainingLabel.setText("Remaining: " + String.format("%.1f", remaining) + "%");
    }

    // Save everything
    private void saveData() {
        storageManager.saveCategories(categories);
        storageManager.saveTransactions(transactions);
        storageManager.saveIncome(income);
    }

    // Load everything into memory and UI
    private void loadData() {
        categories = storageManager.loadCategories();
        transactions = storageManager.loadTransactions();
        income = storageManager.loadIncome();

        // update UI fields
        incomeField.setText(String.valueOf(income));
        updateCategoryDisplays();

        // populate table
        expenseTableModel.setRowCount(0);
        for (Transaction t : transactions) {
            expenseTableModel.addRow(new Object[] {
                t.getDateTime().format(DISPLAY_DT),
                t.getCategory(),
                t.getDescription(),
                String.format("%.2f", t.getAmount())
            });
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BudgetBuddyGUI g = new BudgetBuddyGUI();
            g.setVisible(true);
        });
    }
}
