package fengfei.berain.client.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fengfei.berain.client.BerainClient;

public class WatchedService implements Runnable, Executor {

	private static WatchedService watchedService = new WatchedService();
	private ExecutorService executor = Executors.newFixedThreadPool(Runtime
			.getRuntime()
			.availableProcessors() * 2 + 1);
	private Set<BerainHttpClient> berainClients = new HashSet<>();
	private long sleepMillis = 3000;

	private boolean isRunning = false;

	public static WatchedService get() {
		return watchedService;
	}

	private WatchedService() {

	}

	public void start() {
		if (isRunning) {
			return;
		}
		isRunning = true;
		executor.execute(this);
	}

	public void stop() {
		isRunning = false;
		executor.shutdown();
	}

	@Override
	public void run() {

		while (isRunning) {
			try {
				for (BerainHttpClient client : berainClients) {
					executor.execute(client);
				}

				Thread.sleep(sleepMillis);
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
		}
	}

	@Override
	public void execute(Runnable command) {
		executor.execute(command);
	}

	public void addBerainClient(BerainHttpClient berainClient) {
		berainClients.add(berainClient);
	}

	public void removeBerainClient(BerainClient berainClient) {
		berainClients.remove(berainClient);
	}

	public static void main(String[] args) {
		WatchedService service = new WatchedService();
		service.run();
		// service.executor.shutdown();
	}

}
