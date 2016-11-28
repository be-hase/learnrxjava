package learnrxjava.examples;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public class HelloWorldTest {
    @Test
    public void test1() {
        // Hello World
        Observable.create(subscriber -> {
            subscriber.onNext("Hello World!");
            subscriber.onCompleted();
        }).subscribe(this::print);
    }

    @Test
    public void test2() {
        // shorten by using helper method
        Observable.just("Hello", "World!")
                  .subscribe(this::print);
    }

    @Test
    public void test3() {
        // add onError and onComplete listeners
        Observable.just("Hello World!")
                  .subscribe(this::print,
                             Throwable::printStackTrace,
                             () -> print("Done"));
    }

    @Test
    public void test4() {
        // expand to show full classes
        Observable.create(new OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                subscriber.onNext("Hello World!");
                subscriber.onCompleted();
            }
        }).subscribe(new Subscriber<String>() {
            @Override
            public void onCompleted() {
                print("Done");
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onNext(String t) {
                print(t);
            }
        });
    }

    @Test
    public void test5() {
        // add error propagation
        Observable.create(subscriber -> {
            try {
                subscriber.onNext("Hello World!");
                subscriber.onCompleted();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }).subscribe(this::print);
    }

    @Test
    public void test6() {
        // add concurrency (manually)
        Observable.create(subscriber -> {
            new Thread(() -> {
                try {
                    print("onNext !");
                    subscriber.onNext("data");
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).start();
        }).subscribe(data -> print("Got " + data));
    }

    @Test
    public void test7() {
        // add concurrency (using a Scheduler)
        Observable.create(subscriber -> {
            try {
                print("onNext !");
                subscriber.onNext("data");
                subscriber.onCompleted();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.io())
                  .subscribe(data -> print("Got " + data));
    }

    @Test
    public void test8() {
        // add operator
        Observable.create(subscriber -> {
            try {
                print("onNext !");
                subscriber.onNext("data");
                subscriber.onCompleted();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.io())
                  .map(data -> data + " --> at " + System.currentTimeMillis())
                  .subscribe(data -> print("Got " + data));
    }

    @Test
    public void test9() throws Exception {
        // add error handling
        Observable.create(subscriber -> {
            try {
                print("onNext !");
                subscriber.onNext("data");
                throw new RuntimeException();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.io())
                  .onErrorResumeNext(e -> Observable.just("Fallback Data"))
                  .map(data -> data + " --> at " + System.currentTimeMillis())
                  .subscribe(this::print);
    }

    @Test
    public void test10() {
        // infinite
        Observable.create(subscriber -> {
            int i = 0;
            while (!subscriber.isUnsubscribed()) {
                print("onNext:" + i);
                subscriber.onNext(i++);
            }
        }).take(10).subscribe(this::print);
    }

    @Test
    public void test11() {
        // error handle 1
        Observable.create(subscriber -> {
            throw new RuntimeException("failed!");
        }).onErrorResumeNext(throwable -> {
            return Observable.just("fallback value");
        }).subscribe(this::print);
    }

    @Test
    public void test12() {
        // error handle 2
        Observable.create(subscriber -> {
            throw new RuntimeException("failed!");
        }).onErrorResumeNext(Observable.just("fallback value"))
                  .subscribe(this::print);
    }

    @Test
    public void test13() {
        // error handle 3
        Observable.create(subscriber -> {
            throw new RuntimeException("failed!");
        }).onErrorReturn(throwable -> {
            return "fallback value";
        }).subscribe(this::print);
    }

    @Test
    public void test14() {
        // retry
        Observable.create(subscriber -> {
            throw new RuntimeException("failed!");
        }).retryWhen(attempts -> {
            return attempts.zipWith(Observable.range(1, 3), (throwable, i) -> i)
                           .flatMap(i -> {
                               print("delay retry by " + i + " second(s)");
                               return Observable.timer(i, TimeUnit.SECONDS);
                           }).concatWith(Observable.error(new RuntimeException("Exceeded 3 retries")));
        })
                  .subscribe(this::print, t -> t.printStackTrace());

        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
        }
    }

    public void print(Object s) {
        System.out.println("[" + Thread.currentThread().getName() + "] " + s);
    }
}