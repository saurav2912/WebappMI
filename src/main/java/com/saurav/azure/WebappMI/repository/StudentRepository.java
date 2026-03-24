package com.saurav.azure.WebappMI.repository;

import com.saurav.azure.WebappMI.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface StudentRepository extends JpaRepository<Student, String> {
}
