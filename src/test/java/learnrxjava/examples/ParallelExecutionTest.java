package learnrxjava.examples;

import org.junit.Test;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public class ParallelExecutionTest {
    @Test
    public void test1() {
        // mergingAsync
        Observable.merge(getDataAsync(1), getDataAsync(2)).toBlocking().forEach(this::print);
    }

    @Test
    public void test2() {
        // mergingSync
        // here you'll see the delay as each is executed synchronously
        Observable.merge(getDataSync(1), getDataSync(2)).toBlocking().forEach(this::print);
    }

    @Test
    public void test3() {
        // mergingSyncMadeAsync
        // if you have something synchronous and want to make it async, you can schedule it like this
        // so here we see both executed concurrently
        Observable.merge(getDataSync(1).subscribeOn(Schedulers.io()),
                         getDataSync(2).subscribeOn(Schedulers.io())).toBlocking().forEach(this::print);
    }

    @Test
    public void test4() {
        // flatMapExampleAsync
        Observable.range(0, 5).flatMap(i -> {
            return getDataAsync(i);
        }).toBlocking().forEach(this::print);
    }

    @Test
    public void test5() {
        // flatMapExampleSync
        Observable.range(0, 5).flatMap(i -> {
            return getDataSync(i);
        }).toBlocking().forEach(this::print);
    }

    @Test
    public void test6() {
        // flatMapBufferedExampleAsync
        Observable.range(0, 30).buffer(3).flatMap(i -> {
            return Observable.from(i).subscribeOn(Schedulers.computation()).map(item -> {
                // simulate computational work
                try {
                    Thread.sleep(1);
                } catch (Exception e) {
                }
                return item + " processed";
            });
        }).toBlocking().forEach(this::print);
    }

    @Test
    public void test7() {
        // flatMapWindowedExampleAsync
        Observable.range(0, 30).window(3).flatMap(work -> {
            return work.observeOn(Schedulers.computation()).map(item -> {
                // simulate computational work
                try {
                    Thread.sleep(1);
                } catch (Exception e) {
                }
                return item + " processed";
            });
        }).toBlocking().forEach(this::print);
    }

    @Test
    public void test() throws InterruptedException {
        Observable.create(subscriber -> {
            print("subscribe: 1");
            subscriber.onNext(1);
            print("subscribe: 2");
            subscriber.onNext(2);
            subscriber.onCompleted();
        })
                  .subscribeOn(Schedulers.newThread())
                  .map(i -> {
                      int result = (Integer) i + 1;
                      print("call +1");
                      return result;
                  })
                  .observeOn(Schedulers.newThread())
                  .map(i -> {
                      int result = i + 2;
                      print("call +2");
                      return result;
                  })
                  .subscribe(this::print);

        Thread.sleep(5000);
    }

    // artificial representations of IO work
    public Observable<Integer> getDataAsync(int i) {
        return getDataSync(i).subscribeOn(Schedulers.io());
    }

    public Observable<Integer> getDataSync(int i) {
        return Observable.create((Subscriber<? super Integer> s) -> {
            // simulate latency
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
            print("onNext: " + i);
            s.onNext(i);
            s.onCompleted();
        });
    }

    public void print(Object s) {
        System.out.println("[" + Thread.currentThread().getName() + "] " + s);
    }
}