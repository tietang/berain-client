package fengfei.berain.client;

public abstract class Wather  implements Runnable {

	private BerainWatchedEvent event;

	@Override
	public void run() {
		call(event);
	}

	public void setEvent(BerainWatchedEvent event) {
		this.event = event;
	}

	public abstract void call(BerainWatchedEvent event);
}
