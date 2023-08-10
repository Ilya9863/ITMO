package info.kgeorgiy.ja.kurkin.bankRMI;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public final class Client {
    /**
     * Utility class.
     */
    private Client() {
    }

    public static void main(final String... args) throws RemoteException {
        final Bank bank;
        try {
            bank = (Bank) Naming.lookup("//localhost:8888/bank");
        } catch (final NotBoundException e) {
            System.out.println("Bank is not bound");
            return;
        } catch (final MalformedURLException e) {
            System.out.println("Bank URL is invalid");
            return;
        }

        final String accountId = args.length >= 1 ? args[0] : "geo";

        String name = "huy";
        String surname = "huy";
        String passportID = "huy";
        String subId = "huy:89789"; // passportId:accountId
        int amount = Integer.parseInt("59");

        bank.createPerson(name, surname, passportID);
        Account account = bank.getAccount(accountId);
        if (account == null) {
            System.out.println("Creating account");
            account = bank.createAccount(subId);
        } else {
            System.out.println("Account already exists");
        }
        System.out.println("Account id: " + account.getId());
        System.out.println("Money: " + account.getAmount());
        System.out.println("Adding money");
        account.setAmount(account.getAmount() + 100);
        System.out.println("Money: " + account.getAmount());
        Person localPerson = bank.getLocalPerson(passportID);
        System.out.println();
        System.out.println(((LocalPerson) localPerson).getAccounts());
    }
}
