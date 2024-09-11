package sandbox.apricot.artmuseum.service;

import java.io.UnsupportedEncodingException;
import java.util.List;
import sandbox.apricot.artmuseum.dto.response.ArtMuseumDTO;

public interface ArtMuseumService {

  // 미술관/도서관 정보를 불러옵니다.
  List<ArtMuseumDTO> getAllArtMuseums() throws UnsupportedEncodingException, InterruptedException;

  // 미술관/도서관 데이터를 받아, 데이터베이스에 저장합니다.
  void saveArtMuseum(List<ArtMuseumDTO> artMuseum);
}
