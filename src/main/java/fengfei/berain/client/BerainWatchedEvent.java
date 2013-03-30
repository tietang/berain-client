package fengfei.berain.client;

public class BerainWatchedEvent {

	protected EventType eventType;
	protected String path;
	protected Wather wather;

	public BerainWatchedEvent() {
	}

	public BerainWatchedEvent(EventType eventType, String path, Wather wather) {

		this.eventType = eventType;
		this.path = path;
		this.wather = wather;
	}

	public BerainWatchedEvent(int eventType, String path, Wather wather) {
		this(EventType.fromInt(eventType), path, wather);
	}

	public BerainWatchedEvent(EventType eventType, String path) {
		this(eventType, path, null);
	}

	public BerainWatchedEvent(int eventType, String path) {
		this(eventType, path, null);
	}

	public Wather getWather() {
		return wather;
	}

	public void setWather(Wather wather) {
		this.wather = wather;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public EventType getEventType() {
		return eventType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((eventType == null) ? 0 : eventType.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BerainWatchedEvent other = (BerainWatchedEvent) obj;
		if (eventType != other.eventType)
			return false;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "WatchedEvent state:" + eventType + " path:" + path;
	}

}
