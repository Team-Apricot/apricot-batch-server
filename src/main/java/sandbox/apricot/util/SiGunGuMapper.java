package sandbox.apricot.util;

import java.util.HashMap;
import java.util.Map;

public class SiGunGuMapper {

  private static final Map<String, String> siGunGuMap = new HashMap<>();

  // 시군구 csv 를 읽고, 시군구에 해당하는 코드와 이름, 읍면동의 정보를 가진 JSON파일을 만듭니다.
  // 1. Read 하는 방법 찾기. BufferedStream Reader와 같은 방식을 이용.
  // 2. JSON 파일에 입력하는 방법. 수정하는 방법을 이용하여 csv에서 읽어온 한줄의 데이터를 토대로 JSON 파일을 생성


  static {
    // Mapper 초기화
    siGunGuMap.put("110", "종로구");
    siGunGuMap.put("111", "중구");
    siGunGuMap.put("112", "용산구");
  }

  public static String getSiGunGuName(String code) {
    // 코드의 앞 3자리만 사용하여 시군구 이름 반환
    String key = code.substring(0, 5);
    return siGunGuMap.getOrDefault(key, "알 수 없는 시군구");
  }
}
