package ru.curs.celesta.spring.boot.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import ru.curs.celesta.java.Celesta;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;


public class CelestaAutoConfigurationTest {

    private static final String SCORE_PATH = "src/test/resources/score";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CelestaAutoConfiguration.class))
            .withPropertyValues("celesta.scorePath:" + SCORE_PATH);

    @Test
    void testBeanRegistration() {
        this.contextRunner
                .withPropertyValues("celesta.h2.inMemory:true")
                .run((context -> {
                    Celesta celesta = context.getBean(Celesta.class);

                    //Celesta bean is registered
                    assertNotNull(context.getBean(Celesta.class));

                    //Properties mapped correctly
                    Properties p = celesta.getSetupProperties();

                    assertAll(
                            () -> assertEquals(6, p.size()),
                            () -> assertEquals("true", p.getProperty("h2.in-memory")),
                            () -> assertEquals("false", p.getProperty("h2.referential.integrity")),
                            () -> assertEquals(SCORE_PATH, p.getProperty("score.path")),
                            () -> assertEquals("false", p.getProperty("skip.dbupdate")),
                            () -> assertEquals("false", p.getProperty("force.dbinitialize")),
                            () -> assertEquals("false", p.getProperty("log.logins"))
                    );

                    celesta.callContext().getConn().createStatement().execute("SHUTDOWN");
                    celesta.close();
                }));
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
                    Celesta celesta = context.getBean(Celesta.class);

                    //Celesta bean is registered
                    assertNotNull(context.getBean(Celesta.class));

                    //Properties mapped correctly
                    Properties p = celesta.getSetupProperties();

                    assertAll(
                            () -> assertEquals(11, p.size()),
                            () -> assertEquals(
                                    "jdbc:h2:mem:celesta;DB_CLOSE_DELAY=-1",
                                    p.getProperty("rdbms.connection.url")
                            ),
                            () -> assertEquals("username", p.getProperty("rdbms.connection.username")),
                            () -> assertEquals("password", p.getProperty("rdbms.connection.password")),
                            () -> assertEquals("false", p.getProperty("h2.in-memory")),
                            () -> assertEquals("true", p.getProperty("h2.referential.integrity")),
                            () -> assertEquals("1234", p.getProperty("h2.port")),
                            () -> assertEquals(SCORE_PATH, p.getProperty("score.path")),
                            () -> assertEquals("true", p.getProperty("skip.dbupdate")),
                            () -> assertEquals("true", p.getProperty("force.dbinitialize")),
                            () -> assertEquals("true", p.getProperty("log.logins")),
                            () -> assertEquals(
                                    "ru.curs.celesta.spring.boot,ru.curs.celesta.spring",
                                    p.getProperty("celestaScan")
                            )
                    );

                    celesta.callContext().getConn().createStatement().execute("SHUTDOWN");
                    celesta.close();
                }));
    }

}
