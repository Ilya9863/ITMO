package info.kgeorgiy.ja.kurkin.bankRMI;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * this class is responsible for the logic of forming bank customer accounts
 *
 * @author Kurkin Ilya
 * {@link UnicastRemoteObject}
 * {@link Bank}
 **/

public class RemoteBank extends UnicastRemoteObject implements Bank {
    private final int port;
    private final ConcurrentMap<String, Account> accounts = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Person> persons = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Set<String>> accountsForPerson = new ConcurrentHashMap<>();

    /**
     * Default constructor
     *
     * @param port for rmi connecting
     * @throws RemoteException if a data transfer error has occurred
     */
    public RemoteBank(final int port) throws RemoteException {
        super(port);
        this.port = port;
    }

    /**
     * this method creating account
     *
     * @param subId account id
     * @return new account created by subId
     * @throws RemoteException if a data transfer error has occurred
     */
    @Override
    public Account createAccount(final String subId) throws RemoteException {
        String[] params = subId.split(":");
        String passwordId = params[0];
        if (!persons.containsKey(passwordId)) return null;
        String accountId = params[1];
        System.out.println("Creating account " + accountId);
        final Account account = new RemoteAccount(accountId, port);
        if (accounts.putIfAbsent(subId, account) == null) {
            if (accountsForPerson.get(passwordId) == null) {
                accountsForPerson.put(passwordId, new ConcurrentSkipListSet<>());
            }
            accountsForPerson.get(passwordId).add(subId);
            return account;
        } else {
            return getAccount(subId);
        }
    }

    /**
     * @param id account id
     * @return account by id
     * @throws RemoteException if a data transfer error has occurred
     */
    @Override
    public Account getAccount(final String id) throws RemoteException {
        System.out.println("Retrieving account " + id);
        return accounts.get(id);
    }

    /**
     * @param id person id
     * @return all accounts by id
     * @throws RemoteException if a data transfer error has occurred
     */
    @Override
    public Set<String> getAccounts(final String id) throws RemoteException {
        return accountsForPerson.get(id);
    }

    /**
     * creating person with: name, surname, passportId
     *
     * @param name       for person
     * @param surname    for person
     * @param passportId for person
     * @throws RemoteException if a data transfer error has occurred
     */
    @Override
    public void createPerson(String name, String surname, String passportId) throws RemoteException {
        if (persons.containsKey(passportId)) {
            return;
        }

        Person person = new RemotePerson(port, name, surname, passportId);
        persons.put(passportId, person);
        accountsForPerson.put(passportId, new ConcurrentSkipListSet<>());
    }

    /**
     * @param passportId for person
     * @return remotePerson by passportId
     * @throws RemoteException if a data transfer error has occurred
     */
    @Override
    public Person getRemotePerson(String passportId) throws RemoteException {
        return persons.get(passportId);
    }

    /**
     * @param passportId for person
     * @return localPerson
     * @throws RemoteException if a data transfer error has occurred
     */
    @Override
    public LocalPerson getLocalPerson(String passportId) throws RemoteException {
        Person person = persons.get(passportId);
        Set<String> personAccounts = accountsForPerson.get(person.getPassportID());
        ConcurrentMap<String, LocalAccount> accountList = new ConcurrentHashMap<>();
        for (var it : personAccounts) {
            String[] params = it.split(":");
            String passwordSubId = params[1];
            accountList.put(it, new LocalAccount(passwordSubId, accounts.get(it).getAmount()));
        }
        return new LocalPerson(person.getName(), person.getSurname(), person.getPassportID(), accountList);
    }
}
