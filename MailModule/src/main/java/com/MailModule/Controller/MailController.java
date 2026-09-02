package com.MailModule.Controller;

import com.MailModule.Entity.EmployeeMailConstructor;
import com.MailModule.Service.MailService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.util.List;


@RestController
@Slf4j
public class MailController {
    @Autowired
    private MailService mailService;

    @CrossOrigin(origins = "*")
    @GetMapping("/getEmployeeWithEmailNotNull")
    public List<EmployeeMailConstructor> getEmployeeWithEmailNotNull() {
       return mailService.findByEmailNotNullOrEmpty();
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/getEmailToSendExpirationAdvice")
    public List<String> getEmailToSendExpirationAdvice() throws JsonProcessingException {
        return mailService.getEmailToSendExpirationAdvice();
    }
}
