package com.haifachagwey.springmvc.service;

import com.haifachagwey.springmvc.models.*;
import com.haifachagwey.springmvc.repository.HistoryGradeDao;
import com.haifachagwey.springmvc.repository.MathGradeDao;
import com.haifachagwey.springmvc.repository.ScienceGradeDao;
import com.haifachagwey.springmvc.repository.StudentDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class StudentAndGradeService {

    @Autowired
    private StudentDao studentDao;

    @Autowired
    private MathGradeDao mathGradeDao;

    @Autowired
    @Qualifier("mathGrades")
    private MathGrade mathGrade;

    @Autowired
    private ScienceGradeDao scienceGradeDao;

    @Autowired
    @Qualifier("scienceGrades")
    private ScienceGrade scienceGrade;

    @Autowired
    private HistoryGradeDao historyGradeDao;

    @Autowired
    @Qualifier("historyGrades")
    private HistoryGrade historyGrade;

    @Autowired
    private StudentGrades studentGrades;

    public void createStudent(String firstName, String lastName, String emailAddress) {
        CollegeStudent student = new CollegeStudent(firstName, lastName, emailAddress);
        studentDao.save(student);
    }

    public boolean checkIfStudentExist(int id) {
        Optional<CollegeStudent> student = studentDao.findById(id);
        if (student.isPresent()) {
            return true;
        }
        return false;
    }

    public void deleteStudent(int id) {
        if (checkIfStudentExist(id)) {
            studentDao.deleteById(id);
            mathGradeDao.deleteByStudentId(id);
            scienceGradeDao.deleteByStudentId(id);
            historyGradeDao.deleteByStudentId(id);
        }
    }

    public Gradebook getGradebook () {

        Iterable<CollegeStudent> collegeStudents = studentDao.findAll();

        Iterable<MathGrade> mathGrades = mathGradeDao.findAll();

        Iterable<ScienceGrade> scienceGrades = scienceGradeDao.findAll();

        Iterable<HistoryGrade> historyGrades = historyGradeDao.findAll();

        Gradebook gradebook = new Gradebook();

        for (CollegeStudent collegeStudent : collegeStudents) {
            List<Grade> mathGradesPerStudent = new ArrayList<>();
            List<Grade> scienceGradesPerStudent = new ArrayList<>();
            List<Grade> historyGradesPerStudent = new ArrayList<>();

            for (MathGrade grade : mathGrades) {
                if (grade.getStudentId() == collegeStudent.getId()) {
                    mathGradesPerStudent.add(grade);
                }
            }
            for (ScienceGrade grade : scienceGrades) {
                if (grade.getStudentId() == collegeStudent.getId()) {
                    scienceGradesPerStudent.add(grade);
                }
            }

            for (HistoryGrade grade : historyGrades) {
                if (grade.getStudentId() == collegeStudent.getId()) {
                    historyGradesPerStudent.add(grade);
                }
            }

            studentGrades.setMathGradeResults(mathGradesPerStudent);
            studentGrades.setScienceGradeResults(scienceGradesPerStudent);
            studentGrades.setHistoryGradeResults(historyGradesPerStudent);

            GradebookCollegeStudent gradebookCollegeStudent = new GradebookCollegeStudent(collegeStudent.getId(), collegeStudent.getFirstname(), collegeStudent.getLastname(),
                    collegeStudent.getEmailAddress(), studentGrades);

            gradebook.getStudents().add(gradebookCollegeStudent);
        }

        return gradebook;
    }

    public boolean createGrade(double grade, int studentId, String gradeType) {
        if (!checkIfStudentExist(studentId)) {
            return false;
        }
        if (grade >= 0 && grade <= 100) {
            if (gradeType.equals("math")) {
                mathGrade.setGrade(grade);
                mathGrade.setStudentId(studentId);
                mathGradeDao.save(mathGrade);
                return true;
            }
            if (gradeType.equals("science")) {
                scienceGrade.setGrade(grade);
                scienceGrade.setStudentId(studentId);
                scienceGradeDao.save(scienceGrade);
                return true;
            }
            if (gradeType.equals("history")) {
                historyGrade.setGrade(grade);
                historyGrade.setStudentId(studentId);
                historyGradeDao.save(historyGrade);
                return true;
            }
        }
        return false;
    }

    public int deleteGrade(int gradeId, String gradeType) {
        int studentId = 0;
        if(gradeType.equals("math")){
            Optional<MathGrade> grade = mathGradeDao.findById(gradeId);
            if(!grade.isPresent()) {
                return studentId;
            }
            studentId = grade.get().getStudentId();
            mathGradeDao.deleteById(gradeId);
        }
        if(gradeType.equals("science")){
            Optional<ScienceGrade> grade = scienceGradeDao.findById(gradeId);
            if(!grade.isPresent()) {
                return studentId;
            }
            studentId = grade.get().getStudentId();
            scienceGradeDao.deleteById(gradeId);
        }
        if(gradeType.equals("history")){
            Optional<HistoryGrade> grade = historyGradeDao.findById(gradeId);
            if(!grade.isPresent()) {
                return studentId;
            }
            studentId = grade.get().getStudentId();
            historyGradeDao.deleteById(gradeId);
        }
        return  studentId;
    }

    // Get student information by its id
    public GradebookCollegeStudent getStudent(int studentId) {
        if (!checkIfStudentExist(studentId)) {
            return null;
        }
        Optional<CollegeStudent> student = studentDao.findById(studentId);
        Iterable<MathGrade> mathGrades = mathGradeDao.findGradeByStudentId(studentId);
        Iterable<ScienceGrade> scienceGrades = scienceGradeDao.findGradeByStudentId(studentId);
        Iterable<HistoryGrade> historyGrades = historyGradeDao.findGradeByStudentId(studentId);

        List<Grade> mathGradeList = new ArrayList<>();
        mathGrades.forEach(mathGradeList::add);

        List<Grade> scienceGradeList = new ArrayList<>();
        scienceGrades.forEach(scienceGradeList::add);

        List<Grade> historyGradeList = new ArrayList<>();
        historyGrades.forEach(historyGradeList::add);

        studentGrades.setMathGradeResults(mathGradeList);
        studentGrades.setScienceGradeResults(scienceGradeList);
        studentGrades.setHistoryGradeResults(historyGradeList);

        GradebookCollegeStudent gradebookCollegeStudent = new GradebookCollegeStudent(student.get().getId(),
                student.get().getFirstname(), student.get().getLastname(), student.get().getEmailAddress(), studentGrades);
        return gradebookCollegeStudent;
    }

    public void configureStudentInformationModel(int studentId, Model model) {

        GradebookCollegeStudent studentEntity = getStudent(studentId);

        model.addAttribute("student", studentEntity);
        if (studentEntity.getStudentGrades().getMathGradeResults().size() > 0) {
            model.addAttribute("mathAverage", studentEntity.getStudentGrades().findGradePointAverage(
                    studentEntity.getStudentGrades().getMathGradeResults()
            ));
        } else {
            model.addAttribute("mathAverage", "N/A");
        }

        if (studentEntity.getStudentGrades().getScienceGradeResults().size() > 0) {
            model.addAttribute("scienceAverage", studentEntity.getStudentGrades().findGradePointAverage(
                    studentEntity.getStudentGrades().getScienceGradeResults()
            ));
        } else {
            model.addAttribute("scienceAverage", "N/A");
        }

        if (studentEntity.getStudentGrades().getHistoryGradeResults().size() > 0) {
            model.addAttribute("historyAverage", studentEntity.getStudentGrades().findGradePointAverage(
                    studentEntity.getStudentGrades().getHistoryGradeResults()
            ));
        } else {
            model.addAttribute("historyAverage", "N/A");
        }
    }


}
