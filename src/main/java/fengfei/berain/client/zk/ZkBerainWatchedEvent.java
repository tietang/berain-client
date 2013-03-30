package fengfei.berain.client.zk;

import org.apache.zookeeper.WatchedEvent;

import fengfei.berain.client.BerainWatchedEvent;
import fengfei.berain.client.EventType;

public class ZkBerainWatchedEvent extends BerainWatchedEvent {

	WatchedEvent watchedEvent;

	public ZkBerainWatchedEvent(WatchedEvent event) {
		this.watchedEvent = event;
		this.eventType = EventType.fromInt(event.getType().getIntValue());
		this.path = event.getPath();
	}

	public WatchedEvent getWatchedEvent() {
		return watchedEvent;
	}
}
