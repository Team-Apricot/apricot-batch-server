package sandbox.apricot.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AgeInfoMapper {

    public static Map<String, Integer> extractAgeRange(String ageInfo) {
        Map<String, Integer> ageMap = new HashMap<>();

        Pattern pattern1 = Pattern.compile("만\\s*(\\d+)세\\s*~\\s*(\\d+)세");
        Pattern pattern2 = Pattern.compile("만\\s*(\\d+)세\\s*이상");
        Pattern pattern3 = Pattern.compile("만\\s*(\\d+)세\\s*~\\s*(\\d+)세\\s*이상");
        Pattern pattern4 = Pattern.compile("제한없음");

        Matcher matcher1 = pattern1.matcher(ageInfo);
        Matcher matcher2 = pattern2.matcher(ageInfo);
        Matcher matcher3 = pattern3.matcher(ageInfo);
        Matcher matcher4 = pattern4.matcher(ageInfo);

        if (matcher1.find()) {
            ageMap.put("minAge", Integer.parseInt(matcher1.group(1)));
            ageMap.put("maxAge", Integer.parseInt(matcher1.group(2)));
        } else if (matcher2.find()) {
            ageMap.put("minAge", Integer.parseInt(matcher2.group(1)));
            ageMap.put("maxAge", null); // 제한 없음
        } else if (matcher3.find()) {
            ageMap.put("minAge", Integer.parseInt(matcher3.group(1)));
            ageMap.put("maxAge", null); // 제한 없음
        } else if (matcher4.find()) {
            ageMap.put("minAge", null); // 제한 없음
            ageMap.put("maxAge", null); // 제한 없음
        } else {
            ageMap.put("minAge", 0); // 미정
            ageMap.put("maxAge", 0); // 미정
        }

        return ageMap;
    }
}
