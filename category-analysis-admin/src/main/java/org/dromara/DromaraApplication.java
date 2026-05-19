package org.dromara;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;

import java.security.Security;

/**
 * 启动程序
 *
 * @author Lion Li
 */

@SpringBootApplication
public class DromaraApplication {

    private static final Logger log = LoggerFactory.getLogger(DromaraApplication.class);

    public static void main(String[] args) {
        enableLegacySqlServerTlsIfNeeded();
        SpringApplication application = new SpringApplication(DromaraApplication.class);
        application.setApplicationStartup(new BufferingApplicationStartup(2048));
        application.run(args);
        System.out.println("(♥◠‿◠)ﾉﾞ  category-analysis启动成功   ლ(´ڡ`ლ)ﾞ");
    }

    private static void enableLegacySqlServerTlsIfNeeded() {
        String enabled = System.getenv("DIAG_SQLSERVER_ENABLE_LEGACY_TLS10");
        if (!"true".equalsIgnoreCase(enabled)) {
            return;
        }
        Security.setProperty(
            "jdk.tls.disabledAlgorithms",
            "SSLv3, RC4, DES, MD5withRSA, DH keySize < 1024, EC keySize < 224, anon, NULL, ECDH"
        );
        System.setProperty("jdk.tls.client.protocols", "TLSv1,TLSv1.1,TLSv1.2");
        System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
        log.warn("Legacy TLS 1.0 support enabled for SQL Server connectivity via env DIAG_SQLSERVER_ENABLE_LEGACY_TLS10=true");
    }

}
