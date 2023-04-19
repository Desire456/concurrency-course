package course.concurrency.exams.refactoring;

public class RefreshResult {
    private String address;
    private boolean success;

    public RefreshResult(String address, boolean success) {
        this.address = address;
        this.success = success;
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
}
