import javax.swing.*;
import java.awt.*;
import java.sql.*;

class User {
    private String username;
    private String password;
    private BankAccount bankAccount;

    public User(String username, String password, double initialBalance) {
        this.username = username;
        this.password = password;
        this.bankAccount = new BankAccount(1, initialBalance);
    }

    public boolean authenticate(String enteredPassword) {
        return password.equals(enteredPassword);
    }

    public BankAccount getBankAccount() {
        return bankAccount;
    }
}

class BankAccount {
    private double balance;
    private int accountId;

    public BankAccount(int accountId, double initialBalance) {
        this.accountId = accountId;
        this.balance = initialBalance;
    }

    public double getBalance() {
        return balance;
    }

    public void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
            updateBalanceInDatabase();
            System.out.println("Deposit successful. Current balance: $" + balance);
        } else {
            System.out.println("Invalid deposit amount.");
        }
    }

    public void withdraw(double amount) {
        if (amount > 0 && amount <= balance) {
            balance -= amount;
            updateBalanceInDatabase();
            System.out.println("Withdrawal successful. Current balance: $" + balance);
        } else {
            System.out.println("Invalid withdrawal amount or insufficient funds.");
        }
    }

    private void updateBalanceInDatabase() {
        try {
            Connection connection = DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
            String updateQuery = "UPDATE accounts SET balance = ? WHERE account_id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
                preparedStatement.setDouble(1, balance);
                preparedStatement.setInt(2, accountId);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

class CustomOptionPane {
    private static String storedPassword;

    public static String showLoginDialog() {
        if (storedPassword == null) {
            JPanel panel = new JPanel();
            JLabel label = new JLabel("Enter your password:");
            JPasswordField passwordField = new JPasswordField(10);
            panel.add(label);
            panel.add(passwordField);

            int result = JOptionPane.showConfirmDialog(null, panel, "Login", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                storedPassword = new String(passwordField.getPassword());
            } else {
                return null;
            }
        }

        return storedPassword;
    }
}

class ATM {
    private User user;

    public ATM(User user) {
        this.user = user;
    }

    public void displayMenu() {
        String[] options = {"1. Check Balance", "2. Deposit", "3. Withdraw", "4. Exit"};

        while (true) {
            int choice = showOptionDialog("ATM Menu", "                                                Welcome Mr. " + user.authenticate(CustomOptionPane.showLoginDialog()), options);

            if (choice == -1 || choice == 3) {
                break;
            }

            switch (choice) {
                case 0:
                    checkBalance();
                    break;
                case 1:
                    deposit();
                    break;
                case 2:
                    withdraw();
                    break;
                default:
                    JOptionPane.showMessageDialog(null, "Option invalide. Veuillez choisir à nouveau.");
            }
        }
    }

    private void checkBalance() {
        JOptionPane.showMessageDialog(null, "Solde actuel : $" + user.getBankAccount().getBalance());
    }

    private void deposit() {
        String amountStr = JOptionPane.showInputDialog("Entrez le montant du dépôt :");
        try {
            double amount = Double.parseDouble(amountStr);
            user.getBankAccount().deposit(amount);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Saisie invalide. Veuillez entrer un montant valide.");
        }
    }

    private void withdraw() {
    String amountStr = JOptionPane.showInputDialog("Entrez le montant du retrait :");
    try {
        double amount = Double.parseDouble(amountStr);
        double currentBalance = user.getBankAccount().getBalance();

        if (amount > 0 && amount <= currentBalance) {
            user.getBankAccount().withdraw(amount);
            JOptionPane.showMessageDialog(null, "Please collect your Money.");
        } else {
            JOptionPane.showMessageDialog(null, "Montant de retrait invalide. Veuillez entrer un montant valide.");
        }
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(null, "Saisie invalide. Veuillez entrer un montant valide.");
    }
}


    private int showOptionDialog(String title, String message, String[] options) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) screenSize.getWidth();
        int height = (int) screenSize.getHeight();

        return JOptionPane.showOptionDialog(null, message, title, JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
    }
    public static void main(String[] args) {
        // Initialize the H2 database
        try {
            Connection connection = DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
            String createTableQuery = "CREATE TABLE IF NOT EXISTS accounts (account_id INT PRIMARY KEY, balance DOUBLE)";
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(createTableQuery);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Create a User with a username, password, and an initial balance
        User user = new User("username", "password", 2000.0);

        // Authenticate the user
        String enteredPassword = CustomOptionPane.showLoginDialog();
        if (enteredPassword != null && user.authenticate(enteredPassword)) {
            // Create an ATM with the User
            ATM atm = new ATM(user);

            // Display the ATM menu
            atm.displayMenu();
        } else {
            JOptionPane.showMessageDialog(null, "Authentication failed. Incorrect password.");
        }
    }
}


