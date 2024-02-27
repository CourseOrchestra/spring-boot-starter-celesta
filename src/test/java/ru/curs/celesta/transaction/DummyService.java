package ru.curs.celesta.transaction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import ru.curs.celesta.CallContext;
import ru.curs.celesta.DBType;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@Service
public class DummyService {

    public static final String ERROR_MESSAGE = "error message";

    @Autowired
    @Lazy
    private DummyService self;

    @CelestaTransaction
    public void withContext(CallContext cc, String name) throws InterruptedException {
        //to test 'getDuration'
        Thread.sleep(1);
        assertAll(
                () -> assertEquals("foo", cc.getUserId()),
                () -> assertEquals("bar", name),
                () -> assertEquals("DummyService.withContext(..)", cc.getProcName()),
                () -> assertTrue(cc.getConn().isValid(1000)),
                () -> assertEquals(DBType.H2, cc.getDbAdaptor().getType()),
                //we slept for 1 ms
                () -> assertTrue(cc.getDurationNs() >= 1000)
        );
    }

    @CelestaTransaction
    public void noContext(String param) {
        assertEquals("test", param);
    }

    @CelestaTransaction
    public void exception(CallContext cc) throws SQLException {
        throw new SQLException(ERROR_MESSAGE);
    }

    @CelestaTransaction
    public void callProxiedLevel0(CallContext cc) {
        self.callProxiedLevel1(cc);
    }

    @CelestaTransaction
    public void callProxiedLevel1(CallContext cc) {
        // do nothing
    }

}
