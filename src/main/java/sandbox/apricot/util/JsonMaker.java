package sandbox.apricot.util;

import jakarta.persistence.criteria.Root;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import org.json.simple.JSONObject;

public class JsonMaker {

  static void SiGunGuJson() throws IOException {
    List<String[]> rc = CsvReader.readCSV("src/main/java/sandbox/apricot/util/SiGunGuCode.csv");

    HashMap<String, Object> RootMap = new HashMap<>();
    HashMap<String, Object> SiMap = new HashMap<>();
//    HashMap<String,Object> SiMap = new HashMap<>();
//    JSONObject RootObject = new JSONObject();
    JSONObject SiObject = new JSONObject();
    JSONObject GuObject = new JSONObject();
//    JSONObject DongObject = new JSONObject();

    for (String[] row : rc) {
      System.out.println(row[0] + "\t" + row[1] + "\t" + row[2] + "\t" + row[3]);
      if (SiObject.get(row[1]) == null) {
        SiObject.put(row[1], GuObject);
        GuObject.put(row[2], row[0]);
      } else {
        GuObject.put(row[2], row[0]);
      }
    }
    System.out.println(SiObject.toJSONString());
  }

}
