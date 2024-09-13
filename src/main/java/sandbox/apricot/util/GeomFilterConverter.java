package sandbox.apricot.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public class GeomFilterConverter {

  public static void createMultipolygonRegion() throws FileNotFoundException {
    // JSON 파일 경로 (리소스 경로로 설정)
    String JSONPath = "/artmuseum/TL_SCCO_SIG.json";  // 파일 경로는 resources 폴더를 기준으로 설정
    ArrayList<String> enumList = new ArrayList<>();
    ArrayList<String> regionKORNames = new ArrayList<>();
    ArrayList<String> regionENGNames = new ArrayList<>();

    // Multipolygon 최대 개수 설정
    int limiter = 190;
    // StreamReader
    try (Reader reader = new InputStreamReader(
        GeomFilterConverter.class.getResourceAsStream(JSONPath),
        StandardCharsets.UTF_8)) {
      // JSONParser 객체 생성
      JSONParser parser = new JSONParser();
      // JSONObject 생성 시작
      JSONObject jsonObject = (JSONObject) parser.parse(reader);
      List<JSONObject> features = (List<JSONObject>) jsonObject.get("features");
      for (JSONObject feature : features) {

        // 지역구별 Multipolygon 형식으로 RegionCoord에 저장
        StringBuilder regionCoord = new StringBuilder();
        JSONObject geom = (JSONObject) feature.get("geometry");
        List<List> coordinates = (List<List>) geom.get("coordinates");
        regionCoord.append("MULTIPOLYGON(((");

        // 지역구 별 한글 이름 RegionKORName에 저장
        StringBuilder regionKORName = new StringBuilder();
        JSONObject properties = (JSONObject) feature.get("properties");
        regionKORName.append(properties.get("SIG_KOR_NM"));

        // 지역구 별 영어 이름 RegionENGName에 저장
        StringBuilder regionENGName = new StringBuilder();
        regionENGName.append(properties.get("SIG_ENG_NM")); // 영어 이름이 SIG_ENG_NM 필드에 있다고 가정

        // 지역구 별 행정 코드 String RegionSIGCode으로 저장.
        String RegionSIGCode = (String) properties.get("SIG_CD");

        if (RegionSIGCode.startsWith("11")) {
          int mpCount = 0;
          Object finale = coordinates.get(0).get(0);
          for (Object coord : coordinates.get(0)) {
            if (mpCount >= limiter) {
              System.out.println(regionKORName + "의 Multipolygon이 너무 길어 끊겼습니다.😥");
              for (String c1 : finale.toString().substring(1, finale.toString().length() - 1)
                  .split(",")) {
                regionCoord.append(c1);
              }
              break;
            }

            for (Object c : (List) coord) {
              regionCoord.append(c.toString())
                  .append(" ");
            }

            mpCount++;
            regionCoord.deleteCharAt(regionCoord.length() - 1);
            regionCoord.append(",");
          }

          // 마지막 콤마 제거 후 마무리
          regionCoord.deleteCharAt(regionCoord.length() - 1);
          regionCoord.append(")))");

          System.out.println(regionKORName + "의 url 예상 길이 : " + (regionCoord.length() + 127));

          enumList.add(regionCoord.toString());
          regionKORNames.add(regionKORName.toString());
          regionENGNames.add(regionENGName.toString());
        }
      }
      // Enum 파일 생성
      createEnumFile("MultiPolygonRegion", regionENGNames, regionKORNames, enumList);

    } catch (IOException | ParseException e) {
      e.printStackTrace();
    }

  }

  // 3. Enum 형태로 저장하기.
  private static void createEnumFile(String enumName, List<String> regionENGNames,
      List<String> regionKORNames, List<String> enumValues) {
    String filePath = "src/main/java/sandbox/apricot/util/" + enumName + ".java"; // 생성할 파일 경로 설정
    File file = new File(filePath);

    try (FileWriter fileWriter = new FileWriter(file, false)) {
      // Enum 클래스 시작
      fileWriter.write("package sandbox.apricot.util;\n\n");
      fileWriter.write("import lombok.Getter;\n");
      fileWriter.write("import lombok.RequiredArgsConstructor;\n\n");
      fileWriter.write("@Getter\n");
      fileWriter.write("@RequiredArgsConstructor\n");
      fileWriter.write("public enum " + enumName + " {\n");

      // Enum 값 작성
      for (int i = 0; i < regionENGNames.size(); i++) {
        String regionENGName = regionENGNames.get(i).toUpperCase().replace(" ", "_")
            .replace("-", "_");
        String regionKORName = regionKORNames.get(i);
        String multipolygon = enumValues.get(i);
        fileWriter.write(
            "    " + regionENGName + "(\"" + regionKORName + "\", \"" + multipolygon + "\")");
        if (i < regionENGNames.size() - 1) {
          fileWriter.write(",\n");
        } else {
          fileWriter.write(";\n");
        }
      }

      // 추가 필드 및 생성자
      fileWriter.write("\n    private final String korName;\n");
      fileWriter.write("    private final String multipolygon;\n\n");

      // Enum 클래스 끝
      fileWriter.write("}\n");

      System.out.println(enumName + " enum 파일이 생성되었습니다: " + filePath);

    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
