package de.uni_leipzig.wcmprak.books.wcmbookserver.serve;

import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.utils.Configurable;
import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.utils.Initializable;
import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.utils.Props;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Erik on 22.04.2015.
 */
public class DataCache implements Configurable, Initializable {
    private final static Logger log = LoggerFactory.getLogger(DataCache.class);

    private static DataCache instance;
    private Props props = null;
    private boolean hasBeenInitialized = false;

    private Hashtable<String, Object> data = new Hashtable<>();
    private Hashtable<String, Long> dataTTL = new Hashtable<>();
    private ScheduledExecutorService cleanScheduler = null;
    private long ttl = TTL;

    public final static String PROP_KEY_TTL = "cache.ttl";
    public final static long TTL = 24 * 60 * 60 * 1000;

    private DataCache() {
        cleanScheduler = Executors.newScheduledThreadPool(1);

        cleanScheduler.scheduleAtFixedRate(new Thread(new Runnable() {
            @Override
            public void run() {
                log.debug("scheduled cleanup ...");

                instance.cleanUp();
            }
        }, "DataCache-CleanUp-Thread"), 0, 3, TimeUnit.HOURS);

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    cleanScheduler.shutdownNow();
                    cleanScheduler.awaitTermination(5, TimeUnit.SECONDS);
                } catch (Exception e) {
                    log.error("stop scheduler", e);
                } // try-catch
            }
        }, "shutdownHook-DataCache-Scheduler"));
    }

    public void store(String key, Object value) {
        if (key != null) {
            if (value == null) {
                del(key);
                kill(key);
            } else {
                data.put(key, value);
                updateLive(key);
            }
        }
    }

    public Object get(String key) {
        if (!isAlive(key)) {
            return null;
        }

        log.debug("Cache-Hit! id: {}", key);

        return data.get(key);
    }

    public boolean has(String key) {
        return data.containsKey(key) && isAlive(key);
    }

    public void del(String key) {
        if (has(key)) {
            data.remove(key);
        }

        kill(key);
    }

    private boolean isAlive(String key) {
        if (key == null) {
            return false;
        }

        if (!dataTTL.containsKey(key)) {
            return false;
        }

        // time is gone ...
        if (System.currentTimeMillis() >= dataTTL.get(key)) {
            log.debug("Left for dead ... id: {}", key);
            return false;
        }

        return true;
    }

    private void updateLive(String key) {
        if (key != null) {
            dataTTL.put(key, System.currentTimeMillis() + TTL);
        }
    }

    private void kill(String key) {
        if (key != null) {
            dataTTL.remove(key);
        }
    }

    public void cleanUp() {
        HashSet<String> keys = new HashSet<>(data.keySet());
        keys.addAll(dataTTL.keySet());

        for (String key : keys) {
            if (!isAlive(key)) {
                del(key);
            }
        }
    }

    /**
     * Returns the single shared instance used for data caching.
     *
     * @return {@link DataCache}
     */
    public synchronized static DataCache getInstance() {
        if (instance == null) {
            instance = new DataCache();
        } // if

        return instance;
    }

    /**
     * Set configuration for data caching.
     *
     * @param props standard java properties
     */
    @Override
    public void configureWith(Properties props) {
        if (props == null) {
            this.props = new Props();
        } else {
            if (props instanceof Props) {
                this.props = (Props) props;
            } else {
                this.props = new Props(props);
            } // if-else
        } // if-else

        ttl = this.props.getLongProp(PROP_KEY_TTL, ttl);
    }

    @Override
    public boolean hasBeenInitialized() {
        return hasBeenInitialized;
    }

    /**
     * Initialize instance for data caching.
     *
     * @throws Exception
     */
    @Override
    public void initialize() throws Exception {
        this.hasBeenInitialized = true;
    }

    public void stop() throws Exception {
        cleanScheduler.shutdownNow();
    }
}
