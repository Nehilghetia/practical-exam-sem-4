package com.jenkins.practical;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/students")
public class StudentController {

    private List<Student> students = new ArrayList<>();
    private long currentId = 1;

    // Create
    @PostMapping
    public Student createStudent(@RequestBody Student student) {
        student.setId(currentId++);
        students.add(student);
        return student;
    }

    // Read all
    @GetMapping
    public List<Student> getAllStudents() {
        return students;
    }

    // Read by ID
    @GetMapping("/{id}")
    public Student getStudentById(@PathVariable Long id) {
        return students.stream()
                .filter(s -> s.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    // Update
    @PutMapping("/{id}")
    public Student updateStudent(@PathVariable Long id, @RequestBody Student updatedStudent) {
        for (int i = 0; i < students.size(); i++) {
            Student student = students.get(i);
            if (student.getId().equals(id)) {
                updatedStudent.setId(id);
                students.set(i, updatedStudent);
                return updatedStudent;
            }
        }
        return null;
    }

    // Delete
    @DeleteMapping("/{id}")
    public String deleteStudent(@PathVariable Long id) {
        boolean removed = students.removeIf(s -> s.getId().equals(id));
        if (removed) {
            return "Student deleted successfully.";
        } else {
            return "Student not found.";
        }
    }
}
