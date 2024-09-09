package sandbox.apricot.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtractMapper {

    public static Map<String, String> extractDates(String date) {
        Map<String, String> dateMap = new HashMap<>();

        // Pattern for YYYY-MM-DD ~ YYYY-MM-DD
        Pattern pattern1 = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})\\s*~\\s*(\\d{4}-\\d{2}-\\d{2})");
        // Pattern for YYYY.MM.DD ~ YYYY.MM.DD
        Pattern pattern2 = Pattern.compile("(\\d{4}\\.\\d{2}\\.\\d{2})\\s*~\\s*(\\d{4}\\.\\d{2}\\.\\d{2})");
        // Pattern for --DD ~ --DD
        Pattern pattern3 = Pattern.compile("--(\\d{2})~--(\\d{2})");

        Matcher matcher1 = pattern1.matcher(date);
        Matcher matcher2 = pattern2.matcher(date);
        Matcher matcher3 = pattern3.matcher(date);

        if (matcher1.find()) {
            dateMap.put("policyStartDate", matcher1.group(1));
            dateMap.put("policyEndDate", matcher1.group(2));
        } else if (matcher2.find()) {
            dateMap.put("policyStartDate", matcher2.group(1).replace('.', '-'));
            dateMap.put("policyEndDate", matcher2.group(2).replace('.', '-'));
        } else if (matcher3.find()) {
            dateMap.put("policyStartDate", "--" + matcher3.group(1));
            dateMap.put("policyEndDate", "--" + matcher3.group(2));
        }

        return dateMap;
    }

}
