package ru.curs.celesta.transaction;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.context.annotation.UserConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import ru.curs.celesta.CallContext;
import ru.curs.celesta.Celesta;
import ru.curs.celesta.SystemCallContext;
import ru.curs.celesta.spring.boot.autoconfigure.CelestaAutoConfiguration;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CelestaTransactionTest {
    private static final String SCORE_PATH = "classpath:score";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CelestaAutoConfiguration.class))
            .withPropertyValues("celesta.scorePath:" + SCORE_PATH);


    @Test
    void activatesContext() {
        this.contextRunner
                .withConfiguration(UserConfigurations.of(DummyService.class))
                .withPropertyValues("celesta.h2.inMemory:true")
                .run((context -> {
                    try (Celesta celesta = context.getBean(Celesta.class)) {
                        DummyService srv = context.getBean(DummyService.class);
                        CallContext cc = new CallContext("foo");
                        assertFalse(cc.isClosed());
                        assertNull(cc.getCelesta());

                        srv.withContext(cc, "bar");

                        assertNotNull(cc.getCelesta());
                        assertSame(celesta, cc.getCelesta());
                        assertTrue(cc.isClosed());

                        shutDownH2(celesta);
                    }
                }));
    }

    @Test
    void handlesMethodWithNoContext() {
        this.contextRunner
                .withConfiguration(UserConfigurations.of(DummyService.class))
                .withPropertyValues("celesta.h2.inMemory:true")
                .run((context -> {
                    try (Celesta celesta = context.getBean(Celesta.class)) {
                        DummyService srv = context.getBean(DummyService.class);
                        srv.noContext("test");
                        shutDownH2(celesta);
                    }
                }));
    }

    @Test
    void handlesErrorInMethod() {
        this.contextRunner
                .withConfiguration(UserConfigurations.of(DummyService.class))
                .withPropertyValues("celesta.h2.inMemory:true")
                .run((context -> {
                    try (Celesta celesta = context.getBean(Celesta.class)) {
                        DummyService srv = context.getBean(DummyService.class);
                        CallContext cc = new SystemCallContext();
                        assertFalse(cc.isClosed());
                        assertEquals(DummyService.ERROR_MESSAGE,
                                assertThrows(SQLException.class,
                                        () -> srv.exception(cc)
                                ).getMessage());
                        assertTrue(cc.isClosed());
                        shutDownH2(celesta);
                    }
                }));
    }

    @Test
    void handlesRollbackErrorInMethod() {
        this.contextRunner
                .withConfiguration(UserConfigurations.of(DummyService.class))
                .withPropertyValues("celesta.h2.inMemory:true")
                .run((context -> {
                    try (Celesta celesta = context.getBean(Celesta.class)) {
                        DummyService srv = context.getBean(DummyService.class);
                        CallContext cc = new SystemCallContext(){
                            @Override
                            public void rollback(){
                                throw new IllegalStateException("no rollback");
                            }
                        };
                        assertFalse(cc.isClosed());
                        assertEquals("no rollback",
                                assertThrows(IllegalStateException.class,
                                        () -> srv.exception(cc)
                                ).getMessage());
                        assertTrue(cc.isClosed());
                        shutDownH2(celesta);
                    }
                }));
    }

    private void shutDownH2(Celesta celesta) throws SQLException {
        celesta.getConnectionPool().get().createStatement().execute("SHUTDOWN");
    }

    @Test
    void handlesCallContextActivationFailure() {
        Celesta celesta = mock(Celesta.class);
        when(celesta.getConnectionPool()).thenThrow(new IllegalStateException("no connections"));
        CallContext ctx = new SystemCallContext();

        ProceedingJoinPoint jp = mock(ProceedingJoinPoint.class);
        Signature signature = mock(Signature.class);
        when(signature.toShortString()).thenReturn("foo");
        when(jp.getArgs()).thenReturn(new Object[]{ctx});
        when(jp.getSignature()).thenReturn(signature);

        CelestaTransactionAspect aspect = new CelestaTransactionAspect(celesta);
        assertEquals("no connections",
                assertThrows(IllegalStateException.class,
                        () -> aspect.execEntryPoint(jp)).getMessage());
    }

}
