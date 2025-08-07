package com.liboshuai.demo.thread.safe.wait;

/**
 * 使用两个线程打印 1-100。线程1, 线程2 交替打印
 */
public class PrintNumberTest {
    public static void main(String[] args) throws InterruptedException {
        PrintNumber printNumber = new PrintNumber();
        Thread t1 = new Thread(printNumber, "线程1");
        Thread t2 = new Thread(printNumber, "线程2");
        t1.start();
        t2.start();
        System.out.println("主线程等待中......");
        t1.join();
        t2.join();
        System.out.println("主线程等待完毕......");
    }
}

class PrintNumber implements Runnable {

    private int m = 1;

    @Override
    public void run() {
        while (true) {
            synchronized (this) {
                if (m <= 100) {
                    // 线程1一进来就唤醒所有等待PrintNumber实例对象锁的线程，但是此时没有线程wait，所以这个行代码第一次没有任何作用
                    // 线程2一进来就唤醒阻塞中的线程1，但是此时this锁被线程2持有，线程1只能等待线程1释放锁
                    notifyAll();
                    System.out.println(Thread.currentThread().getName() + ": " + m);
                    m++;
                    // 为什么要添加这行代码？
                    // 如果不添加这个判断，则在线程1将m变成101时，还会继续执行wait将自己阻塞住
                    // 然后线程2获取到锁进入同步代码块，但是此时m已经大于100了，不满足m<=100
                    // 所以notifyAll()就不会被执行，那么线程1就会被永远wait。
                    // 又因为线程1为用户线程，程序就不会结束，一直挂起
                    if (m <= 100) {
                        try {
                            // 从1变成2后，线程1阻塞住，然后释放锁。以便线程2可以获取到锁，来执行代码
                            // 从2变成3后，线程2阻塞住，然后释放锁。以便线程1可以获取到锁，来执行代码
                            wait();
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                } else {
                    break;
                }                
            }
        }
    }

}