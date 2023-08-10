package info.kgeorgiy.ja.kurkin.bankRMI;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

public interface Bank extends Remote {
    /**
     * Creates a new account with specified identifier if it does not already exist.
     *
     * @param id account id
     * @return created or existing account.
     */
    Account createAccount(String id) throws RemoteException;

    /**
     * Returns account by identifier.
     *
     * @param id account id
     * @return account with specified identifier or {@code null} if such account does not exist.
     */
    Account getAccount(String id) throws RemoteException;

    /**
     * @param id for person
     * @return all accounts for persons
     * @throws RemoteException if a data transfer error has occurred
     */
    Set<String> getAccounts(String id) throws RemoteException;

    void createPerson(String name, String surname, String passportId) throws RemoteException;

    Person getRemotePerson(String passportId) throws RemoteException;

    LocalPerson getLocalPerson(String passportId) throws RemoteException;
}
