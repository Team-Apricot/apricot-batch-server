package sandbox.apricot.artmuseum.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import sandbox.apricot.artmuseum.entity.ArtMuseum;

public interface ArtMuseumRepository extends JpaRepository<ArtMuseum, String> {

  // ArtMuseum의 정보가 없을 경우를 고려한 Optional 타입.
  Optional<ArtMuseum> findArtMuseumByMuseumName(String museumName);
}
