package ru.curs.celesta.spring.boot.autoconfigure;

import org.springframework.stereotype.Service;
import ru.curs.celesta.CallContext;
import ru.curs.celesta.CelestaTransaction;
import ru.curs.celesta.DBType;
import ru.curs.celesta.ICelesta;

import static org.junit.jupiter.api.Assertions.*;

@Service
public class DummyService {
    @CelestaTransaction
    public void greet(CallContext cc, String name) throws InterruptedException {
        //to test 'getDuration'
        Thread.sleep(1);
        assertAll(
                () -> assertEquals("foo", cc.getUserId()),
                () -> assertEquals("bar", name),
                () -> assertEquals("DummyService.greet(..)", cc.getProcName()),
                () -> assertTrue(cc.getConn().isValid(1000)),
                () -> assertEquals(DBType.H2, cc.getDbAdaptor().getType()),
                //we slept for 1 ms
                () -> assertTrue(cc.getDurationNs() >= 1000)
        );
    }
}
