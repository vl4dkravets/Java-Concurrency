import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.*;

public class Operations2 {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        final BankAccount acc1 = new BankAccount(10000);
        final BankAccount acc2 = new BankAccount(20000);
        Random rand = new Random();

        ExecutorService service = Executors.newFixedThreadPool(3);
        ArrayList<Future> transferResults = new ArrayList<Future>();


        /**
         * A thread which will be scheduled to run after each second & display some info
         * about accounts
         */
        ScheduledExecutorService scheduledService = Executors.newScheduledThreadPool(1);
        scheduledService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                System.out.println("Bank account 1 - failed tranfer attempts#: " + acc1.getFailCounter());
                System.out.println("Bank account 2 - failed tranfer attempts#: " + acc2.getFailCounter());
            }
        }, 2, 1, TimeUnit.SECONDS);


        /**
         * Thread, which calls await() on the instance of CountDownLatch
         * will fall asleep; but when someone will call countDown()
         * & the counter reaches 0, all the threads which called await() earlier will wake up & start
         * automatically all at once
         *
         */
        CountDownLatch startLatch = new CountDownLatch(1);

        /**
         * submit() method return future proxy object
         * which has get() method to obtain return value from a thread
         */
        for(int i = 0; i < 10; i++) {
            Transfer transfer = new Transfer(i, acc1, acc2, rand.nextInt(400));
            transfer.setStartLatch(startLatch);
            Future<Boolean> res = service.submit(transfer);
            transferResults.add(res);
        }

        /**
         * When all the transfers have been set up, count down the latch;
         * Thus its counter reaches zero, which make all the threads which fell asleep on
         * the latch wake up all at once & start running
         */
        Thread.sleep(2000);
        startLatch.countDown();
        System.out.println("CountDownLatch reached zero!");


        /**
         * Service will run the sumbitted tasks in the background, but
         * won't accept new ones
         */
        service.shutdown();

        /**
         * The main thread will block for specified timeout, as if waiting while the
         * tasks will finish their work
         */
        System.out.println("awaitTermination: " + service.awaitTermination(20, TimeUnit.SECONDS));

        for(Future<Boolean> transferRes: transferResults) {
            System.out.println(transferRes.get());
        }
    }
}
