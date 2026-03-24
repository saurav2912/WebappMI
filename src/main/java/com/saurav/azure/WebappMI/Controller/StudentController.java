package com.saurav.azure.WebappMI.Controller;

import com.saurav.azure.WebappMI.entity.Student;
import com.saurav.azure.WebappMI.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public class StudentController {

    @Autowired
    private StudentRepository repo;

    public ResponseEntity<String> createStudent(@RequestBody List<Student> stuList) {
        String message = "";
        try {
            repo.saveAll(stuList);
            message = "Student Insertion succeed";
        } catch (Exception ex) {
            ex.printStackTrace();
            message = "Student Insertion failed";
        }

        return ResponseEntity.ok(message);
    }
}
