package com.salah.csvprocessor.model;

import lombok.AllArgsConstructor;

import java.time.LocalDate;


@AllArgsConstructor
public class Employee {
    private Long id;
    private String name;
    private double salary;
    private LocalDate joinedDate;
    private String role;
    private double projectCompletion;


    public synchronized void updateSalary(double percentage){
        salary =  salary + (salary * percentage);
    }

    @Override
    public String toString(){
        return id + ": Employee: " + name + " Role: " + role + " Joined Date: " + joinedDate + " Salary: " + salary + " Project completion %: " + projectCompletion;
    }

}
