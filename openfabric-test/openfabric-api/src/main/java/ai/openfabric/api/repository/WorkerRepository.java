package ai.openfabric.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import ai.openfabric.api.model.Worker;

public interface WorkerRepository extends CrudRepository<Worker, String> {

	List<Worker> findAll();
    Optional<Worker> findById(String id);
    Optional<Worker> findByName(String name);
    Page<Worker> findAll(Pageable pageable);
}
