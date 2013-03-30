package fengfei.berain.client.impl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fengfei.berain.client.BerainWatchedEvent;

public class WatchedContainer {

	private final Logger logger = LoggerFactory.getLogger(getClass());
 
	private final Lock lock = new ReentrantLock();
	private final Map<String, Set<BerainWatchedEvent>> watchedEvents = new ConcurrentHashMap<>();

 

	public WatchedContainer() {
	}

	public Set<BerainWatchedEvent> getWatchedEvents(String path) {
		return watchedEvents.get(path);
	}

	public void addWatchedEvent(BerainWatchedEvent event) {
		try {
			lock.lock();

			if (event != null) {
				Set<BerainWatchedEvent> events = watchedEvents.get(event.getPath());
				if (events == null) {
					events = new HashSet<>();
				}
				events.add(event);
				watchedEvents.put(event.getPath(), events);
			}

		} catch (Throwable e) {
			logger.error("addWatchedEvent error", e);

		} finally {
			lock.unlock();
		}
	}

	public void removeWatchedEvent(BerainWatchedEvent event) {

		try {
			lock.lock();
			Set<BerainWatchedEvent> events = watchedEvents.get(event.getPath());
			if (events == null) {
				events = new HashSet<>();
			}
			events.remove(event);

			watchedEvents.put(event.getPath(), events);
		} catch (Throwable e) {
			logger.error("addWatchedEvent error", e);

		} finally {
			lock.unlock();
		}
	}

	public void clearAllWatchedEvent() {
		watchedEvents.clear();
	}

	public int size() {
		return watchedEvents.size();
	}

	public Map<String, Set<BerainWatchedEvent>> getWatchedEvents() {
		return watchedEvents;
	}

}