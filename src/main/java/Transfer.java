import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Transfer implements Callable<Boolean> {
    private BankAccount acc1;
    private BankAccount acc2;
    private int amount;
    private int id;
    private CountDownLatch startLatch;

    public Transfer(int id,
                    BankAccount accountFrom,
                    BankAccount accountTo,
                    int amount) {
        this.id = id;
        acc1 = accountFrom;
        acc2 = accountTo;
        this.amount = amount;
    }

    @Override
    public Boolean call() throws Exception {
        /**
         * Make thread to fall asleep & wait for others
         * with synchronizer CountDownLatch
         */
        System.out.println("Transfer " + id + " is waiting to start..");
        startLatch.await();

        System.out.println("Transfer " + id + " initiated");
        if(acc1.getBalance() < amount) {
            throw new InsufficientFundsException("Not enough money");
        }
        boolean wasOperationSuccess = true;
        final int WAIT_SEC = 3;

        if(acc1.getLock().tryLock(WAIT_SEC, TimeUnit.SECONDS)) {
            try {
                if(acc2.getLock().tryLock(WAIT_SEC, TimeUnit.SECONDS)) {
                    try {
                        acc1.withdraw(amount);
                        acc2.deposit(amount);
                        //simulating longer process
                        Thread.sleep(new Random().nextInt(5+1)*1000);
                    }
                    finally {
                        acc2.getLock().unlock();
                    }
                }
                else {
                    System.out.println("Transfer " + id + " attempt failed");
                    acc2.incFailedTransferCount();
                    acc1.incFailedTransferCount();
                    wasOperationSuccess = false;
                }
            }
            finally {
                acc1.getLock().unlock();
            }
            System.out.println("Transfer " + id + " was successful");
        }
        else {
            System.out.println("Transfer " + id + " attempt failed");
            acc1.incFailedTransferCount();
            acc2.incFailedTransferCount();
            wasOperationSuccess = false;
        }
        return wasOperationSuccess;
    }

    public CountDownLatch getStartLatch() {
        return startLatch;
    }

    public void setStartLatch(CountDownLatch startLatch) {
        this.startLatch = startLatch;
    }
}
