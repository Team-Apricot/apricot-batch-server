package sandbox.apricot.util;

public class PeriodFormatter {

    public static String formatPrdRpttSecd(String prdRpttSecd) {
        return switch (prdRpttSecd) {
            case "002001" -> "상시";
            case "002002" -> "연간반복";
            case "002003" -> "월간반복";
            case "002004" -> "특정기간";
            default -> "미정"; // 002005 ...
        };
    }

}
