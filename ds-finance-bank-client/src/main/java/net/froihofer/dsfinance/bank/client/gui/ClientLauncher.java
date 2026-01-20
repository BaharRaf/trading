package net.froihofer.dsfinance.bank.client.gui;

import net.froihofer.dsfinance.bank.client.BankClient;

import javax.swing.*;

public final class ClientLauncher {

    private ClientLauncher() {}

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        String[] options = {
                "Employee GUI",
                "Customer GUI",
                "Part 2 CLI (BankClient)",
                "Exit"
        };

        int choice = JOptionPane.showOptionDialog(
                null,
                "Which client do you want to start?",
                "DS Finance Bank - Client Launcher",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 0) {
            EmployeeClientGUI.main(new String[0]);
        } else if (choice == 1) {
            CustomerClientGUI.main(new String[0]);
        } else if (choice == 2) {
            BankClient.main(new String[0]);
        } else {
            System.exit(0);
        }
    }
}
