package course.concurrency.exams.refactoring;

public class RefreshResult {
    private String address;
    private boolean success;
    private boolean timedOut;

    public RefreshResult(String address, boolean success) {
        this.address = address;
        this.success = success;
        this.timedOut = false;
    }

    public RefreshResult(String address, boolean success, boolean timedOut) {
        this.address = address;
        this.success = success;
        this.timedOut = timedOut;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setTimedOut(boolean timedOut) {
        this.timedOut = timedOut;
    }

    public boolean isTimedOut() {
        return timedOut;
    }
}
