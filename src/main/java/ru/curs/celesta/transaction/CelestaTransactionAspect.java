package ru.curs.celesta.transaction;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import ru.curs.celesta.CallContext;
import ru.curs.celesta.ICelesta;

import java.util.Arrays;
import java.util.Optional;

/**
 * Aspect for CelestaTransaction annotated methods.
 */
@Aspect
public final class CelestaTransactionAspect {

    private final ICelesta celesta;

    public CelestaTransactionAspect(ICelesta celesta) {
        this.celesta = celesta;
    }

    /**
     * Implements Around advice for @CelestaTransaction-annotated methods.
     *
     * @param joinPoint Method call
     * @return Method result
     * @throws Throwable Rethrown method exception
     */
    @Around(value = "@annotation(CelestaTransaction)")
    public Object execEntryPoint(ProceedingJoinPoint joinPoint) throws Throwable {
        Optional<CallContext> cc =
                Arrays.stream(joinPoint.getArgs())
                        .filter(CallContext.class::isInstance)
                        .map(CallContext.class::cast)
                        .findFirst();
        if (cc.isPresent()) {
            return proceedInTransaction(cc.get(), joinPoint);
        } else {
            return joinPoint.proceed();
        }
    }

    private Object proceedInTransaction(CallContext c, ProceedingJoinPoint joinPoint) throws Throwable {

        /* Skip CallContext activation in case it is already active. Non-zero DBPID
           is not a perfect way to distinguish active CallContexts (e. g. a closed
           CallContext will also have a cached non-zero DBPID), but  since we
           do not have public 'isActive' property on CallContext, it's just
           a working solution. */
        if (c.getDBPid() != 0) {
            return joinPoint.proceed();
        }

        try {
            c.activate(celesta, joinPoint.getSignature().toShortString());
            Object result = joinPoint.proceed();
            c.commit();
            return result;
        } catch (Throwable e) {
            c.rollback();
            throw e;
        } finally {
            c.close();
        }
    }

}
