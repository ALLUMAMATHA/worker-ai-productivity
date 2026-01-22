package com.worker.ai_productivity.model;

import jakarta.persistence.*;

@Entity
@Table(name = "workers")
public class Worker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "worker_id", nullable = false, unique = true)
    private String workerId;

    @Column(nullable = false)
    private String name;

    public Worker() {
    }

    public Worker(String workerId, String name) {
        this.workerId = workerId;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
