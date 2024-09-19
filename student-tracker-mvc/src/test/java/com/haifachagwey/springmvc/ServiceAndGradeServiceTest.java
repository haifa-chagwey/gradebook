package com.haifachagwey.springmvc;

import com.haifachagwey.springmvc.models.*;
import com.haifachagwey.springmvc.repository.HistoryGradeDao;
import com.haifachagwey.springmvc.repository.MathGradeDao;
import com.haifachagwey.springmvc.repository.ScienceGradeDao;
import com.haifachagwey.springmvc.repository.StudentDao;
import com.haifachagwey.springmvc.service.StudentAndGradeService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
@TestPropertySource("/application-test.properties")
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServiceAndGradeServiceTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private StudentAndGradeService studentService;

    @Autowired
    private StudentDao studentDao;

    @Autowired
    private MathGradeDao mathGradeDao;

    @Autowired
    private ScienceGradeDao scienceGradeDao;

    @Autowired
    private HistoryGradeDao historyGradeDao;

    @Value("${sql.scripts.create.student}")
    private String sqlAddStudent;

    @Value("${sql.scripts.create.math.grade}")
    private String sqlAddMathStudent;

    @Value("${sql.scripts.create.science.grade}")
    private String sqlAddScienceStudent;

    @Value("${sql.scripts.create.history.grade}")
    private String sqlAddHistoryStudent;

    @Value("${sql.scripts.delete.student}")
    private String sqlDeleteStudent;

    @Value("${sql.scripts.delete.math.grade}")
    private String sqlDeleteMathStudent;

    @Value("${sql.scripts.delete.science.grade}")
    private String sqlDeleteScienceStudent;

    @Value("${sql.scripts.delete.history.grade}")
    private String sqlDeleteHistoryStudent;

    @BeforeEach
    public void setupDatabase(){
        jdbc.execute(sqlAddStudent);
        jdbc.execute(sqlAddMathStudent);
        jdbc.execute(sqlAddScienceStudent);
        jdbc.execute(sqlAddHistoryStudent);
    }

    @Test
    public void getGradeBookService(){
        Gradebook gradebook = studentService.getGradebook();
        List<CollegeStudent> collegeStudents = new ArrayList<>();
        for (CollegeStudent collegeStudent : gradebook.getStudents()) {
            collegeStudents.add(collegeStudent);
        }
        assertEquals(1, collegeStudents.size());
    }

    @Test
    @Order(0)
    public void createStudentService() {
        studentService.createStudent("test","test", "test@gmail.com");
        CollegeStudent student = studentDao.findByEmailAddress("test@gmail.com");
        assertEquals("test@gmail.com", student.getEmailAddress(),"Find by email");
    }
    @Test
    @Order(1)
    public void checkIfStudentExistService() {
        assertTrue(studentService.checkIfStudentExist(1), "Should returns true because student exist");
        assertFalse(studentService.checkIfStudentExist(0), "Should returns false because student does not exist");
    }

    @Test
    // Delete the student created during setupDatabase function (@Before each)
    public void deleteStudentService() {
        Optional<CollegeStudent> deletedCollegeStudent = studentDao.findById(1);
        Optional<MathGrade> deletedMathGrade = mathGradeDao.findById(1);
        Optional<ScienceGrade> deletedScienceGrade = scienceGradeDao.findById(1);
        Optional<HistoryGrade> deletedHistoryGrade = historyGradeDao.findById(1);

        assertTrue(deletedCollegeStudent.isPresent(), "Should returns True");
        assertTrue(deletedMathGrade.isPresent(), "Should returns True");
        assertTrue(deletedScienceGrade.isPresent(), "Should returns True");
        assertTrue(deletedHistoryGrade.isPresent(), "Should returns True");

        studentService.deleteStudent(1);

        deletedCollegeStudent = studentDao.findById(1);
        deletedMathGrade = mathGradeDao.findById(1);
        deletedScienceGrade = scienceGradeDao.findById(1);
        deletedHistoryGrade = historyGradeDao.findById(1);

        assertFalse(deletedCollegeStudent.isPresent(), "Should return false");
        assertFalse(deletedMathGrade.isPresent(),"Should return false");
        assertFalse(deletedScienceGrade.isPresent(),"Should return false");
        assertFalse(deletedHistoryGrade.isPresent(),"Should return false");
    }



    @Test
    public void createGradeService() {
        // Create the grade for the syudent that we have created during SetupDatabase funtion
        assertTrue(studentService.createGrade(80.50, 1, "math"));
        assertTrue(studentService.createGrade(80.50, 1, "science"));
        assertTrue(studentService.createGrade(80.50, 1, "history"));
        // Get all grades with studentId
        Iterable<MathGrade> mathGrades = mathGradeDao.findGradeByStudentId(1);
        Iterable<ScienceGrade> scienceGrades = scienceGradeDao.findGradeByStudentId(1);
        Iterable<HistoryGrade> historyGrades = historyGradeDao.findGradeByStudentId(1);
        // Verify there is grades
//        assertTrue(mathGrades.iterator().hasNext(), "Student has math grades");
//        assertTrue(scienceGrades.iterator().hasNext());
//        assertTrue(historyGrades.iterator().hasNext());
        // Cast Iterable to connection this will allow us to access the size of the collection
        assertTrue(((Collection<MathGrade>) mathGrades).size() == 2, "Student has math grades");
        assertTrue(((Collection<ScienceGrade>) scienceGrades).size() == 2, "Student has science grades");
        assertTrue(((Collection<HistoryGrade>) historyGrades).size() == 2, "Student has history grades");
    }

    @Test
    public void createGradeServiceReturnFalse() {
        // Outside of range 0 - 100
        assertFalse(studentService.createGrade(105,1, "math"));
        assertFalse(studentService.createGrade(-5,1, "math"));
        // Invalid student id
        assertFalse(studentService.createGrade(-5,2, "math"));
        // Invalid Subject
        assertFalse(studentService.createGrade(-5,2, "literature"));
    }

    @Test
    public void deleteGradeService() {
        assertEquals(1, studentService.deleteGrade(1, "math"), "Returns id after delete");
        assertEquals(1, studentService.deleteGrade(1, "science"), "Returns id after delete");
        assertEquals(1, studentService.deleteGrade(1, "history"), "Returns id after delete");
    }

    @Test
    public void deleteGradeServiceReturnStudentIdOfZero(){
        assertEquals(0, studentService.deleteGrade(0, "science"), "No student should have 0 id");
        assertEquals(0, studentService.deleteGrade(0, "literature"), "No student should have a literature class");
    }

    @Test
    public void studentInformation() {
        GradebookCollegeStudent gradebookCollegeStudent = studentService.studentInformation(1);

        assertNotNull(gradebookCollegeStudent);
        assertEquals(1, gradebookCollegeStudent.getId());
        assertEquals("Haifa", gradebookCollegeStudent.getFirstname());
        assertEquals("Chagwey", gradebookCollegeStudent.getLastname());
        assertEquals("haifachagwey@gmail.com", gradebookCollegeStudent.getEmailAddress());
        assertTrue(gradebookCollegeStudent.getStudentGrades().getMathGradeResults().size() == 1);
        assertTrue(gradebookCollegeStudent.getStudentGrades().getScienceGradeResults().size()== 1);
        assertTrue(gradebookCollegeStudent.getStudentGrades().getHistoryGradeResults().size()== 1);
    }

    @Test
    public void studentInformationServiceReturnNull() {
        GradebookCollegeStudent gradebookCollegeStudent = studentService.studentInformation(0);
        assertNull(gradebookCollegeStudent);
    }

    @AfterEach
    public void setupAfterTransaction() {
        jdbc.execute(sqlDeleteStudent);
        jdbc.execute(sqlDeleteMathStudent);
        jdbc.execute(sqlDeleteScienceStudent);
        jdbc.execute(sqlDeleteHistoryStudent);
    }

}
