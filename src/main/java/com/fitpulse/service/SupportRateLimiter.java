package com.fitpulse.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.HexFormat;

@Service
public class SupportRateLimiter {
    private final JdbcTemplate jdbc;
    public SupportRateLimiter(JdbcTemplate jdbc){this.jdbc=jdbc;}

    /** Commit failed attempts too. Row locking makes the limit atomic across application instances. */
    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public boolean allow(String client) {
        String key;
        try { key=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(client.getBytes(StandardCharsets.UTF_8))); }
        catch(NoSuchAlgorithmException e){throw new IllegalStateException(e);}
        jdbc.update("DELETE FROM support_rate_limits WHERE window_start < DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY)");
        jdbc.update("INSERT IGNORE INTO support_rate_limits(client_key,window_start,attempts) VALUES (?,CURRENT_TIMESTAMP,0)",key);
        jdbc.queryForObject("SELECT attempts FROM support_rate_limits WHERE client_key=? FOR UPDATE",Integer.class,key);
        jdbc.update("UPDATE support_rate_limits SET attempts=IF(window_start < DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 15 MINUTE),1,attempts+1), window_start=IF(window_start < DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 15 MINUTE),CURRENT_TIMESTAMP,window_start) WHERE client_key=?",key);
        return jdbc.queryForObject("SELECT attempts FROM support_rate_limits WHERE client_key=?",Integer.class,key)<=5;
    }
}
