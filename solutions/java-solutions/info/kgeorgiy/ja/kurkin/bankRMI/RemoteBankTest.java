//package info.kgeorgiy.ja.kurkin.bankRMI;
//
//import jdk.jshell.execution.LoaderDelegate;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.BeforeClass;
//import org.junit.FixMethodOrder;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
//import org.junit.runners.MethodSorters;
//
//import java.net.MalformedURLException;
//import java.rmi.Naming;
//import java.rmi.RemoteException;
//import java.rmi.registry.LocateRegistry;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.TimeUnit;
//
//import static org.testng.CommandLineArgs.THREAD_COUNT;
//
//class RemoteBankTest {
//
//    private static final int PORT = 8889;
//    private static Bank bank;
//    private static LocalPerson localPerson;
//
//    @BeforeClass
//    public static void beforeClass() throws RemoteException, MalformedURLException {
//        bank = new RemoteBank(PORT);
//        LocateRegistry.createRegistry(PORT);
//        Naming.rebind("//localhost:" + PORT + "/bank", bank);
//    }
//
//    @Test
//    void creatingAccount() throws RemoteException {
//        //bank = new RemoteBank(PORT);
//        String subId = "pass:2900";
//        Account account = bank.createAccount(subId);
//        Account returnAccount = bank.getAccount(subId);
//        Assertions.assertNotEquals(null, account);
//        Assertions.assertNotEquals(null, returnAccount);
//    }
//
//    public static void hhh() {
//
//    }
//
//
//
////    @Test
////    void creatingLocalAndRemotePerson() throws RemoteException {
////        bank = new RemoteBank(PORT);
////        String name = "Person";
////        String surname = "Personvich";
////        String passportID = "8900";
////        bank.createPerson(name, surname, passportID);
////        Person remotePerson = bank.getRemotePerson(passportID);
////        LocalPerson localPerson = bank.getLocalPerson(passportID);
////
////        String subID = "4";
////        String ID = remotePerson.getPassportID() + ":" + subID;
////
////        bank.createAccount(ID);
////        Assertions.assertNotNull(bank.getAccount(ID));
////        Assertions.assertEquals(0, localPerson.getAccounts().size());
////        localPerson = bank.getLocalPerson(passportID);
////        Assertions.assertEquals(1, localPerson.getAccounts().size());
////        Assertions.assertNull(bank.getRemotePerson(subID));
////
////    }
////
////    @Test
////    void testAmount() throws RemoteException {
////        bank = new RemoteBank(PORT);
////        String name = "Person";
////        String surname = "Personvich";
////        String passportID = "8900";
////        bank.createPerson(name, surname, passportID);
////        Person remotePerson = bank.getRemotePerson(passportID);
////        Assertions.assertNotNull(remotePerson);
////        String subID = "1082";
////        String ID = remotePerson.getPassportID() + ":" + subID;
////        bank.createAccount(ID);
////        Account account = bank.getAccount(ID);
////        account.setAmount(account.getAmount() + 100);
////        account.setAmount(account.getAmount() + 100);
////        LocalPerson localPerson = bank.getLocalPerson(passportID);
////        Assertions.assertNotNull(localPerson);
////        Assertions.assertEquals(200, account.getAmount());
////    }
////
////    @Test
////    void searchingAndCreatingPersonsBorn() throws RemoteException {
////        final int CONST_COUNT_OF_PERSON = 100;
////        int currentCountOfPerson = 0;
////        bank = new RemoteBank(PORT);
////        String name, surname, passportID;
////        Person testovich;
////        for (int i = 0; i < CONST_COUNT_OF_PERSON; i++) {
////            name = "aboba" + i;
////            surname = "bebra" + i;
////            passportID = "000" + i;
////            bank.createPerson(name, surname, passportID);
////            testovich = bank.getRemotePerson(passportID);
////            Assertions.assertNotNull(testovich);
////            String subID = String.valueOf(i);
////            String ID = testovich.getPassportID() + ":" + subID;
////            bank.createAccount(ID);
////            localPerson = bank.getLocalPerson(passportID);
////            Assertions.assertNotNull(localPerson);
////            currentCountOfPerson++;
////        }
////        Assertions.assertEquals(currentCountOfPerson, CONST_COUNT_OF_PERSON);
////    }
////
////    @Test
////    void remotePersonBeforeLocalTest() throws RemoteException {
////        bank = new RemoteBank(PORT);
////        bank.createPerson("VovaBefore", "VovachicBefore", "635478");
////        Person remotePerson = bank.getRemotePerson("635478");
////
////        Assertions.assertNotNull(remotePerson);
////        String subID = "1082";
////        String passportId = remotePerson.getPassportID();
////        String ID = passportId + ":" + subID;
////        Assertions.assertNotNull(bank.createAccount(ID));
////        Account remoteAccount = bank.getAccount(ID);
////
////        LocalPerson localPerson = bank.getLocalPerson(passportId);
////        Assertions.assertNotNull(localPerson);
////        Assertions.assertEquals(1, localPerson.getAccounts().size());
////
////        remoteAccount.setAmount(remoteAccount.getAmount() + 111);
////
////        Person localPerson2 = bank.getLocalPerson(passportId);
////        Assertions.assertNotNull(localPerson2);
////
////        Account localAccount = bank.getAccount(ID);
////        Account localAccount2 = bank.getAccount(ID);
////        Assertions.assertNotNull(localAccount);
////        Assertions.assertNotNull(localAccount2);
////
////        Assertions.assertEquals(localAccount.getAmount(), remoteAccount.getAmount());
////        Assertions.assertEquals(111, localAccount.getAmount());
////    }
////
////    @Test
////    void localPersonBeforeRemoteTest() throws RemoteException {
////        bank = new RemoteBank(PORT);
////        bank.createPerson("localPersonBefore", "1", "7859598780");
////
////        Person remotePerson = bank.getRemotePerson("7859598780");
////        Assertions.assertNotNull(remotePerson);
////
////        String subID = "1082";
////        String passportId = remotePerson.getPassportID();
////        String ID = passportId + ":" + subID;
////
////        Assertions.assertNotNull(bank.createAccount(ID));
////
////        Person localPerson = bank.getLocalPerson("7859598780");
////        Assertions.assertNotNull(localPerson);
////
////        Account localAccount = bank.getLocalPerson(passportId).getAccounts()
////                .stream().filter(ac -> {
////                    try {
////                        return ac.getId().equals(subID);
////                    } catch (RemoteException e) {
////                        throw new RuntimeException(e);
////                    }
////                }).findAny().orElse(null);
////        Assertions.assertNotNull(localAccount);
////
////        localAccount.setAmount(1127);
////
////        Account remoteAccount = bank.getAccount(ID);
////
////        Assertions.assertEquals(1127, localAccount.getAmount());
////        Assertions.assertEquals(0, remoteAccount.getAmount());
////    }
////
////    //____________________
////    private static final int THREAD_COUNT = 10;
////    private static final int OPERATION_COUNT = 1000;
////
////    @Test
////    public void concurrentCreatingAccount() throws InterruptedException {
////        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
////
////        for (int i = 0; i < OPERATION_COUNT; i++) {
////            int finalI = i;
////            executorService.submit(() -> {
////                try {
////                    bank.createPerson("pass:2900" + finalI, "Surname" + finalI, "password" + finalI);
////                } catch (RemoteException e) {
////                    throw new RuntimeException(e);
////                }
////            });
////        }
////
////        executorService.shutdown();
////        executorService.awaitTermination(1, TimeUnit.MINUTES);
////    }
////
////    @Test
////    public void concurrentAccessToAccount() throws InterruptedException, RemoteException {
////        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
////        String subId = "pass:2900";
////        bank = new RemoteBank(PORT);
////        bank.createPerson("A", "BB", "pass");
////        Account account = null;
////        for (int i = 0; i < 10; i++) {
////            int finalI = i;
////            executorService.submit(() -> {
////                try {
////                    bank.createAccount(subId + finalI);
////                } catch (RemoteException e) {
////                    throw new RuntimeException(e);
////                }
////            });
////        }
////        Person person = bank.getRemotePerson("pass");
////        Assertions.assertEquals(10, bank.getAccounts("pass").size());
////        try {
////            account = bank.createAccount(subId);
////        } catch (RemoteException e) {
////            throw new RuntimeException(e);
////        }
////
////        for (int i = 0; i < OPERATION_COUNT; i++) {
////            Account finalAccount = account;
////            executorService.submit(() -> {
////                try {
////                    finalAccount.setAmount(finalAccount.getAmount() + 100);
////                } catch (RemoteException e) {
////                    throw new RuntimeException(e);
////                }
////            });
////        }
//
////
////        executorService.shutdown();
////        executorService.awaitTermination(1, TimeUnit.MINUTES);
////
////        int expectedAmount = OPERATION_COUNT * 100;
////        Assert.assertEquals(expectedAmount, account.getAmount());
// //   }
//}