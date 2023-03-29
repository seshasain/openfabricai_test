package ai.openfabric.api.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ai.openfabric.api.model.Worker;
import ai.openfabric.api.repository.WorkerRepository;
import ai.openfabric.api.service.WorkerService;

@RestController
@RequestMapping("${node.api.path}/worker")
public class WorkerController {

    @Autowired
    private WorkerService workerservice;
    private WorkerRepository workerRepository;

    // List workers (paginated)
    @GetMapping("/")
    public Page<Worker> getAllWorkers(Pageable pageable) {
        return workerRepository.findAll(pageable);
    }

    // Start worker
    @PostMapping("/{id}/start")
    public ResponseEntity<String> startWorker(@PathVariable String id) throws Exception {
        
    	workerservice.startWorker(id);
        return ResponseEntity.ok("Worker with ID " + id + " started successfully");
    }

    // Stop worker
    @PostMapping("/{id}/stop")
    public ResponseEntity<String> stopWorker(@PathVariable String id) {
        workerservice.stopWorker(id);
        return ResponseEntity.ok("Worker with ID " + id + " stopped successfully");
    }

    // Get worker information
    @GetMapping("/{id}")
    public ResponseEntity<Worker> getWorkerById(@PathVariable String id) {
        return workerRepository.findById(id)
                .map(worker -> ResponseEntity.ok().body(worker))
                .orElse(ResponseEntity.notFound().build());
    }

    // Get worker statistics
    @GetMapping("/{id}/stats")
    public ResponseEntity<String> getWorkerStatistics(@PathVariable String id) {
        workerservice.getWorkerStatistics(id);
        return ResponseEntity.ok("Statistics for worker with ID " + id);
    }

}
