package ru.curs.celesta.spring.boot.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.curs.celesta.java.Celesta;

import java.util.Properties;

/**
 * {@link org.springframework.boot.autoconfigure.EnableAutoConfiguration Auto-configuration} for Celesta support.
 *
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties(CelestaProperties.class)
    public class CelestaAutoConfiguration {

    /**
     * @since  1.0.0
     *
     * Creates Celesta bean
     * @param celestaProperties configuration properties
     * @return Configured Celesta
     */
    @Bean
    @ConditionalOnMissingBean
    public Celesta celesta(CelestaProperties celestaProperties) {
        CelestaProperties.JdbcProperties jdbc = celestaProperties.getJdbc();
        CelestaProperties.H2Properties h2 = celestaProperties.getH2();

        PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();

        Properties properties = new Properties();

        map.from(celestaProperties::getScorePath).to(x -> properties.put("score.path", String.valueOf(x)));

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
        map.from(celestaProperties::isLogLogins)
                .to(x -> properties.put("log.logins", String.valueOf(x)));

        return Celesta.createInstance(properties);
    }

}
