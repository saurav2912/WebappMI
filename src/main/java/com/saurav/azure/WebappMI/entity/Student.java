package com.saurav.azure.WebappMI.entity;

import jakarta.persistence.*;

@Entity
@Table(name="Student")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private String id;
    @Column(name = "FirstName")
    private String fname;
    @Column(name = "LastName")
    private String lname;
    @Column(name = "Class")
    private String standard;
    @Column(name = "RollNo")
    private int rollNo;
}
