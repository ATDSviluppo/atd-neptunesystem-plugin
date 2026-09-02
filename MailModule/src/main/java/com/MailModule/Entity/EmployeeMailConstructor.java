package com.MailModule.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import jakarta.persistence.Id;

@Entity
@Table(name = "Employee")
@Data
public class EmployeeMailConstructor {
    @Id
    @Column(name = "employeeId")
    private String employeeId;

    @Column(name = "employeeName")
    private String employeeName;

    @Column(name = "email", unique = true)
    private String email;
}
