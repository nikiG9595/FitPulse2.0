package com.fitpulse.config;

import com.fitpulse.service.GymContent;
import org.springframework.stereotype.Component;

@Component
public class BrowserMedia {
    public boolean local(String path) {
        return path != null && path.startsWith("/") && !path.startsWith("//") && !path.contains("\\");
    }

    public String external(String url) {
        return url != null && !url.isBlank() && GymContent.webUrl(url) ? url.strip() : null;
    }
}
