package ru.curs.celesta.spring.boot.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashSet;

/**
 * @since 1.0.0
 *
 * Configuration properties for Celesta
 */
@ConfigurationProperties(prefix = "celesta")
public class CelestaProperties {

    private String scorePath = "classpath:score";
    private JdbcProperties jdbc;
    private H2Properties h2;
    private boolean skipDbUpdate = false;
    private boolean forceDbInitialize = false;
    private boolean logLogins = false;
    private LinkedHashSet<String> celestaScan;

    public final String getScorePath() {
        return scorePath;
    }

    public final void setScorePath(final String scorePath) {
        this.scorePath = scorePath;
    }

    public final JdbcProperties getJdbc() {
        return jdbc;
    }

    public final void setJdbc(JdbcProperties jdbc) {
        this.jdbc = jdbc;
    }

    public final H2Properties getH2() {
        return h2;
    }

    public final void setH2(H2Properties h2) {
        this.h2 = h2;
    }

    public final boolean isSkipDbUpdate() {
        return skipDbUpdate;
    }

    public final void setSkipDbUpdate(boolean skipDbUpdate) {
        this.skipDbUpdate = skipDbUpdate;
    }

    public final boolean isForceDbInitialize() {
        return forceDbInitialize;
    }

    public final void setForceDbInitialize(boolean forceDbInitialize) {
        this.forceDbInitialize = forceDbInitialize;
    }

    public final boolean isLogLogins() {
        return logLogins;
    }

    public final void setLogLogins(boolean logLogins) {
        this.logLogins = logLogins;
    }

    public final LinkedHashSet<String> getCelestaScan() {
        return celestaScan;
    }

    public final void setCelestaScan(LinkedHashSet<String> celestaScan) {
        this.celestaScan = celestaScan;
    }

    /**
     * @since 1.0.0
     *
     * Configuration properties for Celesta jdbc connection
     */
    public static class JdbcProperties {
        private String url;
        private String username;
        private String password;

        public final String getUrl() {
            return url;
        }

        public final void setUrl(String url) {
            this.url = url;
        }

        public final String getUsername() {
            return username;
        }

        public final void setUsername(String username) {
            this.username = username;
        }

        public final String getPassword() {
            return password;
        }

        public final void setPassword(String password) {
            this.password = password;
        }
    }

    /**
     * @since 1.0.0
     *
     * Configuration properties for Celesta H2 db
     */
    public static class H2Properties {
        private boolean inMemory = false;
        private boolean referentialIntegrity = false;
        private Integer port;

        public final boolean isInMemory() {
            return inMemory;
        }

        public final void setInMemory(boolean inMemory) {
            this.inMemory = inMemory;
        }

        public final boolean isReferentialIntegrity() {
            return referentialIntegrity;
        }

        public final void setReferentialIntegrity(boolean referentialIntegrity) {
            this.referentialIntegrity = referentialIntegrity;
        }

        public final Integer getPort() {
            return port;
        }

        public final void setPort(Integer port) {
            this.port = port;
        }
    }

}
