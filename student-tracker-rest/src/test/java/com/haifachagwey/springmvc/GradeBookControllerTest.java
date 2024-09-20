package com.haifachagwey.springmvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.haifachagwey.springmvc.models.CollegeStudent;
import com.haifachagwey.springmvc.models.MathGrade;
import com.haifachagwey.springmvc.repository.MathGradesDao;
import com.haifachagwey.springmvc.repository.StudentDao;
import com.haifachagwey.springmvc.service.StudentAndGradeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.Optional;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;

// How can we verify HTTP response (status code, content type and JSON response body)?

// Integration tests

@TestPropertySource("/application-test.properties")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class GradeBookControllerTest {

    private static MockHttpServletRequest request;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private StudentAndGradeService studentAndGradeService;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StudentDao studentDao;

    @Autowired
    private MathGradesDao mathGradeDao;

    @Value("${sql.script.create.student}")
    private String sqlAddStudent;

    @Value("${sql.script.create.math.grade}")
    private String sqlAddMathGrade;

    @Value("${sql.script.create.science.grade}")
    private String sqlAddScienceGrade;

    @Value("${sql.script.create.history.grade}")
    private String sqlAddHistoryGrade;

    @Value("${sql.script.delete.student}")
    private String sqlDeleteStudent;

    @Value("${sql.script.delete.math.grade}")
    private String sqlDeleteMathGrade;

    @Value("${sql.script.delete.science.grade}")
    private String sqlDeleteScienceGrade;

    @Value("${sql.script.delete.history.grade}")
    private String sqlDeleteHistoryGrade;

    public static final MediaType APPLICATION_JSON_UTF8 = MediaType.APPLICATION_JSON;

    @BeforeAll
    public static void setup() {
        request = new MockHttpServletRequest();
        request.setParameter("firstname", "Chad");
        request.setParameter("lastname", "Darby");
        request.setParameter("emailAddress", "chad.darby@luv2code.com");
    }

    @BeforeEach
    public void setupDatabase() {
        jdbc.execute(sqlAddStudent);
        jdbc.execute(sqlAddMathGrade);
        jdbc.execute(sqlAddScienceGrade);
        jdbc.execute(sqlAddHistoryGrade);
    }

    @Test
    public void getGradebookHttpRequest() throws Exception {
        CollegeStudent student = new CollegeStudent();
        student.setFirstname(request.getParameter("firstname"));
        student.setLastname(request.getParameter("lastname"));
        student.setEmailAddress(request.getParameter("emailAddress"));
        entityManager.persist(student);
        entityManager.flush();
        // Send the http request
        mockMvc.perform(MockMvcRequestBuilders.get("/"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                // Verify the size of JSON array
                // JsonPath allows you to access elements of JSON
                // Spring Boot Test starter includes support for JsonPath, no need for you to manually add JsonPath dependency
                // $ The root element to query. Starts all path expressions
                .andExpect(jsonPath("$",hasSize(2)));
    }

    @Test
    public void getStudentHttpRequest() throws Exception {
        Optional<CollegeStudent> student = studentDao.findById(1);
        assertTrue(student.isPresent());
        mockMvc.perform(MockMvcRequestBuilders.get("/studentInformation/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.firstname", is("Eric")))
                .andExpect(jsonPath("$.lastname", is("Roby")))
                .andExpect(jsonPath("$.emailAddress", is("eric.roby@gmail.com")));
    }

    @Test
    public void getNonExistentStudentHttpRequest() throws Exception {
        Optional<CollegeStudent> student = studentDao.findById(0);
        assertFalse(student.isPresent());
        mockMvc.perform(MockMvcRequestBuilders.get("/studentInformation/{id}", 0))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Student or Grade was not found")));
    }


    @Test
    public void createStudentHttpRequest() throws Exception {
        CollegeStudent student = new CollegeStudent();
        student.setFirstname(request.getParameter("firstname"));
        student.setLastname(request.getParameter("lastname"));
        student.setEmailAddress(request.getParameter("emailAddress"));
        mockMvc.perform(post("/")
                .contentType(MediaType.APPLICATION_JSON)
                // ObjectMapper is from Jackson API
                // writeValueAsString will generate a JSON string from the Java object
                .content(objectMapper.writeValueAsString(student)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
        CollegeStudent verifyStudent = studentDao.findByEmailAddress("chad.darby@luv2code.com");
        assertNotNull(verifyStudent, "Student should be valid");
    }

    @Test
    public void deleteStudentHttpRequest() throws Exception {
        assertTrue(studentDao.findById(1).isPresent());
        mockMvc.perform(MockMvcRequestBuilders.delete("/student/{id}",1))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", hasSize(0)));
        assertFalse(studentDao.findById(1).isPresent());
    }

    @Test
    public void deleteNonExistentStudentHttpRequest() throws Exception {
        assertFalse(studentDao.findById(0).isPresent());
        mockMvc.perform(MockMvcRequestBuilders.delete("/student/{id}", 0))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Student or Grade was not found")));
    }

    // Grades

    @Test
    public void createGradeHttpRequest() throws Exception {
        this.mockMvc.perform(post("/grades")
                .contentType(MediaType.APPLICATION_JSON)
                .param("grade", "85.00")
                .param("gradeType", "math")
                .param("studentId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.firstname", is("Eric")))
                .andExpect(jsonPath("$.lastname", is("Roby")))
                .andExpect(jsonPath("$.emailAddress", is("eric.roby@gmail.com")))
                .andExpect(jsonPath("$.studentGrades.mathGradeResults", hasSize(2)));
    }

    // Create a grade for a student id that does not exist
    @Test
    public void createGradeForNonExistentStudentHttpRequest() throws Exception {
        this.mockMvc.perform(post("/grades")
                .contentType(MediaType.APPLICATION_JSON)
                .param("grade", "85.00")
                .param("gradeType", "math")
                .param("studentId", "0"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Student or Grade was not found")));
    }

    // Create a grade for invalid grade type / subject
    @Test
    public void createGradeWhereGradeTypeDoesNotExistHttpRequest() throws Exception{
        this.mockMvc.perform(post("/grades")
                .contentType(MediaType.APPLICATION_JSON)
                .param("grade", "85.00")
                .param("gradeType", "literature")
                .param("studentId", "1"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Student or Grade was not found")));
    }

    @Test
    public void deleteGradeHttpRequest() throws Exception {
        Optional<MathGrade> mathGrade = mathGradeDao.findById(1);
        assertTrue(mathGrade.isPresent());
        mockMvc.perform(MockMvcRequestBuilders.delete("/grades/{id}/{gradeType}", 1, "math"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.firstname", is("Eric")))
                .andExpect(jsonPath("$.lastname", is("Roby")))
                .andExpect(jsonPath("$.emailAddress", is("eric.roby@gmail.com")))
                .andExpect(jsonPath("$.studentGrades.mathGradeResults", hasSize(0)));
    }

    @Test
    public void deleteGradeForNonExistentStudentHttpRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/grades/{id}/gradeType", 2, "history"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Student or Grade was not found")));
    }

    @Test
    public void deleteGradeWhereGradeTypeDoesNotExist() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/grades/{id}/{gradeType}", 1 , "literature"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Student or Grade was not found")));
    }

    @AfterEach
    public void setupAfterTransaction() {
        jdbc.execute(sqlDeleteStudent);
        jdbc.execute(sqlDeleteMathGrade);
        jdbc.execute(sqlDeleteScienceGrade);
        jdbc.execute(sqlDeleteHistoryGrade);
    }
}
