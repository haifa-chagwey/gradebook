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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.ModelAndViewAssert;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Integration tests

@AutoConfigureMockMvc
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestPropertySource("/application-test.properties")
public class GradebookControllerTest {

    private static MockHttpServletRequest request;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private MockMvc mockMvc;

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

    @BeforeAll
    // Per the JUnit Docs, @BeforeAll methods must be declared as static
    // also must be public and return void
    public static void setup() {
        request = new MockHttpServletRequest();
        request.setParameter("firstname", "Eric");
        request.setParameter("lastname", "Roby");
        request.setParameter("emailAddress", "eric.roby@gmail.com");
    }

    @BeforeEach
    public void beforeEach(){
        jdbc.execute(sqlAddStudent);
        jdbc.execute(sqlAddMathStudent);
        jdbc.execute(sqlAddScienceStudent);
        jdbc.execute(sqlAddHistoryStudent);
    }

    @Test
    @Order(0)
    public void getGradebookHttpRequest() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/"))
                .andExpect(status().isOk()).andReturn();
        ModelAndView modelAndView = mvcResult.getModelAndView();
        ModelAndViewAssert.assertViewName(modelAndView, "index");
        List<GradebookCollegeStudent> studentList = (List<GradebookCollegeStudent>) modelAndView.getModel().get("students");
        assertEquals(1, studentList.size());
        assertEquals("Haifa", studentList.get(0).getFirstname());
    }

    @Test
    @Order(1)
    public void createStudentHttpRequest() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(post("/")
                .contentType(MediaType.APPLICATION_JSON)
                .param("firstname", request.getParameterValues("firstname"))
                .param("lastname", request.getParameterValues("lastname"))
                .param("emailAddress", request.getParameterValues("emailAddress")))
                .andExpect(status().isOk()).andReturn();
        ModelAndView modelAndView = mvcResult.getModelAndView();
        ModelAndViewAssert.assertViewName(modelAndView, "index");
        CollegeStudent verifyStudent = studentDao.findByEmailAddress("eric.roby@gmail.com");
        assertNotNull(verifyStudent, "Student should be found after create");
    }

    @Test
    @Order(2)
    public void deleteStudentHttpRequest() throws Exception {
        assertTrue(studentDao.findById(1).isPresent());
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .get("/delete/student/{id}", 1))
                .andExpect(status().isOk()).andReturn();
        ModelAndView modelAndView = mvcResult.getModelAndView();
        ModelAndViewAssert.assertViewName(modelAndView, "index");
        assertFalse(studentDao.findById(1).isPresent());
    }

//    The delete student function that we have implemented should return an error page when the student does not exist
    @Test
    @Order(3)
    public void deleteNonExistentStudentsHttpRequest() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .get("/delete/student/{id}", 0))
                .andExpect(status().isOk()).andReturn();
        ModelAndView modelAndView = mvcResult.getModelAndView();
        ModelAndViewAssert.assertViewName(modelAndView, "error");
    }

    @Test
    @Order(4)
    public void getStudentHttpRequest() throws Exception {
        assertTrue(studentDao.findById(1).isPresent());
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/{id}", 1))
                .andExpect(status().isOk()).andReturn();
        ModelAndView modelAndView = mvcResult.getModelAndView();
        ModelAndViewAssert.assertViewName(modelAndView, "studentInformation");
    }

    @Test
    @Order(5)
    public void getNonExistentStudentRequest() throws Exception {
        assertFalse(studentDao.findById(0).isPresent());
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/{id}", 0))
                .andExpect(status().isOk()).andReturn();
        ModelAndView modelAndView = mvcResult.getModelAndView();
        ModelAndViewAssert.assertViewName(modelAndView, "error");
    }

    //    ------------------------------------------------------------------------

    @Test
    @Order(6)
    public void createMathGradeHttpRequest() throws Exception {
        // Check that the student exists
        assertTrue(studentDao.findById(1).isPresent());
        GradebookCollegeStudent student = studentAndGradeService.getStudent(1);
        assertEquals(1, student.getStudentGrades().getMathGradeResults().size());

        MvcResult mvcResult = this.mockMvc.perform(post("/grades")
                .contentType(MediaType.APPLICATION_JSON)
                .param("grade", "85.00")
                .param("gradeType", "math")
                .param("studentId", "1"))
                .andExpect(status().isOk()).andReturn();
        ModelAndView modelAndView = mvcResult.getModelAndView();
        ModelAndViewAssert.assertViewName(modelAndView, "studentInformation");
        student = studentAndGradeService.getStudent(1);
        assertEquals(2, student.getStudentGrades().getMathGradeResults().size());
    }

    @Test
    @Order(7)
    public void createScienceGradeHttpRequest() throws Exception {
        // Check that the student exists
        assertTrue(studentDao.findById(1).isPresent());
        GradebookCollegeStudent student = studentAndGradeService.getStudent(1);
        assertEquals(1, student.getStudentGrades().getScienceGradeResults().size());

        MvcResult mvcResult = this.mockMvc.perform(post("/grades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("grade", "85.00")
                        .param("gradeType", "science")
                        .param("studentId", "1"))
                .andExpect(status().isOk()).andReturn();
        ModelAndView modelAndView = mvcResult.getModelAndView();
        ModelAndViewAssert.assertViewName(modelAndView, "studentInformation");
        student = studentAndGradeService.getStudent(1);
        assertEquals(2, student.getStudentGrades().getScienceGradeResults().size());
    }

    @Test
    @Order(8)
    public void createHistoryGradeHttpRequest() throws Exception {
        // Check that the student exists
        assertTrue(studentDao.findById(1).isPresent());
        GradebookCollegeStudent student = studentAndGradeService.getStudent(1);
        assertEquals(1, student.getStudentGrades().getHistoryGradeResults().size());

        MvcResult mvcResult = this.mockMvc.perform(post("/grades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("grade", "85.00")
                        .param("gradeType", "history")
                        .param("studentId", "1"))
                .andExpect(status().isOk()).andReturn();
        ModelAndView modelAndView = mvcResult.getModelAndView();
        ModelAndViewAssert.assertViewName(modelAndView, "studentInformation");
        student = studentAndGradeService.getStudent(1);
        assertEquals(2, student.getStudentGrades().getHistoryGradeResults().size());
    }

//    We have to verify that the function that we have implemented should return an error page when the student does not exist
    @Test
    @Order(9)
    public void createGradeForNonExistentStudentHttpRequest() throws Exception {
        assertFalse(studentDao.findById(0).isPresent());
        MvcResult mvcResult = this.mockMvc.perform(post("/grades")
                .contentType(MediaType.APPLICATION_JSON)
                .param("grade", "85.00")
                .param("gradeType", "history")
                .param("studentId", "0"))
                .andExpect(status().isOk()).andReturn();
        ModelAndView modelAndView = mvcResult.getModelAndView();
        ModelAndViewAssert.assertViewName(modelAndView, "error");
    }

//    We have to verify that the function that we have implemented should return an error page when the grade type does not exist
    @Test
    @Order(10)
    public void createGradeWhenGradeTypeDoesNotExistHttpRequest() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(post("/grades")
                .contentType(MediaType.APPLICATION_JSON)
                        .param("grade", "85.00")
                        .param("gradeType", "literature")
                        .param("studentId", "1"))
                .andExpect(status().isOk()).andReturn();
        ModelAndView modelAndView = mvcResult.getModelAndView();
        ModelAndViewAssert.assertViewName(modelAndView, "error");
    }

    @Test
    public void createGradeWhenGradeIsHigherThan100HttpRequest () throws Exception {

        MvcResult mvcResult = this.mockMvc.perform(post("/grades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("grade", "101.00")
                        .param("gradeType", "math")
                        .param("studentId", "1"))
                .andExpect(status().isOk())
                .andReturn();

        ModelAndView mav = mvcResult.getModelAndView();

        ModelAndViewAssert.assertViewName(mav, "error");
    }

    @Test
    public void createGradeWhenGradeIsNegativeHttpRequest () throws Exception {

        MvcResult mvcResult = this.mockMvc.perform(post("/grades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("grade", "-5")
                        .param("gradeType", "math")
                        .param("studentId", "1"))
                .andExpect(status().isOk())
                .andReturn();

        ModelAndView mav = mvcResult.getModelAndView();

        ModelAndViewAssert.assertViewName(mav, "error");
    }

//    ------------------------------------------------------------------------
    @Test
    @Order(9)
    public void deleteMathGradeHttpRequest() throws Exception {
        // Check that the grade exists
        Optional<MathGrade> mathGrade = mathGradeDao.findById(1);
        assertTrue(mathGrade.isPresent());
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders
                .get("/grades/{id}/{gradeType}", 1, "math"))
                .andExpect(status().isOk()).andReturn();
        ModelAndView modelAndView = mvcResult.getModelAndView();
        ModelAndViewAssert.assertViewName(modelAndView, "studentInformation");

        mathGrade = mathGradeDao.findById(1);
        assertFalse(mathGrade.isPresent(),"Grade should not exist");
    }

    @Test
    @Order(9)
    public void deleteScienceGradeHttpRequest() throws Exception {
        // Check that the grade exists
        Optional<ScienceGrade> scienceGrade = scienceGradeDao.findById(1);
        assertTrue(scienceGrade.isPresent());
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/grades/{id}/{gradeType}", 1, "science"))
                .andExpect(status().isOk()).andReturn();
        ModelAndView modelAndView = mvcResult.getModelAndView();
        ModelAndViewAssert.assertViewName(modelAndView, "studentInformation");

        scienceGrade = scienceGradeDao.findById(1);
        assertFalse(scienceGrade.isPresent(),"Grade should not exist");
    }
    @Test
    @Order(9)
    public void deleteHistoryGradeHttpRequest() throws Exception {
        // Check that the grade exists
        Optional<HistoryGrade> historyGrade = historyGradeDao.findById(1);
        assertTrue(historyGrade.isPresent());
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/grades/{id}/{gradeType}", 1, "history"))
                .andExpect(status().isOk()).andReturn();
        ModelAndView modelAndView = mvcResult.getModelAndView();
        ModelAndViewAssert.assertViewName(modelAndView, "studentInformation");

        historyGrade = historyGradeDao.findById(1);
        assertFalse(historyGrade.isPresent(),"Grade should not exist");
    }

    //    The delete grade function that we have implemented should return an error page when the student does not exist
    @Test
    public void deleteGradeForNonExistentStudentHttpRequest() throws Exception {
        Optional<MathGrade> mathGrade = mathGradeDao.findById(2);
        // Check that grade does not exist
        assertFalse(mathGrade.isPresent());
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .get("/grades/{id}/{gradeType}", 2, "math"))
                .andExpect(status().isOk()).andReturn();
        ModelAndView modelAndView = mvcResult.getModelAndView();
        ModelAndViewAssert.assertViewName(modelAndView, "error");
    }

    //    The delete grade function that we have implemented should return an error page when the grade type does not exist
    @Test
    public void deleteGradeWhenGradeTypeDoesNotExistHttpRequest() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders
                .get("/grades/{id}/{gradeType}", 1, "literature"))
                .andExpect(status().isOk()).andReturn();
        ModelAndView modelAndView = mvcResult.getModelAndView();
        ModelAndViewAssert.assertViewName(modelAndView, "error");
    }

    @AfterEach
    public void setupAfterTransaction() {
        jdbc.execute(sqlDeleteStudent);
        jdbc.execute(sqlDeleteMathStudent);
        jdbc.execute(sqlDeleteScienceStudent);
        jdbc.execute(sqlDeleteHistoryStudent);
    }


}
