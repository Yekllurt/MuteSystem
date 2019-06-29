package dev.yekllurt.mutesystem.api;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class MuteEntry {

    private Integer id;
    private MuteEntryType type;
    private UUID target;
    private String executor;
    private String reason;
    private Date start;
    private Date end;

    public MuteEntry(Integer id, UUID target, String executor, Date start) {
        this(id, MuteEntryType.UNMUTE, target, executor, null, start, null);
    }

    public MuteEntry(Integer id, UUID target, String executor, String reason, Date start) {
        this(id, MuteEntryType.MUTE, target, executor, reason, start, null);
    }

    public MuteEntry(Integer id, UUID target, String executor, String reason, Date start, Date end) {
        this(id, MuteEntryType.TEMP_MUTE, target, executor, reason, start, end);
    }

    public MuteEntry(Integer id, MuteEntryType type, UUID target, String executor, String reason, Date start, Date end) {
        this.id = id;
        this.type = type;
        this.target = target;
        this.executor = executor;
        this.reason = reason;
        this.start = start;
        this.end = end;
    }

    public Integer getId() {
        return this.id;
    }

    public MuteEntryType getType() {
        return type;
    }

    public UUID getTarget() {
        return this.target;
    }

    public String getExecutor() {
        return this.executor;
    }

    public String getReason() {
        return this.reason;
    }

    public Date getStartAsDate() {
        return this.start;
    }

    public Date getEndAsDate() {
        return this.end;
    }

    public Calendar getStartAsCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(getStartAsDate());
        return calendar;
    }

    public Calendar getEndAsCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(getEndAsDate());
        return calendar;
    }

}
