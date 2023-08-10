package info.kgeorgiy.ja.kurkin.bankRMI;

import java.rmi.Remote;
import java.rmi.RemoteException;

interface Person extends Remote {
    String getName() throws RemoteException;

    String getSurname() throws RemoteException;

    String getPassportID() throws RemoteException;
}
