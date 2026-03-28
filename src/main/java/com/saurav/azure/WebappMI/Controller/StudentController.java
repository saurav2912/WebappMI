package com.saurav.azure.WebappMI.Controller;

import com.saurav.azure.WebappMI.entity.Student;
import com.saurav.azure.WebappMI.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class StudentController {

    @Autowired
    private StudentRepository repo;

    @PostMapping("/students")
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

    @GetMapping("/students")
    public ResponseEntity<List<Student>> getAllStudent() {
        List<Student> students = null;
        try {
            students = repo.findAll();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return ResponseEntity.ok(students);
    }

    @GetMapping("/student/{id}")
    public ResponseEntity<Student> getStudentById(@PathVariable("id") String id ) {

        Student student = null;
        try {
            student =    repo.findById(id).orElseGet(null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return ResponseEntity.ok(student);
    }
}
