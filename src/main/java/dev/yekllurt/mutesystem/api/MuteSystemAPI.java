package dev.yekllurt.mutesystem.api;

import dev.yekllurt.mutesystem.TimeUtility;
import dev.yekllurt.mutesystem.core.database.SQLDatabase;
import dev.yekllurt.mutesystem.core.database.SQLQueries;
import dev.yekllurt.mutesystem.core.database.SQLResult;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class MuteSystemAPI {

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

    private final JavaPlugin javaPlugin;
    private final SQLDatabase sqlDatabase;

    private final long cacheTime;

    private final boolean automaticCacheClearing;
    private final long automaticCacheClearingInterval;

    private Integer schedulerId;
    private Map<UUID, MuteEntry> muteEntryCache = new HashMap<>();
    private Map<UUID, Long> muteEntryCacheUpdate = new HashMap<>();

    public MuteSystemAPI(JavaPlugin javaPlugin, SQLDatabase sqlDatabase, long cacheTime, boolean automaticCacheClearing, long automaticCacheClearingInterval) {
        this.javaPlugin = javaPlugin;
        this.sqlDatabase = sqlDatabase;
        this.cacheTime = cacheTime * 1000L;
        this.automaticCacheClearing = automaticCacheClearing;
        this.automaticCacheClearingInterval = automaticCacheClearingInterval * 20;
    }

    public void startAutomaticCacheClearing() {
        if (!this.automaticCacheClearing || this.schedulerId != null) {
            return;
        }
        this.schedulerId = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this.javaPlugin, () -> {
            List<UUID> uuids = new ArrayList<>(this.muteEntryCacheUpdate.keySet());
            for (int index = 0; index < uuids.size(); index++) {
                if (!isCacheDataValid(uuids.get(index))) {
                    this.muteEntryCacheUpdate.remove(uuids.get(index));
                    this.muteEntryCache.remove(uuids.get(index));
                }
            }
        }, 20 * 3L, this.automaticCacheClearingInterval);
    }

    public void stopAutomaticCacheClearing() {
        if (schedulerId == null) return;
        Bukkit.getScheduler().cancelTask(schedulerId);
    }

    public void mute(UUID target, String executor, String reason) {
        MuteEntry muteEntry = new MuteEntry(null, target, executor, reason, TimeUtility.getCurrentDate());
        sqlDatabase.executeAsyncUpdate(SQLQueries.INSERT_PERMANENT,
                new Object[]{target.toString(), executor, reason, muteEntry.getStartAsDate()}, (result) -> {
                });
        muteEntryCache.put(target, muteEntry);
        muteEntryCacheUpdate.putIfAbsent(target, System.currentTimeMillis());
    }

    public void mute(UUID target, String executor, String reason, Date end) {
        MuteEntry muteEntry = new MuteEntry(null, target, executor, reason, TimeUtility.getCurrentDate(), end);
        sqlDatabase.executeAsyncUpdate(SQLQueries.INSERT_TEMPORARY,
                new Object[]{target.toString(), executor, reason, muteEntry.getStartAsDate(), muteEntry.getEndAsDate()},
                (result) -> {
                });
        muteEntryCache.put(target, muteEntry);
        muteEntryCacheUpdate.putIfAbsent(target, System.currentTimeMillis());
    }

    public void unmute(UUID target, String executor) {
        MuteEntry muteEntry = new MuteEntry(null, target, executor, TimeUtility.getCurrentDate());
        sqlDatabase.executeAsyncUpdate(SQLQueries.INSERT_UNMUTE,
                new Object[]{target.toString(), executor, muteEntry.getStartAsDate()}, (result) -> {
                });
        muteEntryCache.put(target, muteEntry);
        muteEntryCacheUpdate.putIfAbsent(target, System.currentTimeMillis());
    }

    public void isMuted(UUID uuid, Consumer<Boolean> result) {
        EXECUTOR_SERVICE.execute(() -> result.accept(isMuted(uuid)));
    }

    public boolean isMuted(UUID uuid) {
        return isMuted(getMuteEntry(uuid));
    }

    public void getMuteEntry(UUID uuid, Consumer<MuteEntry> result) {
        EXECUTOR_SERVICE.execute(() -> result.accept(getMuteEntry(uuid)));
    }

    public MuteEntry getMuteEntry(UUID uuid) {
        if (isCacheDataValid(uuid)) {
            return this.muteEntryCache.get(uuid);
        }
        this.muteEntryCache.put(uuid, fetchNewestMuteEntry(uuid));
        this.muteEntryCacheUpdate.put(uuid, System.currentTimeMillis());
        return this.muteEntryCache.get(uuid);
    }

    public void getAllMuteEntries(UUID uuid, Consumer<List<MuteEntry>> result) {
        fetchAllMuteEntries(uuid, result);
    }

    public void getAllMuteEntries(UUID uuid, int limit, Consumer<List<MuteEntry>> result) {
        fetchAllMuteEntries(uuid, limit, result);
    }

    public List<MuteEntry> getAllMuteEntries(UUID uuid) {
        return fetchAllMuteEntries(uuid);
    }

    public List<MuteEntry> getAllMuteEntries(UUID uuid, int limit) {
        return fetchAllMuteEntries(uuid, limit);
    }

    public boolean isMuted(MuteEntry muteEntry) {
        if (muteEntry == null) return false;
        if (muteEntry.getType() == MuteEntryType.UNMUTE) return false;
        if (muteEntry.getType() == MuteEntryType.MUTE) return true;
        if (muteEntry.getType() == MuteEntryType.TEMP_MUTE && TimeUtility.getCurrentDate().before(muteEntry.getEndAsDate()))
            return true;
        return false;
    }

    private boolean isCacheDataValid(UUID uuid) {
        return this.muteEntryCacheUpdate.containsKey(uuid) && this.muteEntryCache.containsKey(uuid)
                && System.currentTimeMillis() - this.muteEntryCacheUpdate.get(uuid) < this.cacheTime;
    }

    private MuteEntry fetchNewestMuteEntry(UUID uuid) {
        MuteEntry muteEntry = null;
        SQLResult sqlResult = sqlDatabase.executeQuery(SQLQueries.GET_LATEST, new Object[]{uuid.toString()});
        if (!sqlResult.isEmpty()) {
            int id = sqlResult.getInt("id");
            MuteEntryType type = MuteEntryType.fromDatabaseName(sqlResult.getString("type"));
            UUID target = UUID.fromString(sqlResult.getString("target"));
            String executor = sqlResult.getString("executor");
            if (type == MuteEntryType.UNMUTE) {
                Date start = sqlResult.getDate("start");
                muteEntry = new MuteEntry(id, target, executor, start);
            }
            if (type == MuteEntryType.MUTE) {
                String reason = sqlResult.getString("reason");
                Date start = sqlResult.getDate("start");
                muteEntry = new MuteEntry(id, target, executor, reason, start);
            }
            if (type == MuteEntryType.TEMP_MUTE) {
                String reason = sqlResult.getString("reason");
                Date start = sqlResult.getDate("start");
                Date end = sqlResult.getDate("end");
                muteEntry = new MuteEntry(id, target, executor, reason, start, end);
            }
        }
        return muteEntry;
    }

    private void fetchAllMuteEntries(UUID uuid, Consumer<List<MuteEntry>> muteEntryList) {
        EXECUTOR_SERVICE.execute(() -> muteEntryList.accept(fetchAllMuteEntries(uuid)));
    }

    private void fetchAllMuteEntries(UUID uuid, int limit, Consumer<List<MuteEntry>> muteEntryList) {
        EXECUTOR_SERVICE.execute(() -> muteEntryList.accept(fetchAllMuteEntries(uuid, limit)));
    }

    private List<MuteEntry> fetchAllMuteEntries(UUID uuid) {
        SQLResult sqlResult = sqlDatabase.executeQuery(SQLQueries.GET_ALL, new Object[]{uuid.toString()});
        List<MuteEntry> muteEntryList = new ArrayList<>();
        if (!sqlResult.isEmpty()) {
            do {
                int id = sqlResult.getInt("id");
                MuteEntryType type = MuteEntryType.fromDatabaseName(sqlResult.getString("type"));
                UUID target = UUID.fromString(sqlResult.getString("target"));
                String executor = sqlResult.getString("executor");
                if (type == MuteEntryType.UNMUTE) {
                    Date start = sqlResult.getDate("start");
                    muteEntryList.add(new MuteEntry(id, target, executor, start));
                }
                if (type == MuteEntryType.MUTE) {
                    String reason = sqlResult.getString("reason");
                    Date start = sqlResult.getDate("start");
                    muteEntryList.add(new MuteEntry(id, target, executor, reason, start));
                }
                if (type == MuteEntryType.TEMP_MUTE) {
                    String reason = sqlResult.getString("reason");
                    Date start = sqlResult.getDate("start");
                    Date end = sqlResult.getDate("end");
                    muteEntryList.add(new MuteEntry(id, target, executor, reason, start, end));
                }
            } while (sqlResult.next());
        }
        return muteEntryList;
    }

    private List<MuteEntry> fetchAllMuteEntries(UUID uuid, int limit) {
        SQLResult sqlResult = sqlDatabase.executeQuery(SQLQueries.GET_ALL_LIMIT, new Object[]{uuid.toString(), limit});
        List<MuteEntry> muteEntryList = new ArrayList<>();
        if (!sqlResult.isEmpty()) {
            do {
                int id = sqlResult.getInt("id");
                MuteEntryType type = MuteEntryType.fromDatabaseName(sqlResult.getString("type"));
                UUID target = UUID.fromString(sqlResult.getString("target"));
                String executor = sqlResult.getString("executor");
                if (type == MuteEntryType.UNMUTE) {
                    Date start = sqlResult.getDate("start");
                    muteEntryList.add(new MuteEntry(id, target, executor, start));
                }
                if (type == MuteEntryType.MUTE) {
                    String reason = sqlResult.getString("reason");
                    Date start = sqlResult.getDate("start");
                    muteEntryList.add(new MuteEntry(id, target, executor, reason, start));
                }
                if (type == MuteEntryType.TEMP_MUTE) {
                    String reason = sqlResult.getString("reason");
                    Date start = sqlResult.getDate("start");
                    Date end = sqlResult.getDate("end");
                    muteEntryList.add(new MuteEntry(id, target, executor, reason, start, end));
                }
            } while (sqlResult.next());
        }
        return muteEntryList;
    }

}
