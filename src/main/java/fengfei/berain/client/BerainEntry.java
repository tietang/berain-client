package fengfei.berain.client;

public class BerainEntry {

	public String key;
	public String value;
	public String path;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public String toString() {
		return "BerainEntry [key=" + key + ", value=" + value + ", path="
				+ path + "]";
	}

	public boolean booleanValue() {
		return "true".equals(value) || "on".equals(value)
				|| "yes".equals(value) || !"0".equals(value);
	}

	public double doubleValue() {
		return Double.parseDouble(value);
	}

	public float floatValue() {
		return Float.parseFloat(value);
	}

	public int intValue() {
		return Integer.parseInt(value);
	}

	public short shortValue() {
		return Short.parseShort(value);
	}

	public byte byteValue() {
		return Byte.parseByte(value);
	}

	public char charValue() {
		return value.charAt(0);
	}

	public long longValue() {
		return Long.parseLong(value);
	}

}
