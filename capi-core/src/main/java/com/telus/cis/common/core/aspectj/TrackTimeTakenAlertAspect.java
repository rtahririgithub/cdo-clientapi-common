
package com.telus.cis.common.core.aspectj;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Aspect
@Component
public class TrackTimeTakenAlertAspect
{

    /**
     * Todo need to validate if Mono.deferContextual is working
     * @trackTime may not work with Reactor-core, actuate operation time should based on Subscriber::subscription
     */
    @Around("@annotation(trackTimeTaken)")
    public Object trackTimeTakenAround(ProceedingJoinPoint joinPoint, TrackTimeTaken trackTimeTaken) throws Throwable
    {
        long start = System.nanoTime();
        try {
            return joinPoint.proceed();
        }
        finally {
            Long timeTakenInMilliSecond = (System.nanoTime() - start) / 1000000;
            if ( timeTakenInMilliSecond > trackTimeTaken.alertTimeLimit() ) {
                Mono.deferContextual( Mono::just )
                        .map( context -> context.get( ServerHttpRequest.class ) )
                        .filter( Objects::nonNull )
                        .doOnSuccess( request -> {
                            log.warn( "time taken: {} seconds for {}({}), request comes from ({}:{})",
                                    timeTakenInMilliSecond.doubleValue() / 1000.0, joinPoint.getSignature().getName(),
                                    argsToString( joinPoint.getArgs() ), request.getRemoteAddress().getHostName(), request.getRemoteAddress().getPort() );
                        } );
            }
            else {
                log.warn( "time taken: {} seconds for {}({})", timeTakenInMilliSecond.doubleValue() / 1000.0,
                        joinPoint.getSignature().getName(), argsToString( joinPoint.getArgs() ) );
            }
        }
    }


    private static String argsToString(Object[] args)
    {
        if ( isEmpty( args ) ) {
            return "";
        }

        StringBuilder builder = new StringBuilder( truncateArg( args[0] ) );
        for ( int i = 1; i < args.length; i++ ) {
            builder.append( "," ).append( truncateArg( args[i] ) );
        }

        return builder.toString();
    }


    private static String truncateArg(Object arg)
    {
        if ( arg == null ) {
            return "null";
        }

        try {
            String str = arg.toString();
            if ( str.length() > 100 ) {
                str = str.substring( 0, 100 ) + "!TRUNCATED";
            }

            return str.replaceAll( "\\r", "" ).replaceAll( "\\n", " " ).replaceAll( "  *", " " );

        }
        catch ( Exception e ) {
            return "...";
        }
    }


    private static boolean isEmpty(final Object object)
    {
        if ( object == null ) {
            return true;
        }
        if ( object instanceof CharSequence ) {
            return ((CharSequence) object).length() == 0;
        }
        if ( object.getClass().isArray() ) {
            return Array.getLength( object ) == 0;
        }
        if ( object instanceof Collection<?> ) {
            return ((Collection<?>) object).isEmpty();
        }
        if ( object instanceof Map<?, ?> ) {
            return ((Map<?, ?>) object).isEmpty();
        }

        return false;
    }

}
