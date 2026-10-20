package com.salah.csvprocessor.service;

import com.salah.csvprocessor.model.Employee;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class CsvService {

    private final ConcurrentProcessService concurrentProcessService;

    public CsvService(ConcurrentProcessService concurrentProcessService) {
        this.concurrentProcessService = concurrentProcessService;
    }

    public ResponseEntity<?> processCsv(MultipartFile file) throws IOException {
        List<Employee> employees = parseCsv(file);

        concurrentProcessService.processEmployees(employees);
        for (Employee e : employees){
            System.out.println(e);
        }
        return new ResponseEntity<>("NICE", HttpStatus.OK);
    }

    public List<Employee> parseCsv(MultipartFile file) throws IOException {
        List<Employee> employess = new ArrayList<>();
        String line;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            while ((line = br.readLine()) != null){
                String[] emp = line.split(",");

                System.out.println(Arrays.toString(emp));

                employess.add(new Employee(
                        Long.parseLong(emp[0]),
                        emp[1],
                        Double.parseDouble(emp[2]),
                        LocalDate.parse(emp[3]),
                        emp[4],
                        Double.parseDouble(emp[5])
                ));
            }
        } catch (IOException | RuntimeException e) {
                throw new RuntimeException("Failed reading or parsing CSV file: " + e.getMessage(), e);
        }
        return employess;
    }
}
