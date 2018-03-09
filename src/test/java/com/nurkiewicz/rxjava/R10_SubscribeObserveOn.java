package com.nurkiewicz.rxjava;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.nurkiewicz.rxjava.util.Sleeper;
import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.TestSubscriber;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

//@Ignore
public class R10_SubscribeObserveOn {

	private static final Logger log = LoggerFactory.getLogger(R10_SubscribeObserveOn.class);

	@Test
	public void subscribeOn() throws Exception {
		Flowable<BigDecimal> obs = slowFromCallable();

		obs
				.subscribeOn(Schedulers.io()) //use thread pool
				.subscribe(
						x -> log.info("Got: {}", x)
				);
		
		log.info("After subscribe");
		Sleeper.sleep(ofMillis(1_100)); //to keep main thread alive till other thread completes
	}

	@Test
	public void subscribeOnForEach() throws Exception {
		Flowable<BigDecimal> obs = slowFromCallable();

		obs
				.subscribeOn(Schedulers.io())
				.subscribe(
						x -> log.info("Got: {}", x)
				);
		Sleeper.sleep(ofMillis(1_100));
	}

	private Flowable<BigDecimal> slowFromCallable() {
		return Flowable.fromCallable(() -> {
			log.info("Starting");
			Sleeper.sleep(ofSeconds(1));
			log.info("Done");
			return BigDecimal.TEN;
		});
	}
	
	@Test
	public void observeOn1() throws Exception {
		slowFromCallable()
				.subscribeOn(Schedulers.io()) //subsequent steps happen in context of this thread
				.doOnNext(x -> log.info("A: {}", x))
				.doOnNext(x -> log.info("B: {}", x))
				.doOnNext(x -> log.info("C: {}", x))
				.subscribe(
						x -> log.info("Got: {}", x)
				);
		Sleeper.sleep(ofMillis(1_100));
	}

	@Test
	public void observeOn() throws Exception {
		//observe on uses new thread pool
		slowFromCallable()
				.subscribeOn(Schedulers.io()) //switch to a thread from IO thread pool
				.doOnNext(x -> log.info("A: {}", x)) //equivalent to Stream.peek(x -> log.info("A: {}", x)
				.observeOn(Schedulers.computation()) //switch to a thread from the computation thread pool
				.doOnNext(x -> log.info("B: {}", x)) //not good idea to do logic here, slows down computation
				.observeOn(Schedulers.newThread())   //Creates a new thread
				.doOnNext(x -> log.info("C: {}", x)) //now runs on new thread from Schedulers.newThread()
				.subscribe(							//runs on new thread from Schedulers.newThread()
						x -> log.info("Got: {}", x)
				);
		Sleeper.sleep(ofMillis(1_100));
	}

	/**
	 * TODO Create CustomExecutor
	 */
	@Test
	public void customExecutor() throws Exception {
		final TestSubscriber<BigDecimal> subscriber = slowFromCallable()
				.subscribeOn(myCustomScheduler())
				.test();
		await().until(() -> {
					Thread lastSeenThread = subscriber.lastThread();
					assertThat(lastSeenThread).isNotNull();
					assertThat(lastSeenThread.getName()).matches("CustomExecutor-\\d+");
				}
		);
	}

	/**
	 * Hint: Schedulers.from()
	 * Hint: ThreadFactoryBuilder
	 */
	private Scheduler myCustomScheduler() {
		final ThreadFactory threadFactory = new ThreadFactoryBuilder()
												.setNameFormat("CustomExecutor-%d")
												.build();
		final ExecutorService eService = Executors.newFixedThreadPool(10,threadFactory);
		final Scheduler scheduler = Schedulers.from(eService);
		return scheduler;
	}
	
}
