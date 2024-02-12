package ru.curs.celesta.spring.boot.autoconfigure;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import ru.curs.celesta.BaseAppSettings;
import ru.curs.celesta.Celesta;
import ru.curs.celesta.ConnectionPool;
import ru.curs.celesta.ConnectionPoolConfiguration;
import ru.curs.celesta.DBType;
import ru.curs.celesta.DatasourceConnectionPool;
import ru.curs.celesta.InternalConnectionPool;
import ru.curs.celesta.transaction.CelestaTransactionAspect;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

/**
 * {@link org.springframework.boot.autoconfigure.EnableAutoConfiguration Auto-configuration} for Celesta support.
 *
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties(CelestaProperties.class)
@EnableAspectJAutoProxy
public class CelestaAutoConfiguration {

    @Autowired
    private ResourceLoader resourceLoader;

    /**
     *
     * @param celestaProperties Configuration properties
     * @param dataSourceObjectProvider Provider for {@link DataSource}
     * @return Configured connection pool for Celesta
     */
    @Bean
    ConnectionPool connectionPool(CelestaProperties celestaProperties,
                                  ObjectProvider<DataSource> dataSourceObjectProvider) {
        DataSource dataSource = dataSourceObjectProvider.getIfUnique();
        if (dataSource == null) {
            ConnectionPoolConfiguration cpc = new ConnectionPoolConfiguration();
            CelestaProperties.JdbcProperties jdbc = celestaProperties.getJdbc();
            if (jdbc == null) {
                cpc.setJdbcConnectionUrl(BaseAppSettings.H2_IN_MEMORY_URL);
                cpc.setLogin("");
                cpc.setPassword("");
            } else {
                cpc.setJdbcConnectionUrl(jdbc.getUrl());
                cpc.setLogin(jdbc.getUsername());
                cpc.setPassword(jdbc.getPassword());
            }
            return InternalConnectionPool.create(cpc);
        } else {
            return new DatasourceConnectionPool(dataSource);
        }
    }

    /**
     * @param celestaProperties configuration properties
     * @param connectionPool connection pool
     * @return Configured Celesta
     * @throws IOException if score path is unavailable
     * @since 1.0.0
     * <p>
     * Creates Celesta bean
     */
    @Bean
    @ConditionalOnMissingBean
    public Celesta celesta(CelestaProperties celestaProperties, ConnectionPool connectionPool) throws IOException {
        CelestaProperties.JdbcProperties jdbc = celestaProperties.getJdbc();
        CelestaProperties.H2Properties h2 = celestaProperties.getH2();

        PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();

        Properties properties = new Properties();

        String scorePath = chooseScorePath(celestaProperties.getScorePath());
        if (scorePath != null) {
            map.from(scorePath::toString).to(x -> properties.put("score.path", x));
        }

        if (jdbc != null) {
            map.from(jdbc::getUrl)
                    .to(x -> properties.put("rdbms.connection.url", String.valueOf(x)));
            map.from(jdbc::getUsername)
                    .to(x -> properties.put("rdbms.connection.username", String.valueOf(x)));
            map.from(jdbc::getPassword)
                    .to(x -> properties.put("rdbms.connection.password", String.valueOf(x)));
        }
        if (h2 != null) {
            map.from(h2::isInMemory)
                    .to(x -> properties.put("h2.in-memory", String.valueOf(x)));
            map.from(h2::isReferentialIntegrity)
                    .to(x -> properties.put("h2.referential.integrity", String.valueOf(x)));
            map.from(h2::getPort)
                    .to(x -> properties.put("h2.port", String.valueOf(x)));
        }

        map.from(celestaProperties::getCelestaScan)
                .to(x -> properties.put("celestaScan", String.join(",", x)));
        map.from(celestaProperties::isSkipDbUpdate)
                .to(x -> properties.put("skip.dbupdate", String.valueOf(x)));
        map.from(celestaProperties::isForceDbInitialize)
                .to(x -> properties.put("force.dbinitialize", String.valueOf(x)));

        return Celesta.createInstance(properties, connectionPool);
    }

    private String chooseScorePath(String celestaScorePath) throws IOException {
        if (celestaScorePath == null) {
            return null;
        }

        List<String> scorePaths = new ArrayList<>();

        for (String cs : celestaScorePath.split(",")) {
            Resource[] scoreResources = new PathMatchingResourcePatternResolver(resourceLoader)
                    .getResources(cs.trim());
            Arrays.stream(scoreResources)
                    .map(this::getFileFromResource)
                    .filter(Objects::nonNull)
                    .map(File::getAbsolutePath)
                    .forEach(scorePaths::add);
        }

        return String.join(File.pathSeparator, scorePaths);
    }

    private File getFileFromResource(Resource resource) {
        if (!resource.isFile()) {
            return null;
        }

        try {
            return resource.getFile();
        } catch (IOException ex) {
            throw new RuntimeException(ex); // This should never happen though
        }
    }

    /**
     * Provides an aspect for wrapping Celesta-transactional methods.
     *
     * @param celesta Configured Celesta.
     * @return Aspect for wrapping Celesta-transactional methods.
     */
    @Bean
    @ConditionalOnBean(Celesta.class)
    public CelestaTransactionAspect celestaTransactionAspect(Celesta celesta) {
        return new CelestaTransactionAspect(celesta);
    }

}
