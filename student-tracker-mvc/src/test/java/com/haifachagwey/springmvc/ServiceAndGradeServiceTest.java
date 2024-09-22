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
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.jdbc.SqlGroup;

import java.util.Collection;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

//  How can we test Spring MVC web controllers?
//  How we can create HTTP request and send to the controller?
//  How can we verify HTTP response (status code, view name, model attributes)?

// In Spring Boot, if an embedded database is listed as a dependency then Spring Boot will auto-configure the database connection
// Our project has H2 as a dependency, so Spring Boot will auto-configure a connection to the embedded H2 database

@TestPropertySource("/application-test.properties")
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServiceAndGradeServiceTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private StudentAndGradeService studentAndGradeService;

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

    @SqlGroup({ @Sql(scripts = "/insertData.sql", config = @SqlConfig(commentPrefix = "`")),
            @Sql("/overrideData.sql"),
            @Sql("/insertGrade.sql")})
    @Test
    public void getGradebookService() {
        Gradebook gradebook = studentAndGradeService.getGradebook();
        Gradebook gradebookTest = new Gradebook();
        for (GradebookCollegeStudent student : gradebook.getStudents()) {
            gradebookTest.getStudents().add(student);
        }
        assertEquals(5, gradebookTest.getStudents().size());
    }

    @Test
    @Order(0)
    public void createStudentService() {
        studentAndGradeService.createStudent("test","test", "test@gmail.com");
        CollegeStudent student = studentDao.findByEmailAddress("test@gmail.com");
        assertEquals("test@gmail.com", student.getEmailAddress(),"Find by email");
    }
    @Test
    @Order(1)
    public void checkIfStudentExistService() {
        assertTrue(studentAndGradeService.checkIfStudentExist(1), "Should returns true because student exist");
        assertFalse(studentAndGradeService.checkIfStudentExist(0), "Should returns false because student does not exist");
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

        studentAndGradeService.deleteStudent(1);

        deletedCollegeStudent = studentDao.findById(1);
        deletedMathGrade = mathGradeDao.findById(1);
        deletedScienceGrade = scienceGradeDao.findById(1);
        deletedHistoryGrade = historyGradeDao.findById(1);

        assertFalse(deletedCollegeStudent.isPresent(), "Should return false");
        assertFalse(deletedMathGrade.isPresent(),"Should return false");
        assertFalse(deletedScienceGrade.isPresent(),"Should return false");
        assertFalse(deletedHistoryGrade.isPresent(),"Should return false");
    }

    // Grades

    @Test
    public void createGradeService() {
        // Create the grade for the student that we have created during SetupDatabase function
        assertTrue(studentAndGradeService.createGrade(80.50, 1, "math"));
        assertTrue(studentAndGradeService.createGrade(80.50, 1, "science"));
        assertTrue(studentAndGradeService.createGrade(80.50, 1, "history"));
        // Get grades by student i  d
        Iterable<MathGrade> mathGrades = mathGradeDao.findGradeByStudentId(1);
        Iterable<ScienceGrade> scienceGrades = scienceGradeDao.findGradeByStudentId(1);
        Iterable<HistoryGrade> historyGrades = historyGradeDao.findGradeByStudentId(1);
        // Cast Iterable to connection this will allow us to access the size of the collection
        assertTrue(((Collection<MathGrade>) mathGrades).size() == 2, "Student has math grades");
        assertTrue(((Collection<ScienceGrade>) scienceGrades).size() == 2, "Student has science grades");
        assertTrue(((Collection<HistoryGrade>) historyGrades).size() == 2, "Student has history grades");
    }

    @Test
    public void createInvalidGradeService() {
        // Outside of range 0 - 100
        assertFalse(studentAndGradeService.createGrade(105,1, "math"));
        assertFalse(studentAndGradeService.createGrade(-5,1, "math"));
        // Invalid student id
        assertFalse(studentAndGradeService.createGrade(55,2, "math"));
        // Invalid Subject
        assertFalse(studentAndGradeService.createGrade(45,2, "literature"));
    }

    @Test
    public void deleteGradeService() {
        assertEquals(1, studentAndGradeService.deleteGrade(1, "math"), "Returns id after delete");
        assertEquals(1, studentAndGradeService.deleteGrade(1, "science"), "Returns id after delete");
        assertEquals(1, studentAndGradeService.deleteGrade(1, "history"), "Returns id after delete");
    }

    @Test
    public void deleteGradeForNonExistentStudentService(){
        assertEquals(0, studentAndGradeService.deleteGrade(0, "science"), "No student should have 0 id");
        assertEquals(0, studentAndGradeService.deleteGrade(1, "literature"), "No student should have a literature class");
    }

    @Test
    public void getStudentService() {
        GradebookCollegeStudent gradebookCollegeStudent = studentAndGradeService.getStudent(1);
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
    public void getNonExistentStudentService() {
        GradebookCollegeStudent gradebookCollegeStudent = studentAndGradeService.getStudent(0);
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
