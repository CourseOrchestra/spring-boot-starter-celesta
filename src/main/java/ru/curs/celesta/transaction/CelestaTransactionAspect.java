package ru.curs.celesta.transaction;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import ru.curs.celesta.CallContext;
import ru.curs.celesta.Celesta;

import java.util.Arrays;
import java.util.Optional;

/**
 * Aspect for CelestaTransaction annotated methods.
 */
@Aspect
public final class CelestaTransactionAspect {
    private final Celesta celesta;

    public CelestaTransactionAspect(Celesta celesta) {
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
                        .filter(arg -> arg instanceof CallContext)
                        .map(arg -> (CallContext)arg)
                        .findFirst();
        cc.ifPresent(c -> c.activate(celesta, joinPoint.getSignature().toShortString()));
        try {
            Object result = joinPoint.proceed();
            cc.ifPresent(CallContext::commit);
            return result;
        } catch (Throwable e) {
            cc.ifPresent(CallContext::rollback);
            throw e;
        } finally {
            cc.ifPresent(CallContext::close);
        }
    }
}
