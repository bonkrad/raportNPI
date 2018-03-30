package pl.radekbonk.repository;

import org.springframework.data.repository.CrudRepository;
import pl.radekbonk.entity.TaskEntity;

import java.util.List;

public interface TaskRepository extends CrudRepository<TaskEntity, Long> {
	List<TaskEntity> findByReportIdOrderByRequiredDateAsc(long reportId);
	void deleteById(long id);
	TaskEntity findOne(long id);
}
