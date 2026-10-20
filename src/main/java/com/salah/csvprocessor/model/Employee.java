package com.salah.csvprocessor.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;


@AllArgsConstructor
@Getter
public class Employee {
    private Long id;
    private String name;
    private double salary;
    private LocalDate joinedDate;
    private String role;
    private double projectCompletion;


    public synchronized void updateSalary(){
        double percentage = 1;
        switch (role){
            case "Employee" -> {percentage = 1.01;}
            case "Manager" -> {percentage = 1.02;}
            case "Director" -> {percentage = 1.05;}
            default -> {
            }
        }
        if (projectCompletion > 0.8){
            percentage = (percentage - 1) * 1.5 + 1;
        }
        int i = getEmploymentTime(this);
        while (i > 0){
            salary = salary * percentage;
            i--;
        }

    }

    @Override
    public String toString(){
        return id + ": Employee: " + name + " Role: " + role + " Joined Date: " + joinedDate + " Salary: " + salary + " Project completion: %" + (int) (projectCompletion*100);
    }

    public int getEmploymentTime(Employee emp){
        return (int) ChronoUnit.YEARS.between(emp.getJoinedDate(),LocalDate.now());
    }

}
