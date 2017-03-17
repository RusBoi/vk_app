package struct;

public class ProgramState {
    public int APP_ID;
    public String CLIENT_SECRET;
    public String ACCESS_TOKEN;
    public BanList banList;

    public ProgramState(int APP_ID, String CLIENT_SECRET, String ACCESS_TOKEN, BanList banList) {
        this.APP_ID = APP_ID;
        this.CLIENT_SECRET = CLIENT_SECRET;
        this.ACCESS_TOKEN = ACCESS_TOKEN;
        this.banList = banList;
    }
}
