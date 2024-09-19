package com.haifachagwey.springmvc.controller;

import com.haifachagwey.springmvc.models.Gradebook;
import com.haifachagwey.springmvc.models.*;
import com.haifachagwey.springmvc.service.StudentAndGradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class GradebookController {

    @Autowired
    Gradebook gradebook;

    @Autowired
    private StudentAndGradeService studentAndGradeService;

    @GetMapping("/")
    public String getGradebook(Model model) {
        Gradebook gradebook = studentAndGradeService.getGradebook();
        model.addAttribute("students", gradebook.getStudents());
        return "index";
    }

    @PostMapping("/")
    public String createStudent(@ModelAttribute("student") CollegeStudent student, Model model) {
        studentAndGradeService.createStudent(student.getFirstname(), student.getLastname(), student.getEmailAddress());
        gradebook = studentAndGradeService.getGradebook();
        model.addAttribute("students", gradebook.getStudents());
        return "index";
    }

    @GetMapping("/delete/student/{id}")
    public String deleteStudent(@PathVariable int id, Model model) {
        if (!studentAndGradeService.checkIfStudentExist(id)) {
            return "error";
        }
        studentAndGradeService.deleteStudent(id);
        Gradebook gradebook = studentAndGradeService.getGradebook();
        model.addAttribute("students", gradebook.getStudents());
        return "index";
    }

    @GetMapping("/{id}")
    public String getStudent(@PathVariable int id, Model model) {
        if (!studentAndGradeService.checkIfStudentExist(id)) {
            return "error";
        }
      	studentAndGradeService.configureStudentInformationModel(id, model);
        return "studentInformation";
    }

    // Student grades

	@PostMapping("/grades")
	public String createGrade(@RequestParam("grade") double grade,
							  @RequestParam("gradeType") String gradeType,
							  @RequestParam("studentId") int studentId,
							  Model model) {
		if (!studentAndGradeService.checkIfStudentExist(studentId)) {
			return "error";
		}
		boolean success = studentAndGradeService.createGrade(grade, studentId, gradeType);
		if (!success){
			return "error";
		}
		studentAndGradeService.configureStudentInformationModel(studentId, model);
		return "studentInformation";
	}

    @GetMapping("/grades/{id}/{gradeType}")
    public String deleteGrade(@PathVariable int id, @PathVariable String gradeType, Model model) {
        int studentId = studentAndGradeService.deleteGrade(id, gradeType);
        if (studentId == 0) {
            return "error";
        }
        studentAndGradeService.configureStudentInformationModel(studentId, model);
        return "studentInformation";
    }

}
