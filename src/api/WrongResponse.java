package api;

public class WrongResponse extends Exception {
    private String error_code;
    private String error_msg;

    public WrongResponse(String error_code, String error_msg) {
        this.error_code = error_code;
        this.error_msg = error_msg;
    }

    @Override
    public String toString() {
        return "WrongResponse{" +
                "error_code='" + error_code + '\'' +
                ", error_msg='" + error_msg + '\'' +
                '}';
    }
}
