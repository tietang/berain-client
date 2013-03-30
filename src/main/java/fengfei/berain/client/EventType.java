package fengfei.berain.client;

public enum EventType {
	None(-1),
	Created(1),
	Deleted(2),
	DataChanged(3),
	ChildrenChanged(4);

	private final int intValue; // Integer representation of value
								// for sending over wire

	EventType(int intValue) {
		this.intValue = intValue;
	}

	public int getIntValue() {
		return intValue;
	}

	public static EventType fromInt(int intValue) {
		switch (intValue) {
		case -1:
			return EventType.None;
		case 1:
			return EventType.Created;
		case 2:
			return EventType.Deleted;
		case 3:
			return EventType.DataChanged;
		case 4:
			return EventType.ChildrenChanged;
		default:
			throw new RuntimeException("Invalid integer value for conversion to EventType");
		}
	}
}