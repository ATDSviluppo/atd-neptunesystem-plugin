package com.MailModule.Mapper;

import com.MailModule.DTO.EmployeeConstructorDTO;
import com.MailModule.Repository.MailRepository;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class EmployeeConstructorMapper {

    @Autowired
    private MailRepository mailRepository;

    public List<EmployeeConstructorDTO> convertListToEmployeeDTO(List<JSONObject> employees) {
        List<EmployeeConstructorDTO> employeeConstructorDTOList = new ArrayList<>();
        for (JSONObject map : employees) {
            String employeeId = (String) map.optString("EmployeeId", null);
            String employeeName = (String) map.optString("EmployeeName", null);
            String employeeRole = (String) map.optString("EmployeeRole", null);
            String employeeCard = (String) map.optString("EmployeeCard", null);

            if (employeeId == null) {
                log.info("Record non adatto per il parsing");
                continue;
            }

            EmployeeConstructorDTO employeeConstructorDTO = new EmployeeConstructorDTO();
            employeeConstructorDTO.setEmployeeCard(employeeCard);
            employeeConstructorDTO.setEmployeeRole(employeeRole);
            employeeConstructorDTO.setEmployeeName(employeeName);
            employeeConstructorDTO.setEmployeeId(employeeId);
            employeeConstructorDTO.setEmail(mailRepository.findEmailFromEmployeeId(employeeId));

            employeeConstructorDTOList.add(employeeConstructorDTO);
        }

        return employeeConstructorDTOList;
    }
}
