package course.concurrency.exams.refactoring;

import java.util.function.Supplier;

public class MountTableRefresher implements Supplier<RefreshResult> {

    private final String adminAddress;
    private final Others.MountTableManager manager;

    public String getAdminAddress() {
        return adminAddress;
    }

    public MountTableRefresher(Others.MountTableManager manager, String adminAddress) {
        this.adminAddress = adminAddress;
        this.manager = manager;
    }

    @Override
    public RefreshResult get() {
        var success = manager.refresh();
        return new RefreshResult(adminAddress, success);
    }
}
