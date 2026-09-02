package com.MailModule.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class EmployeeConstructorDTO {

    @JsonProperty("EmployeeId")
    private String employeeId;

    @JsonProperty("EmployeeName")
    private String employeeName;

    @JsonProperty("EmployeeRole")
    private String employeeRole;

    @JsonProperty("EmployeeCard")
    private String employeeCard;

    @JsonProperty("Email")
    private String email;
}
