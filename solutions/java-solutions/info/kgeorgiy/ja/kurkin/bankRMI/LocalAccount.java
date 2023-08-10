package info.kgeorgiy.ja.kurkin.bankRMI;

import java.io.Serializable;
import java.rmi.RemoteException;

public class LocalAccount implements Account, Serializable {

    private String accountId;
    private int amount;

    /**
     * Default constructor
     * @param accountId for person
     * @param amount for person
     */
    public LocalAccount(String accountId, int amount) {
        this.accountId = accountId;
        this.amount = amount;
    }

    /**
     * Returns account identifier.
     */
    @Override
    public String getId() throws RemoteException {
        return accountId;
    }

    /**
     * Returns amount of money in the account.
     */
    @Override
    public int getAmount() throws RemoteException {
        return amount;
    }

    /**
     * Sets amount of money in the account.
     *
     * @param amount by persons
     */
    @Override
    public void setAmount(int amount) throws RemoteException {
        this.amount = amount;
    }
}
