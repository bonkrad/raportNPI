package pl.radekbonk.repository;

import org.springframework.data.repository.CrudRepository;
import pl.radekbonk.entity.ProblemEntity;

import java.util.List;

public interface ProblemRepository extends CrudRepository<ProblemEntity, Long> {
	List<ProblemEntity> findByReportId(long reportId);
	List<ProblemEntity> findByReportIdAndPriorityGreaterThan(long reportId, int priority);
	List<ProblemEntity> findByReportIdAndPriorityLessThan(long reportId, int priority);

	void deleteById(long id);

	ProblemEntity findOne(long id);
}
