package com.haifachagwey.springmvc;

import com.haifachagwey.springmvc.models.Gradebook;
import com.haifachagwey.springmvc.models.GradebookCollegeStudent;
import com.haifachagwey.springmvc.models.StudentGrades;
import com.haifachagwey.springmvc.repository.MathGradeDao;
import com.haifachagwey.springmvc.repository.StudentDao;
import com.haifachagwey.springmvc.service.StudentAndGradeService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.ModelAndViewAssert;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.servlet.ModelAndView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;



@AutoConfigureMockMvc
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestPropertySource("/application-test.properties")
public class GradebookControllerUnitTest {

//    The @Mock annotation does not create a bean
//    @Mock
//    private StudentAndGradeService studentAndGradeServiceMock;
//
//    @InjectMocks  // Automatically inject the mocked service into the controller
//    private GradebookController gradebookController;
//
//    @BeforeEach
//    public void setup() {
//        // Set up MockMvc with the controller and injected mocks
//        mockMvc = MockMvcBuilders.standaloneSetup(gradebookController).build();
//    }

    private static MockHttpServletRequest request;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudentAndGradeService studentAndGradeService;

//    To fully intergrate StudentAndGradeService in spring framework test we have to use @MockBean
//    This mock will replace the real StudentAndGradeService in spring context and GradebookController will use this mock instead of the real one
//    MockMvc will use the GradebookController which we early inject inside it the StudentAndGradeService mock
//    studentServiceMock is a mock object, it doesn't have access to the real database (you can't use it to retrieve real data from database), instead, it returns predefined values.

//    This is what we have in our spring context
//    StudentAndGradeService studentAndGradeService = new StudentAndGradeService() (mock)
//    GradebookController gradebookController = new GradebookController(studentAndGradeService);

    @MockBean
    private StudentAndGradeService studentAndGradeServiceMock;

    @Autowired
    private StudentDao studentDao;

    @Autowired
    private MathGradeDao mathGradeDao;

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
        GradebookCollegeStudent studentOne = new GradebookCollegeStudent("Eric", "Roby",
                "eric.roby@gmail.com");
        studentOne.setStudentGrades(new StudentGrades());
        GradebookCollegeStudent studentTwo = new GradebookCollegeStudent("Chad", "Darby",
                "chad.darby@gmail.com");
        studentTwo.setStudentGrades(new StudentGrades());
        List<GradebookCollegeStudent> students = new ArrayList<>(Arrays.asList(studentOne, studentTwo));
        Gradebook gradebook = new Gradebook(students);
        when(studentAndGradeServiceMock.getGradebook()).thenReturn(gradebook);

//        If i do not set return values using the "when" statement an error will be regenerated saying
//        Cannot invoke "com.haifachagwey.springmvc.models.Gradebook.getStudents()" because "gradebook" is null
//        at com.haifachagwey.springmvc.controller.GradebookController.getGradebook

//        assertEquals("Roby", studentAndGradeServiceMock.getGradebook().getStudents().get(0).getLastname(), "Id should be 1");
//        assertEquals("Chad", studentAndGradeServiceMock.getGradebook().getStudents().get(1).getFirstname(), "Firstname Chad");

//        The mockMvc will send http request
//        The Gradebook controller is injected with the mock
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/"))
                .andExpect(status().isOk()).andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        ModelAndViewAssert.assertViewName(modelAndView, "index");

        List<GradebookCollegeStudent> studentList = (List<GradebookCollegeStudent>) modelAndView.getModel().get("students");
        assertEquals(2, studentList.size());
        assertEquals("Eric", studentList.get(0).getFirstname());
    }

    @Test
    @Order(1)
    public void createStudentHttpRequest() throws Exception {
        doNothing().when(studentAndGradeServiceMock).createStudent(request.getParameter("firstname"),
                request.getParameter("lastname"),
                request.getParameter("emailAddress"));

        GradebookCollegeStudent studentOne = new GradebookCollegeStudent("Eric", "Roby",
                "eric.roby@gmail.com");
        studentOne.setStudentGrades(new StudentGrades());
        GradebookCollegeStudent studentTwo = new GradebookCollegeStudent("Chad", "Darby",
                "chad.darby@gmail.com");
        studentTwo.setStudentGrades(new StudentGrades());
        List<GradebookCollegeStudent> students = new ArrayList<>(Arrays.asList(studentOne, studentTwo));
        Gradebook gradebook = new Gradebook(students);
        when(studentAndGradeServiceMock.getGradebook()).thenReturn(gradebook);

        MvcResult mvcResult = this.mockMvc.perform(post("/")
                .contentType(MediaType.APPLICATION_JSON)
                .param("firstname", request.getParameterValues("firstname"))
                .param("lastname", request.getParameterValues("lastname"))
                .param("emailAddress", request.getParameterValues("emailAddress")))
                .andExpect(status().isOk()).andReturn();
        ModelAndView modelAndView = mvcResult.getModelAndView();
        ModelAndViewAssert.assertViewName(modelAndView, "index");
    }

    @Test
    @Order(2)
    public void deleteStudentHttpRequest() throws Exception {
        when(studentAndGradeServiceMock.checkIfStudentExist(1)).thenReturn(true);

        doNothing().when(studentAndGradeServiceMock).deleteStudent(1);

        GradebookCollegeStudent studentOne = new GradebookCollegeStudent("Eric", "Roby",
                "eric.roby@gmail.com");
        studentOne.setStudentGrades(new StudentGrades());
        GradebookCollegeStudent studentTwo = new GradebookCollegeStudent("Chad", "Darby",
                "chad.darby@gmail.com");
        studentTwo.setStudentGrades(new StudentGrades());
        List<GradebookCollegeStudent> students = new ArrayList<>(Arrays.asList(studentOne, studentTwo));
        Gradebook gradebook = new Gradebook(students);
        when(studentAndGradeServiceMock.getGradebook()).thenReturn(gradebook);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .get("/delete/student/{id}", 1))
                .andExpect(status().isOk()).andReturn();
        ModelAndView modelAndView = mvcResult.getModelAndView();
        ModelAndViewAssert.assertViewName(modelAndView, "index");
    }

    @Test
    @Order(3)
    public void deleteStudentHttpRequestErrorPage() throws Exception {
//        Mocks return default values unless explicitly configured. For boolean methods, the default is false
//        when(studentAndGradeServiceMock.checkIfStudentExist(0)).thenReturn(false);  // This is not strictly necessary due to the default mock behavior.
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .get("/delete/student/{id}", 3))
                .andExpect(status().isOk()).andReturn();
        ModelAndView modelAndView = mvcResult.getModelAndView();
        ModelAndViewAssert.assertViewName(modelAndView, "error");

    }

//    @Test
//    @Order(4)
//    public void getStudentHttpRequest() throws Exception {
//        when(studentAndGradeServiceMock.checkIfStudentExist(1)).thenReturn(true);
//
//        doNothing().when(studentAndGradeServiceMock).configureStudentInformationModel(1,null);
//        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/{id}", 1))
//                .andExpect(status().isOk()).andReturn();
//        ModelAndView modelAndView = mvcResult.getModelAndView();
//        ModelAndViewAssert.assertViewName(modelAndView, "studentInformation");
//    }

    @Test
    @Order(5)
    public void getStudentHttpStudentDoesNotExistRequest() throws Exception {
//        Mocks return default values unless explicitly configured. For boolean methods, the default is false
//        when(studentAndGradeServiceMock.checkIfStudentExist(0)).thenReturn(false);  // This is not strictly necessary due to the default mock behavior.
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/{id}", 0))
                .andExpect(status().isOk()).andReturn();
        ModelAndView modelAndView = mvcResult.getModelAndView();
        ModelAndViewAssert.assertViewName(modelAndView, "error");
    }


//    ...

    @AfterEach
    public void setupAfterTransaction() {
        jdbc.execute(sqlDeleteStudent);
        jdbc.execute(sqlDeleteMathStudent);
        jdbc.execute(sqlDeleteScienceStudent);
        jdbc.execute(sqlDeleteHistoryStudent);
    }


}
