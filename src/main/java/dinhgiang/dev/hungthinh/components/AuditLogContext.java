package dinhgiang.dev.hungthinh.components;

public final class AuditLogContext {
    private static final ThreadLocal<Boolean> LOGGED = ThreadLocal.withInitial(() -> false);

    private AuditLogContext() {
    }

    public static void markLogged() {
        LOGGED.set(true);
    }

    public static boolean alreadyLogged() {
        return LOGGED.get();
    }

    public static void clear() {
        LOGGED.remove();
    }
}
