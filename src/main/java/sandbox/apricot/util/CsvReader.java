package sandbox.apricot.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CsvReader {

  // Mapper로 보낼 영역
//  public static void main(String[] args) {
//    String csvFilePath = "input.csv";  // CSV 파일 경로
//
//    try {
//      List<String[]> records = readCSV(csvFilePath);
//      for (String[] record : records) {
//        System.out.println(String.join(", ", record));  // CSV 파일의 각 레코드를 출력
//      }
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
//  }

  public static List<String[]> readCSV(String filePath) throws IOException {
    List<String[]> records = new ArrayList<>();
    try (BufferedReader br = Files.newBufferedReader(Paths.get(filePath),
        Charset.forName("EUC-KR"))) {
      String line;
      while ((line = br.readLine()) != null) {
        String[] values = line.split(",");  // 쉼표로 데이터를 나눕니다.
        records.add(values);
      }
    }
    return records;
  }
}