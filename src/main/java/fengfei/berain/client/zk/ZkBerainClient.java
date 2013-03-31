package fengfei.berain.client.zk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.zookeeper.WatchedEvent;

import com.netflix.curator.framework.CuratorFramework;
import com.netflix.curator.framework.CuratorFrameworkFactory;
import com.netflix.curator.framework.api.CuratorWatcher;
import com.netflix.curator.framework.imps.CuratorFrameworkState;
import com.netflix.curator.framework.recipes.locks.InterProcessMutex;
import com.netflix.curator.retry.RetryNTimes;

import fengfei.berain.client.BerainClient;
import fengfei.berain.client.BerainEntry;
import fengfei.berain.client.BerainWatchedEvent;
import fengfei.berain.client.EventType;
import fengfei.berain.client.Wather;

public class ZkBerainClient implements BerainClient {

	public static final String ROOT_PATH = "/";
	public static final String SEPARATOR = "/";
	private CuratorFramework client;

	public ZkBerainClient(Properties properties) {
		String host = properties.getProperty("zk.host");
		String namespace = properties.getProperty("zk.namespace");
		initNamespace();
		int timeout = 5000;
		int retryTimes = Integer.MAX_VALUE;
		int sleepRetry = 1000;
		String stimeout = properties.getProperty("zk.connectionTimeoutMs");
		String sretryTimes = properties.getProperty("zk.RetryNTimes");
		String ssleepRetry = properties.getProperty("zk.sleepMsBetweenRetries");
		if (null == namespace || "".equals(namespace)) {
			namespace = "forest";
		}
		if (null != stimeout && !"".equals(stimeout)) {
			timeout = Integer.parseInt(stimeout);
		}
		if (null != sretryTimes && !"".equals(sretryTimes)) {
			retryTimes = Integer.parseInt(sretryTimes);
		}
		if (null != ssleepRetry && !"".equals(ssleepRetry)) {
			sleepRetry = Integer.parseInt(ssleepRetry);
		}
		client = CuratorFrameworkFactory
				.builder()
				.connectString(host)
				.namespace(namespace)
				.retryPolicy(new RetryNTimes(retryTimes, sleepRetry))
				.connectionTimeoutMs(timeout)
				.build();
		initNamespace();
	}

	public ZkBerainClient(CuratorFramework client) {
		super();
		this.client = client;
		initNamespace();
	}

	public CuratorFramework getClient() {
		return client;
	}

	public CuratorFramework getCuratorFramework() {
		return client;
	}

	public void initNamespace() {
	}

	public boolean exists(String path) throws Exception {
		return client.checkExists().forPath(path) != null;
	}

	@Override
	public void start() {
		if (client.getState() != CuratorFrameworkState.STARTED) {
			client.start();
		}
	}

	@Override
	public void stop() {
		if (client.getState() != CuratorFrameworkState.STOPPED) {
			client.close();
		}
	}

	@Override
	public void login(String username, String password) throws Exception {
	}

	@Override
	public boolean update(String path, String value) throws Exception {
		client.inTransaction().setData().forPath(path, value.getBytes()).and().commit();
		return true;
	}

	@Override
	public boolean create(String path, String value) throws Exception {
		client.inTransaction().create().forPath(path, value.getBytes()).and().commit();
		return true;
	}

	@Override
	public boolean delete(String path) throws Exception {
		client.inTransaction().delete().forPath(path).and().commit();
		return true;
	}

	public static final String LOCKPATH = "/copylock";
	public static final long MAXWAIT = 60;
	public static final TimeUnit WAITUNIT = TimeUnit.SECONDS;

	public static void main(String[] args) throws Exception {
		CuratorFramework framework = CuratorFrameworkFactory
				.builder()
				.connectString("127.0.0.1")
				.namespace("tx")
				.retryPolicy(new RetryNTimes(2, 3000))
				.connectionTimeoutMs(60000)
				.build();
		ZkBerainClient client = new ZkBerainClient(framework);
		client.start();
		String ppath = "/e1";
		client.create(ppath, "x1");
		for (int i = 0; i < 3; i++) {
			String path = ppath + "/f" + i;
			client.create(path, "x1" + i);
			for (int j = 0; j < 4; j++) {
				String cpath = path + "/h" + j;
				client.create(cpath, "x1" + i + j);
			}
		}
		System.out.println(client.nextChildren(ppath));
		client.create("c1", "x1");
		System.out.println(client.nextChildren("c1"));
		client.copy(ppath, "/a1");
		client.stop();
	}

	@Override
	public boolean copy(String originalPath, String newPath) throws Exception {
		InterProcessMutex lock = new InterProcessMutex(client, LOCKPATH);
		if (lock.acquire(MAXWAIT, WAITUNIT)) {
			try {
				String value = get(originalPath);
				create(newPath, value);
				copyChildren(originalPath, newPath);
			} finally {
				lock.release();
			}
		}
		return true;
	}

	private void copyChildren(String originalPath, String newPath) throws Exception {
		List<BerainEntry> children = nextChildren(originalPath);
		if (children == null || children.isEmpty()) {
			return;
		}
		for (BerainEntry entry : children) {
			String childPath = entry.getPath();
			String newChildPath = childPath.replace(originalPath, newPath);
			create(newChildPath, entry.getValue());
			copyChildren(childPath, newChildPath);
		}
	}

	public static String getKey(String path) {
		String[] ps = path.split("/");
		return ps[ps.length - 1];
	}

	@Override
	public List<BerainEntry> nextChildren(String parentPath) throws Exception {
		List<BerainEntry> models = new ArrayList<>();
		if (client.checkExists().forPath(parentPath) != null) {
			List<String> paths = client.getChildren().forPath(parentPath);
			for (String cpath : paths) {
				String ppath = parentPath + SEPARATOR + cpath;
				byte[] data = client.getData().forPath(ppath);
				BerainEntry model = new BerainEntry();
				model.key = getKey(cpath);
				model.path = ppath;
				model.value = new String(data);
				models.add(model);
			}
		}
		return models;
	}

	@Override
	public String get(String path) throws Exception {
		byte[] data = client.getData().forPath(path);
		return new String(data);
	}

	@Override
	public BerainEntry getFull(String path) throws Exception {
		String key = getKey(path);
		byte[] data = client.getData().forPath(path);
		BerainEntry model = new BerainEntry();
		model.key = key;
		model.path = path;
		model.value = new String(data);
		return model;
	}

	public void addChildrenChangedWatcher(final String path, final Wather wather)
			throws Exception {
		CuratorWatcher watcher = new CuratorWatcher() {

			@Override
			public void process(WatchedEvent event) throws Exception {
				System.out.println("xx: " + event);
				ZkBerainWatchedEvent watchedEvent = new ZkBerainWatchedEvent(event);
				wather.call(watchedEvent);
				client.getChildren().usingWatcher(this).forPath(path);
				// addChildrenChangedWatcher(path);
			}
		};
		client.getChildren().usingWatcher(watcher).forPath(path);
	}

	public void addNodeChangedWatcher(final String path, final Wather wather)
			throws Exception {
		CuratorWatcher watcher = new CuratorWatcher() {

			@Override
			public void process(WatchedEvent event) throws Exception {
				System.out.println("xx: " + event);
				ZkBerainWatchedEvent watchedEvent = new ZkBerainWatchedEvent(event);
				wather.call(watchedEvent);
				client.getData().usingWatcher(this).forPath(path);
				// addDataChangedWatcher(path);
			}
		};
		client.getData().usingWatcher(watcher).forPath(path);
	}

	@Deprecated
	@Override
	public void removeWatchable(String path, int type) throws Exception {
		throw new UnsupportedOperationException("don't implemtments.");
	}

	@Deprecated
	@Override
	public void addWatchable(String path, EventType type, Wather wather) throws Exception {
		throw new UnsupportedOperationException("don't implemtments.");
	}

	@Deprecated
	@Override
	public Map<String, List<BerainWatchedEvent>> listChangedNodes() throws Exception {
		throw new UnsupportedOperationException("don't implemtments.");
	}

	@Deprecated
	@Override
	public void removeAllListener() throws Exception {
		throw new UnsupportedOperationException("don't implemtments.");
	}
}
