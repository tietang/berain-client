package fengfei.berain.client;

import java.util.List;
import java.util.Map;

public interface BerainClient {

	void start();

	/**
	 * if has password
	 */
	void login(String username, String password) throws Exception;

	// --------------------------write-----------------------------//
	boolean update(String path, String value) throws Exception;

	boolean create(String path, String value) throws Exception;

	boolean delete(String path) throws Exception;

	boolean copy(String originalPath, String newPath) throws Exception;

	// --------------------------read-----------------------------//
	List<BerainEntry> nextChildren(String parentPath) throws Exception;

	String get(String path) throws Exception;

	BerainEntry getFull(String path) throws Exception;

	boolean exists(String path) throws Exception;

	// --------------------------Event-----------------------------//
	public void addChildrenChangedWatcher(final String path, final Wather wather)
			throws Exception;

	public void addNodeChangedWatcher(final String path, final Wather wather)
			throws Exception;


	void addWatchable(String path, EventType type, Wather wather) throws Exception;

	void removeWatchable(String path, int type) throws Exception;

	Map<String, List<BerainWatchedEvent>> listChangedNodes() throws Exception;

	void removeAllListener() throws Exception;
}