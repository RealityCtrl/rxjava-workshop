package com.nurkiewicz.rxjava;

import io.reactivex.Flowable;
import org.junit.Ignore;
import org.junit.Test;

import static com.nurkiewicz.rxjava.R30_Zip.LOREM_IPSUM;

import java.util.List;
import java.util.concurrent.TimeUnit;

//@Ignore
public class R31_WindowBuffer {
	
	@Test
	public void test1()throws Exception{
		Flowable.range(1, 10)
		.buffer(3,4)
		.subscribe(str -> System.out.println(str));
	}
	
	/**
	 * Hint: use buffer()
	 */
	@Test
	public void everyThirdWordUsingBuffer() throws Exception {
		//given
		Flowable<String> everyThirdWord = LOREM_IPSUM
											.skip(2)
											.buffer(1,3)
											.map(list -> list.get(0));
		
		//then
		everyThirdWord
				.test()
				.assertValues("dolor", "consectetur")
				.assertNoErrors();
	}
	
	/**
	 * Hint: use window()
	 * Hint: use elementAt()
	 */
	@Test
	public void everyThirdWordUsingWindow() throws Exception {
		//given
		Flowable<String> everyThirdWord1 = LOREM_IPSUM;
		
		Flowable<String> everyThirdWord = everyThirdWord1
											.skip(2)
											.window(1, 3)
											.flatMap(flow -> flow
													.elementAt(0)
													.toFlowable());
		
		Flowable<String> everyThirdWord2 = everyThirdWord1
				.window(3)
				.flatMap(flow -> flow
						.elementAt(2)
						.toFlowable());
		
		Flowable<String> everyThirdWord3 = everyThirdWord1
												.window(3)
												.flatMapMaybe(flow -> flow
														.elementAt(2));
		
		//then
		everyThirdWord
				.test()
				.assertValues("dolor", "consectetur")
				.assertNoErrors();
		
		everyThirdWord2
		.test()
		.assertValues("dolor", "consectetur")
		.assertNoErrors();
		
		everyThirdWord3
		.test()
		.assertValues("dolor", "consectetur")
		.assertNoErrors();
	}
	
	@Test
	public void test2()throws Exception{
		Flowable.interval(10, TimeUnit.MICROSECONDS)
		.buffer(1, TimeUnit.SECONDS)
		.map(myList -> myList.size())
		.subscribe(str -> System.out.println(str));
		
		TimeUnit.SECONDS.sleep(5);
	}
	
	@Test
	public void test3()throws Exception{
		Flowable.interval(10, TimeUnit.MICROSECONDS)
		.window(1, TimeUnit.SECONDS)
		.flatMapSingle(win -> win.count())
		.subscribe(str -> System.out.println(str));
		
		TimeUnit.SECONDS.sleep(5);
	}
}

