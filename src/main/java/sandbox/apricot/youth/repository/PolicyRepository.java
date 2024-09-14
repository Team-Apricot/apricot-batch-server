package sandbox.apricot.youth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sandbox.apricot.youth.entity.Policy;

public interface PolicyRepository extends JpaRepository<Policy, String> {
}
