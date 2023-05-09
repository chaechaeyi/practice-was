package com.example.counter;

public class RaceConditionDemo {
    public static void main(String args[]) {
        /**
         * 싱글턴으로 작성하지 않았을 때 발생할 수있는 사항 테스트 (thread safe 하지 않음) - 동기화 처리하기 전
         * 
         * 아래와 같이 결과 값이 나옴 (하나의 자원을 공유하게 되었을 때 결과값, race condition)
         * Value for Thread After increment Thread-1 1
         * Value for Thread After increment Thread-3 3
         * Value for Thread After increment Thread-2 2
         * Value for Thread at last Thread-1 2
         * Value for Thread at last Thread-2 0
         * Value for Thread at last Thread-3 1
         *
         * 동기화 처리 했을 때 결과 값
         * Value for Thread After increment Thread-1 1
         * Value for Thread at last Thread-1 0
         * Value for Thread After increment Thread-3 1
         * Value for Thread at last Thread-3 0
         * Value for Thread After increment Thread-2 1
         * Value for Thread at last Thread-2 0
         *
         */
        Counter counter = new Counter();
        Thread t1 = new Thread(counter, "Thread-1");
        Thread t2 = new Thread(counter, "Thread-2");
        Thread t3 = new Thread(counter, "Thread-3");

        t1.start();
        t2.start();
        t3.start();
    }
}