package server.tools;

public class CheckTools {

    private CheckTools() {
    }

    public static Boolean checkParam(String... params) {
        for (String param : params) {
            if (param == null || param.trim().equals("")) {
                return false;
            }
        }
        return true;
    }
}
