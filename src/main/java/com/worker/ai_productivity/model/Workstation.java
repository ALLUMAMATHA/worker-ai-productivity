package com.worker.ai_productivity.model;

import jakarta.persistence.*;

@Entity
@Table(name = "workstations", indexes = {
        @Index(name = "idx_workstation_id", columnList = "workstation_id")
})
public class Workstation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "workstation_id", nullable = false, unique = true)
    private String workstationId;

    @Column(nullable = false)
    private String name;

    public Workstation() {
    }

    public Workstation(String workstationId, String name) {
        this.workstationId = workstationId;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getWorkstationId() {
        return workstationId;
    }

    public void setWorkstationId(String workstationId) {
        this.workstationId = workstationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
