package de.uni_leipzig.wcmprak.books.wcmbookserver.serve;

import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.utils.Props;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Properties;

/**
 * Created by Erik on 22.04.2015.
 */
public class DataCache {
    private final static Logger log = LoggerFactory.getLogger(DataCache.class);

    private static DataCache instance;
    private static Props props = null;

    private Hashtable<String, Object> data = new Hashtable<>();
    private final static long TTL = 24 * 60 * 60 * 1000;
    private Hashtable<String, Long> dataTTL = new Hashtable<>();

    private DataCache() {
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
    public static DataCache getInstance() {
        return instance;
    }

    /**
     * Set configuration for data caching.
     *
     * @param props standard java properties
     */
    public static void configureWith(Properties props) {
        if (props == null) {
            DataCache.props = new Props();
        } else {
            if (props instanceof Props) {
                DataCache.props = (Props) props;
            } else {
                DataCache.props = new Props(props);
            } // if-else
        } // if-else
    }

    /**
     * Initialize instance for data caching.
     *
     * @throws Exception
     */
    public static void initialize() throws Exception {
        // Create new instance object
        instance = new DataCache();
    }
}
