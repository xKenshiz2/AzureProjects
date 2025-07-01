
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class UserDataUpdater {

    // updates income
    public static void updateIncome(File f, String key, double inc) {
        try {
            // Read the file line-by-line and store the updated values
            List<String> updatedLines = new ArrayList<>();
            Scanner scan = new Scanner(f);

            while (scan.hasNextLine()) {
                String line = scan.nextLine();
                if (line.startsWith(key + ":")) {
                    // this replaces old value with new value
                    updatedLines.add(key + ": $" + inc);
                } else {
                    updatedLines.add(line);
                }
            }
            scan.close();

            // Now, write the updated lines back to the file
            FileWriter writer = new FileWriter(f);
            for (String updatedLine : updatedLines) {
                writer.write(updatedLine + System.lineSeparator());
            }
            writer.close();

        } catch (IOException e) {
            System.out.println("Error updating file: " + e.getMessage());
        }
    }

    // updates budget
    public static void updateBudget(File f, String key, double val) {
        try {
            // Read the file line-by-line and store the updated values
            List<String> updatedLines = new ArrayList<>();
            Scanner scan = new Scanner(f);

            while (scan.hasNextLine()) {
                String line = scan.nextLine();
                if (line.strip().startsWith(key + ":")) {
                    // this replaces old value with new value
                    updatedLines.add("\t" + key + ": " + val + "%");
                } else {
                    updatedLines.add(line);
                }
            }
            scan.close();

            // Now, write the updated lines back to the file
            FileWriter writer = new FileWriter(f);
            for (String updatedLine : updatedLines) {
                writer.write(updatedLine + System.lineSeparator());
            }
            writer.close();

        } catch (Exception e) {
            System.out.println("Error updating file: " + e.getMessage());
        }
    }

    // updates balance
    public static void updateBalance(File f, double amount) {
        try {
            List<String> updatedLines = new ArrayList<>();
            Scanner scan = new Scanner(f);
            double currentBalance = 0.0;

            while (scan.hasNextLine()) {
                String line = scan.nextLine();
                if (line.startsWith("Balance:")) {
                    String balanceString = line.substring("Balance:".length()).trim();
                    currentBalance = Double.parseDouble(balanceString);
                    currentBalance += amount; // add or subtract the amount
                    updatedLines.add("Balance:" + currentBalance);
                } else {
                    updatedLines.add(line);
                }
            }
            scan.close();

            FileWriter writer = new FileWriter(f);
            for (String updatedLine : updatedLines) {
                writer.write(updatedLine + System.lineSeparator());
            }
            writer.close();
        } catch (IOException e) {
            System.out.println("Error updating balance");
        }
    }

    // read the last payday
    public static LocalDate readLastPayday(File f) {
        try {
            Scanner scan = new Scanner(f);
            DateTimeFormatter format = DateTimeFormatter.ofPattern("MM/dd/yyyy");

            while (scan.hasNextLine()) {
                String line = scan.nextLine();
                if (line.startsWith("Last Payday:")) {
                    String dateString = line.substring("Last Payday:".length()).trim();
                    return LocalDate.parse(dateString, format);
                }
            }
            scan.close();
        } catch (Exception e) {
            System.out.println("Could not read last payday.");
        }
        return null;
    }

    // update last payday date in the file
    public static void updateLastPayday(File f, LocalDate today) {
        try {
            List<String> updatedLines = new ArrayList<>();
            Scanner scan = new Scanner(f);
            DateTimeFormatter format = DateTimeFormatter.ofPattern("MM/dd/yyyy");

            while (scan.hasNextLine()) {
                String line = scan.nextLine();
                if (line.startsWith("Last Payday:")) {
                    updatedLines.add("Last Payday: " + today.format(format));
                } else {
                    updatedLines.add(line);
                }
            }
            scan.close();

            FileWriter writer = new FileWriter(f);
            for (String updatedLine : updatedLines) {
                writer.write(updatedLine + System.lineSeparator());
            }
            writer.close();
        } catch (IOException e) {
            System.out.println("Could not update last payday.");
        }
    }
}