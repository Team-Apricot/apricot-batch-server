package sandbox.apricot.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmdJSONConverter {

  public static void createEmdJson(String[] args) {
    String csvFilePath = "src/main/java/sandbox/apricot/util/EmdCode.csv";  // CSV 파일 경로
    String jsonFilePath = "src/main/java/sandbox/apricot/util/EmdCode.json";  // JSON 파일 경로

    try {
      JsonNode jsonNode = convertCsvToCustomJson(csvFilePath);
      saveJsonToFile(jsonNode, jsonFilePath);
      System.out.println("JSON 파일이 생성되었습니다: " + jsonFilePath);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static JsonNode convertCsvToCustomJson(String csvFilePath) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    ObjectNode rootNode = objectMapper.createObjectNode();  // 최상위 JSON 객체

    // CSV 파일을 읽고 파싱
    try (FileReader reader = new FileReader(csvFilePath);
        CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

      List<String> headers = csvParser.getHeaderNames();  // CSV 헤더 가져오기

      // 시군구 코드와 시군구 이름을 저장하는 Map
      Map<String, ObjectNode> siGunGuMap = new HashMap<>();

      // CSV 레코드 순회
      for (CSVRecord record : csvParser) {
        String siGunGuCode = record.get(headers.get(0)).trim();  // 시군구 코드
        String eupMyeonDongCode = record.get(headers.get(1)).trim();  // 읍면동 코드
        String eupMyeonDongName = record.get(headers.get(2)).trim();  // 읍면동 이름

        // 시군구 코드가 없다면 새로 추가
        if (!siGunGuMap.containsKey(siGunGuCode)) {
          // 시군구 이름과 코드를 포함하는 JSON 객체 생성
          ObjectNode siGunGuNode = objectMapper.createObjectNode();
          siGunGuNode.put("시군구 코드", siGunGuCode);
          siGunGuNode.set("해당구 읍면동", objectMapper.createObjectNode());

          // 시군구 이름을 키로 JSON 객체 추가
          rootNode.set(siGunGuCode, siGunGuNode);
          siGunGuMap.put(siGunGuCode, siGunGuNode);
        }

        // 읍면동 정보를 해당 시군구에 추가
        ObjectNode eupMyeonDongNode = (ObjectNode) siGunGuMap.get(siGunGuCode).get("해당구 읍면동");
        eupMyeonDongNode.put(eupMyeonDongName, eupMyeonDongCode);
      }
    }

    return rootNode;  // JSON 객체 반환
  }

  public static void saveJsonToFile(JsonNode jsonNode, String jsonFilePath) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(jsonFilePath), jsonNode);
  }
}
