package pl.radekbonk.repository;

import org.springframework.data.repository.CrudRepository;
import pl.radekbonk.entity.ReportEntity;

import java.util.List;

public interface ReportRepository extends CrudRepository<ReportEntity, Long> {
	List<ReportEntity> findByProductId(long productId);
	List<ReportEntity> findByProductIdOrderByRevisionDesc(long productId);
	List<ReportEntity> findByProductIdOrderByRevisionAsc(long productId);
	ReportEntity findFirstByProductIdOrderByRevisionDesc(long productId);

	ReportEntity findOne(long id);
}
