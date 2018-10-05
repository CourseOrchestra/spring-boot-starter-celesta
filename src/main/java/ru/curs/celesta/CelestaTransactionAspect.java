package ru.curs.celesta;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class CelestaTransactionAspect {
    private final Celesta celesta;

    public CelestaTransactionAspect(Celesta celesta) {
        this.celesta = celesta;
    }

    @Around(value = "@annotation(CelestaTransaction)")
    public Object execEntryPoint(ProceedingJoinPoint joinPoint) throws Throwable {
        CallContext cc = null;
        try {
            for (Object arg : joinPoint.getArgs()) {
                if (arg instanceof CallContext) {
                    cc = (CallContext) arg;
                    cc.activate(celesta, joinPoint.getSignature().toShortString());
                    break;
                }
            }
            Object result = joinPoint.proceed();
            if (cc != null)
                cc.commit();
            return result;
        } catch (Throwable e) {
            if (cc != null)
                cc.rollback();
            throw e;
        } finally {
            if (cc != null)
                cc.close();
        }
    }
}
