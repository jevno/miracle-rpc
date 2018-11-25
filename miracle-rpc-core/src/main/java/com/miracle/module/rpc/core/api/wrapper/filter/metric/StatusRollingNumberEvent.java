package com.miracle.module.rpc.core.api.wrapper.filter.metric;


/**
 * Various states/events that can be captured in the {@link StatusRollingNumber}.
 * <p>
 * Note that events are defined as different types:
 * <ul>
 * <li>Counter: <code>isCounter() == true</code></li>
 * <li>MaxUpdater: <code>isMaxUpdater() == true</code></li>
 * </ul>
 * <p>
 * The Counter type events can be used with {@link StatusRollingNumber#increment}, {@link StatusRollingNumber#add}, {@link StatusRollingNumber#getRollingSum} and others.
 * <p>
 * The MaxUpdater type events can be used with {@link StatusRollingNumber#updateRollingMax} and {@link StatusRollingNumber#getRollingMaxValue}.
 */
public enum StatusRollingNumberEvent {
    SUCCESS(1), FAILURE(1), TIMEOUT(1), SHORT_CIRCUITED(1), DEGRADED(1), TPS_LIMITED(1), THREAD_POOL_REJECTED(1), 
    SEMAPHORE_REJECTED(1), BAD_REQUEST(1), EXCEPTION_THROWN(1), COMMAND_CONCURRENT(1), COMMAND_MAX_CONCURRENT(2),
    RESPONSE_FROM_CACHE(1), SUCCESS_ELAPSED(1), FAILED_ELAPSED(1);

    private final int type;

    private StatusRollingNumberEvent(int type) {
        this.type = type;
    }

    public boolean isCounter() {
        return type == 1;
    }

    public boolean isMaxUpdater() {
        return type == 2;
    }
}
