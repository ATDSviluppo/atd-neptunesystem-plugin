package com.MailModule.Repository;

import com.MailModule.Entity.EmployeeMailConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.List;

@Repository
public interface MailRepository extends JpaRepository<EmployeeMailConstructor, String> {
    @Query("SELECT e FROM EmployeeMailConstructor e WHERE e.email IS NOT NULL AND e.email <> ''")
    List<EmployeeMailConstructor> findByEmailNotNullOrEmpty();

    @Query("SELECT email FROM EmployeeMailConstructor e WHERE e.employeeId = :employeeId")
    String findEmailFromEmployeeId(@Param("employeeId") String employeeId);

    @Modifying
    @Transactional
    @Query("UPDATE EmployeeMailConstructor e SET e.email = :email WHERE e.employeeId = :employeeId")
    int setupEmail(@Param("email") String email, @Param("employeeId") String employeeId);

    @Query(value = """
            SELECT DISTINCT e.email
            FROM Employee e
            INNER JOIN EmployeeChoice ec
                ON e.employeeId = ec.employeeId
            INNER JOIN Devices d
                ON ec.deviceGuid = d.deviceGuid
            WHERE d.expirationDate = DATE_ADD(
                :currentDate,
                INTERVAL :daysBeforeExpiration DAY
            ) AND e.email IS NOT NULL AND e.email <> ''
            AND d.status = 'OK' AND d.holder = true
            """, nativeQuery = true)
    List<String> getEmailToSendExpirationAdvice(
            @Param("daysBeforeExpiration") int daysBeforeExpiration,
            @Param("currentDate") Date currentDate
    );
}
