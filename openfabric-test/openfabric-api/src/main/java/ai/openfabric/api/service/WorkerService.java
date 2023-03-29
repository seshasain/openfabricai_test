package ai.openfabric.api.service;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse.ContainerState;
import com.github.dockerjava.api.command.InspectExecResponse.Container;
import com.github.dockerjava.api.command.StopContainerCmd;
import com.github.dockerjava.api.exception.BadRequestException;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.core.DockerClientBuilder;
import com.yahoo.elide.core.HttpStatus;

import ai.openfabric.api.model.Worker;
import ai.openfabric.api.repository.WorkerRepository;

@Service
public class WorkerService {
    private final WorkerRepository workerRepository;
    private final DockerClient dockerClient;


    public void startWorker(String id) throws Exception {
        Optional<Worker> optionalWorker = workerRepository.findById(id);
        if (optionalWorker.isEmpty()) {
            throw new NotFoundException("Worker not found with id " + id);
        }

        Worker worker = optionalWorker.get();

        if (worker.getStatus().equals("Running")) {
            throw new BadRequestException("Worker is already running");
        }

        // create a new container
        CreateContainerCmd createContainerCmd = dockerClient.createContainerCmd(worker.getImageName())
                .withName(worker.getName())
                .withExposedPorts(ExposedPort.tcp(worker.getPort()))
                .withHostConfig(new HostConfig().withPortBindings(new PortBinding(Ports.Binding.bindPort(worker.getPort()), new ExposedPort(worker.getPort()))));
        
        String containerId = createContainerCmd.exec().getId();

        // start the container
        dockerClient.startContainerCmd(containerId).exec();

        // update worker status
        worker.setStatus("Running");
        worker.setId(containerId);
        workerRepository.save(worker);
    }
    
    
    public ResponseEntity<String> stopWorker(@PathVariable String id) {
        Worker worker = workerRepository.findById(id).orElse(null);
        if (worker == null) {
            return ResponseEntity.notFound().build();
        }

        // Stop the container using Docker Java library
        try {
            StopContainerCmd stopContainerCmd = dockerClient.stopContainerCmd(worker.getContainerId());
            stopContainerCmd.exec();
        } catch (NotFoundException e) {
            // If the container is not found, it means it's already stopped
            return ResponseEntity.ok("Worker is already stopped.");
        }

        // Update the worker status in the database
        worker.setStatus("Stopped");
        workerRepository.save(worker);

        return ResponseEntity.ok("Worker stopped successfully.");
    }
    
    public ResponseEntity<Worker> getWorkerInfo(String id) {
        Optional<Worker> optionalWorker = workerRepository.findById(id);
        if (optionalWorker.isPresent()) {
            Worker worker = optionalWorker.get();
            String containerId = worker.getId();
            DockerClient dockerClient = DockerClientBuilder.getInstance().build();
            InspectContainerResponse inspectContainerResponse = dockerClient.inspectContainerCmd(containerId).exec();
            worker.setStatus(inspectContainerResponse.getState().getStatus());
            return new ResponseEntity<>(worker, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    
    public ResponseEntity<Container> getWorkerStatistics(String id) {
        Optional<Worker> optionalWorker = workerRepository.findById(id);
        if (optionalWorker.isPresent()) {
            Worker worker = optionalWorker.get();
            String containerId = worker.getId();
            DockerClient dockerClient = DockerClientBuilder.getInstance().build();
            Container container = dockerClient.listContainersCmd().withIdFilter(containerId).exec().stream().findFirst().orElse(null);
            if (container != null) {
                ContainerState statsCallback = new ContainerStatsCallback();
                dockerClient.statsCmd(container.getId()).exec(statsCallback);
                ContainerStats stats = statsCallback.awaitResponse();
                return new ResponseEntity<>(stats, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

}
