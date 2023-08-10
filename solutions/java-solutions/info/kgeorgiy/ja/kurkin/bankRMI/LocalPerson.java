package info.kgeorgiy.ja.kurkin.bankRMI;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class LocalPerson implements Person, Serializable {

    private final String name;
    private final String surname;
    private final String passportId;
    private final ConcurrentMap<String, LocalAccount> accounts;

    /**
     * Default constructor
     *
     * @param name       by person
     * @param surname    by person
     * @param passportId by person
     * @param accounts   by person
     */
    public LocalPerson(String name, String surname, String passportId, ConcurrentMap<String, LocalAccount> accounts) {
        this.name = name;
        this.surname = surname;
        this.passportId = passportId;
        this.accounts = accounts;
    }

    /**
     * @return name by person
     * @throws RemoteException if a data transfer error has occurred
     */
    @Override
    public String getName() throws RemoteException {
        return name;
    }

    /**
     * @return surname by person
     * @throws RemoteException if a data transfer error has occurred
     */
    @Override
    public String getSurname() throws RemoteException {
        return surname;
    }

    /**
     * @return passport id by person
     * @throws RemoteException if a data transfer error has occurred
     */
    @Override
    public String getPassportID() throws RemoteException {
        return passportId;
    }

    /**
     * @return accounts
     */

    public Set<LocalAccount> getAccounts() {
        return new HashSet<>(accounts.values());
    }
}
