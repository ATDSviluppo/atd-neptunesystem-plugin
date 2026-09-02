package com.MailModule.Service;

import com.MailModule.Entity.EmployeeMailConstructor;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.List;

@Service
public interface MailService {
    List<EmployeeMailConstructor> findByEmailNotNullOrEmpty();

    boolean setupEmail(String employeeId, String email);

    List<String> getEmailToSendExpirationAdvice() throws JsonProcessingException;
}
