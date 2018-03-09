package com.nurkiewicz.rxjava;

import io.reactivex.Flowable;
import io.reactivex.subscribers.TestSubscriber;
import org.junit.Ignore;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

//@Ignore
public class R01_JustFrom {
	
	@Test
	public void shouldCreateFlowableFromConstants() throws Exception {
		Flowable<String> obs = Flowable.just("A", "B", "C");
		
		//would block if infinite stream as synchronous
		obs.subscribe(
				(String x) -> System.out.println("Got: " + x),
				ex -> ex.printStackTrace(),
				() -> System.out.println("Completed")
		);
		
		
		
		//equivalent
		obs.subscribe(new Subscriber<String>(){

			@Override
			public void onComplete() {
				System.out.println("Completed");
				
			}

			@Override
			public void onError(Throwable arg0) {
				arg0.printStackTrace();
				
			}

			@Override
			public void onNext(String arg0) {
				System.out.println("Got: " + arg0);
				
			}

			@Override
			public void onSubscribe(Subscription arg0) {
				// TODO Auto-generated method stub
				
			}
			
			//Observeable.interval(1) observe every 1 second
		
		});
		
	}
	
	@Test
	public void shouldEmitValues() throws Exception {
		Flowable<String> obs = Flowable.just("A", "B", "C");
		
		final TestSubscriber<String> subscriber = obs.test();
		
		subscriber
				.assertValues("A", "B", "C")
				.assertComplete();
		//alternative
		obs.test()
			.assertValues("A", "B", "C")
			.assertComplete();
	}
	
}
