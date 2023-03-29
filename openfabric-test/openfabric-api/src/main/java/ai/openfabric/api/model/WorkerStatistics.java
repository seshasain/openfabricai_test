package ai.openfabric.api.model;

import javax.persistence.Entity;

import org.springframework.data.annotation.Id;

@Entity
public class WorkerStatistics {
	@Id
    private Long id;


    private Long workerId;


    private Long totalRequests;


    private Long successfulRequests;


    private Long failedRequests;


	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}


	public Long getWorkerId() {
		return workerId;
	}


	public void setWorkerId(Long workerId) {
		this.workerId = workerId;
	}


	public Long getTotalRequests() {
		return totalRequests;
	}


	public void setTotalRequests(Long totalRequests) {
		this.totalRequests = totalRequests;
	}


	public Long getSuccessfulRequests() {
		return successfulRequests;
	}


	public void setSuccessfulRequests(Long successfulRequests) {
		this.successfulRequests = successfulRequests;
	}


	public Long getFailedRequests() {
		return failedRequests;
	}


	public void setFailedRequests(Long failedRequests) {
		this.failedRequests = failedRequests;
	}
    
}
