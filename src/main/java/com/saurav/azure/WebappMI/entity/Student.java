package com.saurav.azure.WebappMI.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name="Student")
@Getter
@Setter
@NoArgsConstructor
public class Student implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    @Column(name = "FirstName")
    private String fname;
    @Column(name = "LastName")
    private String lname;
    @Column(name = "Class")
    private String standard;
    @Column(name = "RollNo")
    private int rollNo;
}
