package com.salah.csvprocessor.service;

import com.salah.csvprocessor.model.Employee;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class ConcurrentProcessService {

    public void processEmployees(List<Employee> employees){

        ExecutorService executor = Executors.newFixedThreadPool(4);

        for (Employee e : employees){
            executor.submit(() -> {
                processEmployee(e);
            });
        }
        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.MINUTES);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Processing interrupted", ex);
        }
    }

    public synchronized void processEmployee(Employee emp){
        if (!validIncrement(emp)){
            System.out.println(emp.getId() + " " + emp.getJoinedDate() + " " + emp.getProjectCompletion());
            return;
        }
        emp.updateSalary();
        System.out.println(Thread.currentThread().getName() + " processed employee ID: " + emp.getId());
    }

    public boolean validIncrement(Employee emp){
        if (emp.getProjectCompletion() < 0.6){
            return false;
        } else {
            return true;
        }
    }



}
