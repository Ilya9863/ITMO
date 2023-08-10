//package info.kgeorgiy.ja.kurkin.bankRMI;
//
//import org.junit.Assert;
//import org.junit.BeforeClass;
//import org.junit.Test;
//
//import java.net.MalformedURLException;
//import java.rmi.Naming;
//import java.rmi.RemoteException;
//import java.rmi.registry.LocateRegistry;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.atomic.AtomicInteger;
//
//public class BankTester {
//
//    private static final int PORT = 8889;
//    private static Bank bank;
//    private static LocalPerson localPerson;
//
//    /**
//     * compiling before class
//     *
//     * @throws RemoteException       if a data transfer error has occurred
//     * @throws MalformedURLException if a data transfer error has occurred
//     */
//
//    @Before
//    public static void beforeClass() throws RemoteException, MalformedURLException {
//        bank = new RemoteBank(PORT);
//        LocateRegistry.createRegistry(PORT);
//        Naming.rebind("//localhost:" + PORT + "/bank", bank);
//    }
//
//    /**
//     * Test for creating local and Remote person
//     *
//     * @throws RemoteException if a data transfer error has occurred
//     */
//
//    @Test
//    public void creatingLocalAndRemotePerson() throws RemoteException {
//        bank = new RemoteBank(PORT);
//        String name = "Person";
//        String surname = "Personvich";
//        String passportID = "8900";
//        bank.createPerson(name, surname, passportID);
//        Person remotePerson = bank.getRemotePerson(passportID);
//        LocalPerson localPerson = bank.getLocalPerson(passportID);
//
//        String subID = "4";
//        String ID = remotePerson.getPassportID() + ":" + subID;
//
//        bank.createAccount(ID);
//        Assert.assertNotNull(bank.getAccount(ID));
//        Assert.assertEquals(0, localPerson.getAccounts().size());
//        localPerson = bank.getLocalPerson(passportID);
//        Assert.assertEquals(1, localPerson.getAccounts().size());
//        Assert.assertNull(bank.getRemotePerson(subID));
//
//    }
//
//    /**
//     * Testing function Amount
//     *
//     * @throws RemoteException if a data transfer error has occurred
//     */
//
//    @Test
//    public void testAmount() throws RemoteException {
//        bank = new RemoteBank(PORT);
//        String name = "Person";
//        String surname = "Personvich";
//        String passportID = "8900";
//        bank.createPerson(name, surname, passportID);
//        Person remotePerson = bank.getRemotePerson(passportID);
//        Assert.assertNotNull(remotePerson);
//        String subID = "1082";
//        String ID = remotePerson.getPassportID() + ":" + subID;
//        bank.createAccount(ID);
//        Account account = bank.getAccount(ID);
//        account.setAmount(account.getAmount() + 100);
//        account.setAmount(account.getAmount() + 100);
//        LocalPerson localPerson = bank.getLocalPerson(passportID);
//        Assert.assertNotNull(localPerson);
//        Assert.assertEquals(200, account.getAmount());
//    }
//
//    /**
//     * stress test searching and creating person
//     *
//     * @throws RemoteException if a data transfer error has occurred
//     */
//    @Test
//    public void searchingAndCreatingPersonsBurn() throws RemoteException {
//        final int CONST_COUNT_OF_PERSON = 100;
//        int currentCountOfPerson = 0;
//        bank = new RemoteBank(PORT);
//        String name, surname, passportID;
//        Person testovich;
//        for (int i = 0; i < CONST_COUNT_OF_PERSON; i++) {
//            name = "aboba" + i;
//            surname = "bebra" + i;
//            passportID = "000" + i;
//            bank.createPerson(name, surname, passportID);
//            testovich = bank.getRemotePerson(passportID);
//            Assert.assertNotNull(testovich);
//            String subID = String.valueOf(i);
//            String ID = testovich.getPassportID() + ":" + subID;
//            bank.createAccount(ID);
//            localPerson = bank.getLocalPerson(passportID);
//            Assert.assertNotNull(localPerson);
//            currentCountOfPerson++;
//        }
//        Assert.assertEquals(currentCountOfPerson, CONST_COUNT_OF_PERSON);
//    }
//
//    /**
//     * Testing creating remote person before local
//     * @throws RemoteException if a data transfer error has occurred
//     */
//
//    @Test
//    public void remotePersonBeforeLocalTest() throws RemoteException {
//        bank = new RemoteBank(PORT);
//        bank.createPerson("VovaBefore", "VovachicBefore", "635478");
//        Person remotePerson = bank.getRemotePerson("635478");
//
//        Assert.assertNotNull(remotePerson);
//        String subID = "1082";
//        String passportId = remotePerson.getPassportID();
//        String ID = passportId + ":" + subID;
//        Assert.assertNotNull(bank.createAccount(ID));
//        Account remoteAccount = bank.getAccount(ID);
//
//        LocalPerson localPerson = bank.getLocalPerson(passportId);
//        Assert.assertNotNull(localPerson);
//        Assert.assertEquals(1, localPerson.getAccounts().size());
//
//        remoteAccount.setAmount(remoteAccount.getAmount() + 111);
//
//        Person localPerson2 = bank.getLocalPerson(passportId);
//        Assert.assertNotNull(localPerson2);
//
//        Account localAccount = bank.getAccount(ID);
//        Account localAccount2 = bank.getAccount(ID);
//        Assert.assertNotNull(localAccount);
//        Assert.assertNotNull(localAccount2);
//
//        Assert.assertEquals(localAccount.getAmount(), remoteAccount.getAmount());
//        Assert.assertEquals(111, localAccount.getAmount());
//    }
//
//    /**
//     * Testing creating remote person before local
//     * @throws RemoteException if a data transfer error has occurred
//     */
//    @Test
//    public void localPersonBeforeRemoteTest() throws RemoteException {
//        bank = new RemoteBank(PORT);
//        bank.createPerson("localPersonBefore", "1", "7859598780");
//
//        Person remotePerson = bank.getRemotePerson("7859598780");
//        Assert.assertNotNull(remotePerson);
//
//        String subID = "1082";
//        String passportId = remotePerson.getPassportID();
//        String ID = passportId + ":" + subID;
//
//        Assert.assertNotNull(bank.createAccount(ID));
//
//        Person localPerson = bank.getLocalPerson("7859598780");
//        Assert.assertNotNull(localPerson);
//
//        Account localAccount = bank.getLocalPerson(passportId).getAccounts()
//                .stream().filter(ac -> {
//                    try {
//                        return ac.getId().equals(subID);
//                    } catch (RemoteException e) {
//                        throw new RuntimeException(e);
//                    }
//                }).findAny().orElse(null);
//        Assert.assertNotNull(localAccount);
//
//        localAccount.setAmount(1127);
//
//        Account remoteAccount = bank.getAccount(ID);
//
//        Assert.assertEquals(1127, localAccount.getAmount());
//        Assert.assertEquals(0, remoteAccount.getAmount());
//    }
//
//    //____________________
//    private static final int THREAD_COUNT = 10;
//    private static final int OPERATION_COUNT = 1000;
//
//    /**
//     * testing concurrent creating account
//     * @throws InterruptedException if a data transfer error has occurred
//     * @throws RemoteException if a data transfer error has occurred
//     */
//
//    @Test
//    public void concurrentAccessToAccount() throws InterruptedException, RemoteException {
//        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
//        String subId = "pass:2900";
//        bank = new RemoteBank(PORT);
//        bank.createPerson("A", "BB", "pass");
//        Account account = null;
//        for (int i = 0; i < OPERATION_COUNT; i++) {
//            int finalI = i;
//            executorService.submit(() -> {
//                try {
//                    bank.createAccount(subId + finalI);
//                    System.out.println(finalI);
//                } catch (RemoteException e) {
//                    throw new RuntimeException(e);
//                }
//            });
//        }
//        executorService.shutdown();
//        executorService.awaitTermination(1, TimeUnit.SECONDS);
//        Person person = bank.getRemotePerson("pass");
//        Assert.assertEquals(OPERATION_COUNT, bank.getAccounts("pass").size());
//        try {
//            account = bank.createAccount(subId);
//        } catch (RemoteException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    /**
//     * testing concurrent creating person
//     * @throws InterruptedException if a data transfer error has occurred
//     * @throws RemoteException if a data transfer error has occurred
//     */
//
//    @Test
//    public void concurrentCreatingPerson() throws InterruptedException, RemoteException {
//        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
//        bank.createPerson("A", "BB", "pass");
//        AtomicInteger counter = new AtomicInteger();
//
//        for (int i = 0; i < OPERATION_COUNT; i++) {
//            int finalI = i;
//            executorService.submit(() -> {
//                try {
//                    counter.getAndIncrement();
//                    bank.createPerson("pass:2900" + finalI, "Surname" + finalI, "password" + finalI);
//                } catch (RemoteException e) {
//                    throw new RuntimeException(e);
//                }
//            });
//        }
//        executorService.shutdown();
//        executorService.awaitTermination(1, TimeUnit.MINUTES);
//        Assert.assertEquals(OPERATION_COUNT, counter.intValue());
//    }
//}
