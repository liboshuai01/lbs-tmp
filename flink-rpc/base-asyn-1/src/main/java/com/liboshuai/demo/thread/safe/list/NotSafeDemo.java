package com.liboshuai.demo.thread.safe.list;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotSafeDemo {
    public static void main(String[] args) {
        ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(30);
        MyList myList = new MyList();
        for (int i = 0; i < 30; i++) {
              newFixedThreadPool.execute(new UpdateListTask(myList));
        }
        newFixedThreadPool.shutdown();
    }
}

class MyList {
    private final List<String> list = new CopyOnWriteArrayList<>();
    
    public void update(String data) {
        list.add(data);
        System.out.printf("[%s] 线程添加了一个元素，现在 list 存在的元素都有: [%s]%n", Thread.currentThread().getName(), list);
    }
}

class UpdateListTask implements Runnable {

    private final MyList myList;

    public UpdateListTask(MyList myList) {
        this.myList = myList;
    }

    @Override
    public void run() {
        myList.update(UUID.randomUUID().toString().substring(0,4));
    }

}