import javax.naming.InsufficientResourcesException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Operations {

    public static void main(String[] args) throws InsufficientFundsException, InterruptedException {
        final BankAccount account1 = new BankAccount(1000);
        final BankAccount account2 = new BankAccount(2000);


        new Thread(new Runnable() {
            public void run() {
                try {
                    transfer(account1, account2, 500);
                } catch (InsufficientFundsException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        transfer(account2, account1, 300);
    }

    /**
     * If to make it synchronized, you'd block all the code in the class
     * & all the thread would be waiting in line, without being able to access transfer()
     * and the rest of code in the class (since the lock will be on 'this' instance)
     * Morale = make the synchronized block as small as possible to avoid blocking
     * & don't synchronize the shared code (better, synchronize BankAccount)
     */
    static void transfer(BankAccount acc1, BankAccount acc2, int amount) throws InsufficientFundsException, InterruptedException {
        if(acc1.getBalance() < amount) {
            throw new InsufficientFundsException("Not enough money");
        }

    /**
     * Example of deadlock
     * When 2 thread perform operation on 2 account in different order
     * Thread 0 take lock on acc1 & then tries to access lock acc2
     * Thread main takes lock on acc2 & then tries to access lock acc1
     * THe result = both threads are blocking & infinitely awaiting each other
     */
/*        synchronized (acc1) {
            System.out.println("Lock for Account1 was obtained by " + Thread.currentThread().getName());
            Thread.sleep(1000);
            synchronized (acc2) {
                System.out.println("Lock for Account2 was obtained by " + Thread.currentThread().getName());
                acc1.withdraw(amount);
                acc2.deposit(amount);
            }
        }*/

        final int WAIT_SEC = 3;
        /**
         * Method tryLock() returns boolean
         * Upon success(true), the lock will be locked automatically
         * Otherwise, the thread will block(sleep) for WAIT_SEC period & thus, trying to
         * get the lock;
         * if after WAIT_SEC thread wasn't able to lock, it fails and skips the sections of if block
         */

        if(acc1.getLock().tryLock(WAIT_SEC, TimeUnit.SECONDS)) {
            try {
                if(acc2.getLock().tryLock(WAIT_SEC, TimeUnit.SECONDS)) {
                   try {
                       acc1.withdraw(amount);
                       acc2.deposit(amount);
                   }
                   finally {
                       acc2.getLock().unlock();
                   }
                }
                else {
                    System.out.println("Transfer attempt failed");
                    acc2.incFailedTransferCount();
                    acc1.incFailedTransferCount();
                }
            }
            finally {
                acc1.getLock().unlock();
            }
            System.out.println("Transfer was successful");
        }
        else {
            System.out.println("Transfer attempt failed");
            acc1.incFailedTransferCount();
            acc2.incFailedTransferCount();
        }
    }



}
