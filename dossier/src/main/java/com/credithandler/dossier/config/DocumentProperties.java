package com.credithandler.dossier.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "dossier.documents")
public class DocumentProperties {

    private String storagePath;
    private String pdfContentType;
    private String fileExtension;
    private String contractBaseName;
    private String paymentScheduleBaseName;
    private String individualConditionsBaseName;
    private FontProperties font;

    @Getter
    @Setter
    public static class FontProperties {

        private String windowsArialPath;
        private String linuxDejavuPath;
        private String linuxDejavuAltPath;
    }
}
