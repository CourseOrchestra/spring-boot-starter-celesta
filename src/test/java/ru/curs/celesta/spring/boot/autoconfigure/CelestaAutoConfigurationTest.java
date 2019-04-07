package ru.curs.celesta.spring.boot.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import ru.curs.celesta.Celesta;
import java.io.File;
import java.sql.SQLException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;


public class CelestaAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CelestaAutoConfiguration.class));

    @Test
    void testBeanRegistration() {
        this.contextRunner
                .withPropertyValues("celesta.h2.inMemory:true")
                .run((context -> {
                    try (Celesta celesta = context.getBean(Celesta.class)) {
                        //Celesta bean is registered
                        assertNotNull(context.getBean(Celesta.class));

                        //Properties mapped correctly
                        Properties p = celesta.getSetupProperties();

                        assertAll(
                                () -> assertEquals(4, p.size()),
                                () -> assertEquals("true", p.getProperty("h2.in-memory")),
                                () -> assertEquals("false", p.getProperty("h2.referential.integrity")),
                                () -> assertEquals("false", p.getProperty("skip.dbupdate")),
                                () -> assertEquals("false", p.getProperty("force.dbinitialize"))
                        );
                        shutDownH2(celesta);
                    }
                }));
    }

    private void shutDownH2(Celesta celesta) throws SQLException {
        celesta.getConnectionPool().get().createStatement().execute("SHUTDOWN");
    }


    @Test
    void testBeanRegistrationWithFullConfiguration() {
        this.contextRunner
                .withPropertyValues("celesta.jdbc.url:jdbc:h2:mem:celesta;DB_CLOSE_DELAY=-1")
                .withPropertyValues("celesta.jdbc.username:username")
                .withPropertyValues("celesta.jdbc.password:password")
                .withPropertyValues("celesta.h2.referentialIntegrity:true")
                .withPropertyValues("celesta.h2.port:1234")
                .withPropertyValues("celesta.skipDbUpdate:true")
                .withPropertyValues("celesta.forceDbInitialize:true")
                .withPropertyValues("celesta.logLogins:true")
                .withPropertyValues("celesta.celestaScan:ru.curs.celesta.spring.boot,ru.curs.celesta.spring")

                .run((context -> {
                    try (Celesta celesta = context.getBean(Celesta.class)) {

                        //Celesta bean is registered
                        assertNotNull(context.getBean(Celesta.class));

                        //Properties mapped correctly
                        Properties p = celesta.getSetupProperties();

                        assertAll(
                                () -> assertEquals(9, p.size()),
                                () -> assertEquals(
                                        "jdbc:h2:mem:celesta;DB_CLOSE_DELAY=-1",
                                        p.getProperty("rdbms.connection.url")
                                ),
                                () -> assertEquals("username", p.getProperty("rdbms.connection.username")),
                                () -> assertEquals("password", p.getProperty("rdbms.connection.password")),
                                () -> assertEquals("false", p.getProperty("h2.in-memory")),
                                () -> assertEquals("true", p.getProperty("h2.referential.integrity")),
                                () -> assertEquals("1234", p.getProperty("h2.port")),
                                () -> assertEquals("true", p.getProperty("skip.dbupdate")),
                                () -> assertEquals("true", p.getProperty("force.dbinitialize")),
                                () -> assertEquals(
                                        "ru.curs.celesta.spring.boot,ru.curs.celesta.spring",
                                        p.getProperty("celestaScan")
                                )
                        );

                        shutDownH2(celesta);
                    }
                }));
    }

    @Test
    void testBeanRegistrationWithScorePath() {

        final String scorePath = "classpath:testScore";
        final String expectedScorePath = new File("target/test-classes/testScore").getAbsolutePath();

        this.contextRunner
                .withPropertyValues("celesta.h2.inMemory:true")
                .withPropertyValues("celesta.scorePath:" + scorePath)
                .run((context -> {
                    try (Celesta celesta = context.getBean(Celesta.class)) {
                        //Celesta bean is registered
                        assertNotNull(context.getBean(Celesta.class));

                        //Properties mapped correctly
                        Properties p = celesta.getSetupProperties();

                        assertAll(
                                () -> assertEquals(expectedScorePath, p.getProperty("score.path"))
                        );
                        shutDownH2(celesta);
                    }
                }));
    }

}
