package com.salah.csvprocessor.model;

import lombok.AllArgsConstructor;


@AllArgsConstructor
public class Employee {
    private String name;
    private int joinedDate;
    private double salary;
    private String role;
    private double projectCompletion;

    public synchronized void updateSalary(double percentage){
        salary =  salary + (salary * percentage);
    }

    @Override
    public String toString(){
        return "Employee: " + name + " Role: " + role + " Joined Date: " + joinedDate + " Salary: " + salary + " Project completion %: " + projectCompletion;
    }

}
