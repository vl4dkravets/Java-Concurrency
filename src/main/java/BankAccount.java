import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BankAccount {
    /**
     * Volatile won't help
     * It will help in the situation when 2 threads access the latest value
     * However, since operations like increments aren't thread-safe, (atomic)
     * then even with the use of volatile, it will still be possible that
     * 2 threads would access the same variable, but get different results
     */
    private int balance;

    private AtomicInteger failCounter = new AtomicInteger(0);

    private Lock lock = new ReentrantLock();

    public BankAccount(int balance) {
        this.balance = balance;
    }

    public void withdraw(int amount) {
        balance -= amount;
    }

    public void deposit(int amount) {
        balance += amount;
    }

    public int getBalance() {
        return balance;
    }

    public Lock getLock() {
        return lock;
    }

    public void incFailedTransferCount() {
        failCounter.incrementAndGet();
    }

    public int getFailCounter() {
        return failCounter.get();
    }
}
