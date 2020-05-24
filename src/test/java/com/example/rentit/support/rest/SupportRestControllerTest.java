package com.example.rentit.support.rest;

import com.example.rentit.RentitApplication;
import com.example.rentit.common.application.dto.BusinessPeriodDTO;
import com.example.rentit.common.application.dto.SimpleErrorDTO;
import com.example.rentit.common.application.dto.ValidationErrorDTO;
import com.example.rentit.inventory.application.dto.PlantInventoryItemDTO;
import com.example.rentit.inventory.application.dto.PlantReservationDTO;
import com.example.rentit.inventory.application.service.PlantInventoryEntryAssembler;
import com.example.rentit.inventory.application.service.PlantInventoryItemAssembler;
import com.example.rentit.inventory.domain.model.EquipmentCondition;
import com.example.rentit.inventory.domain.model.PlantInventoryEntry;
import com.example.rentit.inventory.domain.model.PlantInventoryItem;
import com.example.rentit.inventory.domain.model.PlantStatus;
import com.example.rentit.inventory.domain.repository.PlantInventoryEntryRepository;
import com.example.rentit.inventory.domain.repository.PlantInventoryItemRepository;
import com.example.rentit.inventory.domain.repository.PlantReservationRepository;
import com.example.rentit.support.application.dto.MaintenancePlanDTO;
import com.example.rentit.support.application.dto.MaintenanceTaskDTO;
import com.example.rentit.support.application.service.assemblers.MaintenancePlanAssembler;
import com.example.rentit.support.domain.model.MaintenancePlan;
import com.example.rentit.support.domain.model.TypeOfWork;
import com.example.rentit.support.domain.repository.MaintenancePlanRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = RentitApplication.class)
@WebAppConfiguration
@Sql(scripts = "/support/rest/plants-dataset.sql")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class SupportRestControllerTest {
    @Autowired
    PlantReservationRepository plantReservationRepository;

    @Autowired
    MaintenancePlanRepository maintenancePlanRepository;

    @Autowired
    PlantInventoryEntryRepository plantInventoryEntryRepository;

    @Autowired
    PlantInventoryItemRepository plantInventoryItemRepository;

    @Autowired
    PlantInventoryItemAssembler plantInventoryItemAssembler;

    @Autowired
    MaintenancePlanAssembler maintenancePlanAssembler;

    @Autowired
    PlantInventoryEntryAssembler plantInventoryEntryAssembler;

    @Autowired
    private WebApplicationContext wac;
    private MockMvc mockMvc;

    @Autowired @Qualifier("_halObjectMapper")
    ObjectMapper mapper;

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Test
    public void testGetMaintenancePlantById() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/support/maintenance-plan/100"))
                .andExpect(status().isOk())
                .andReturn();

        MaintenancePlanDTO planDTO = mapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<MaintenancePlanDTO>() {});

        assertThat(planDTO.get_id()).isEqualTo(100L);
        assertThat(planDTO.getPlant().get_id()).isEqualTo(300L);
        assertThat(planDTO.getYearOfAction()).isEqualTo(2017);
    }

    @Test
    public void testGetMaintenancePlanByIdNotFound() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/support/maintenance-plan/99999"))
                .andExpect(status().isNotFound())
                .andReturn();

        SimpleErrorDTO errorDTO = mapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<SimpleErrorDTO>() {});

        assertThat(errorDTO.getMessage()).isEqualTo("Maintenance plan not found (id: 99999)");
    }

    @Test
    public void testPostMaintenancePlan() throws Exception {
        MaintenancePlanDTO partialPlanDTO = new MaintenancePlanDTO();
        PlantInventoryItem item = plantInventoryItemRepository.findById(100L).orElse(null);

        partialPlanDTO.setPlant(plantInventoryItemAssembler.toResource(item));
        partialPlanDTO.setYearOfAction(2040);

        MvcResult result = mockMvc.perform(post("/api/support/maintenance-plan")
                .content(mapper.writeValueAsString(partialPlanDTO)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        MaintenancePlanDTO planDTO = mapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<MaintenancePlanDTO>() {});

        assertThat(planDTO.getPlant().get_id()).isEqualTo(100L);
        assertThat(planDTO.getYearOfAction()).isEqualTo(2040);
    }

    @Test
    public void testPostMaintenancePlanInvalidYearOfAction() throws Exception {
        MaintenancePlanDTO partialPlanDTO = new MaintenancePlanDTO();
        PlantInventoryItem item = plantInventoryItemRepository.findById(100L).orElse(null);

        partialPlanDTO.setPlant(plantInventoryItemAssembler.toResource(item));
        partialPlanDTO.setYearOfAction(1970);

        MvcResult result = mockMvc.perform(post("/api/support/maintenance-plan")
                .content(mapper.writeValueAsString(partialPlanDTO)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        ValidationErrorDTO errorDTO = mapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<ValidationErrorDTO>() {});

        assertThat(errorDTO.getViolations().get("yearOfAction")).isEqualTo("yearOfAction cannot be past");
    }

    @Test
    public void testPostMaintenancePlanInvalidTaskMaintenancePeriodEndBeforeStart() throws Exception {
        ValidationErrorDTO errorDTO = executePostMaintenancePlanAndExpectTaskTermError(BusinessPeriodDTO.of(LocalDate.now().plusDays(10), LocalDate.now().plusDays(4)));
        assertThat(errorDTO.getViolations().get("term")).isEqualTo("startDate must be before endDate");
    }

    @Test
    public void testPostMaintenancePlanInvalidTaskMaintenancePeriodIsInFuture() throws Exception {
        ValidationErrorDTO errorDTO = executePostMaintenancePlanAndExpectTaskTermError(BusinessPeriodDTO.of(LocalDate.now().minusDays(4), LocalDate.now().minusDays(2)));
        assertThat(errorDTO.getViolations().get("term")).isEqualTo("startDate must be in the future");
    }

    @Test
    public void testPostMaintenancePlanInvalidTaskMaintenancePeriodStartIsNull() throws Exception {
        ValidationErrorDTO errorDTO = executePostMaintenancePlanAndExpectTaskTermError(BusinessPeriodDTO.of(null, LocalDate.now().plusDays(5)));
        assertThat(errorDTO.getViolations().get("term")).isEqualTo("startDate cannot be null");
    }

    @Test
    public void testPostMaintenancePlanInvalidTaskMaintenancePeriodEndIsNull() throws Exception {
        ValidationErrorDTO errorDTO = executePostMaintenancePlanAndExpectTaskTermError(BusinessPeriodDTO.of(LocalDate.now().plusDays(5), null));
        assertThat(errorDTO.getViolations().get("term")).isEqualTo("endDate cannot be null");
    }

    @Test
    public void testPostMaintenancePlanTaskValidIdentifier() throws Exception {
        MaintenancePlanDTO partialPlanDTO = createMaintenancePlanWithSingleTaskDTO(BusinessPeriodDTO.of(LocalDate.now().plusDays(10), LocalDate.now().plusDays(15)));

        MvcResult result = mockMvc.perform(post("/api/support/maintenance-plan")
                .content(mapper.writeValueAsString(partialPlanDTO)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        MaintenancePlanDTO planDTO = mapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<MaintenancePlanDTO>() {});

        assertThat(planDTO.getTasks().get(0).get_id()).isEqualTo(101);
    }

    @Test
    public void testPostMaintenancePlanPlantNotReturned() throws Exception {
        MaintenancePlanDTO partialPlanDTO = new MaintenancePlanDTO();
        PlantInventoryItem item = plantInventoryItemRepository.findById(400L).orElse(null);

        partialPlanDTO.setPlant(plantInventoryItemAssembler.toResource(item));
        partialPlanDTO.setYearOfAction(2040);

        MvcResult result = mockMvc.perform(post("/api/support/maintenance-plan")
                .content(mapper.writeValueAsString(partialPlanDTO)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        ValidationErrorDTO errorDTO = mapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<ValidationErrorDTO>() {});

        assertThat(errorDTO.getViolations().get("plant"))
                .isEqualTo("Maintenance plan cannot be scheduled for a non-available plant");
    }

    @Test
    public void testPutMaintenancePlan() throws Exception {
        MaintenancePlanDTO planDTO = setupPlanDTO();

        MvcResult result = mockMvc.perform(put("/api/support/maintenance-plan/100")
                .content(mapper.writeValueAsString(planDTO)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        MaintenancePlanDTO updatedPlanDTO = mapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<MaintenancePlanDTO>() {});

        assertThat(updatedPlanDTO.getPlant().get_id()).isEqualTo(200L);
        assertThat(updatedPlanDTO.getYearOfAction()).isEqualTo(2030);
    }

    @Test
    public void testPutMaintenancePlanInvalidYearOfAction() throws Exception {
        MaintenancePlanDTO planDTO = setupPlanDTO();
        planDTO.setYearOfAction(1960);

        MvcResult result = mockMvc.perform(put("/api/support/maintenance-plan/100")
                .content(mapper.writeValueAsString(planDTO)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        ValidationErrorDTO validationErrorDTO = mapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<ValidationErrorDTO>() {});

        assertThat(validationErrorDTO.getViolations().get("yearOfAction")).isEqualTo("yearOfAction cannot be past");
    }

    @Test
    public void testPostMaintenancePlanTaskPreventiveOnlyWithServiceable() throws Exception {
        MaintenancePlanDTO planDTO = createMaintenancePlanWithTask(TypeOfWork.PREVENTIVE, EquipmentCondition.SERVICEABLE);
        mockMvc.perform(post("/api/support/maintenance-plan")
                .content(mapper.writeValueAsString(planDTO)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        planDTO = createMaintenancePlanWithTask(TypeOfWork.PREVENTIVE, EquipmentCondition.UNSERVICEABLEREPAIRABLE);
        ValidationErrorDTO errorDTO = validatePlanExpectError(planDTO);
        assertThat(errorDTO.getViolations().get("task and condition")).isEqualTo("Preventive maintenance task cannot be scheduled for a non-serviceable plant");

        planDTO = createMaintenancePlanWithTask(TypeOfWork.PREVENTIVE, EquipmentCondition.UNSERVICEABLEINCOMPLETE);
        errorDTO = validatePlanExpectError(planDTO);
        assertThat(errorDTO.getViolations().get("task and condition")).isEqualTo("Preventive maintenance task cannot be scheduled for a non-serviceable plant");

        planDTO = createMaintenancePlanWithTask(TypeOfWork.PREVENTIVE, EquipmentCondition.UNSERVICEABLECONDEMNED);
        errorDTO = validatePlanExpectError(planDTO);
        assertThat(errorDTO.getViolations().get("task and condition")).isEqualTo("Preventive maintenance task cannot be scheduled for a non-serviceable plant");
    }

    @Test
    public void testPostMaintenancePlanTaskCorrectiveOnlyWithUNSERVICEABLEREPAIRABLEorUNSERVICEABLECONDEMNED() throws Exception {
        MaintenancePlanDTO planDTO = createMaintenancePlanWithTask(TypeOfWork.CORRECTIVE, EquipmentCondition.SERVICEABLE);
        ValidationErrorDTO errorDTO = validatePlanExpectError(planDTO);
        assertThat(errorDTO.getViolations().get("task and condition")).isEqualTo("Corrective maintenance task cannot be scheduled for an non-unserviceable repairable or an non-unserviceable complete plant");

        planDTO = createMaintenancePlanWithTask(TypeOfWork.CORRECTIVE, EquipmentCondition.UNSERVICEABLEREPAIRABLE);
        mockMvc.perform(post("/api/support/maintenance-plan")
                .content(mapper.writeValueAsString(planDTO)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        planDTO = createMaintenancePlanWithTask(TypeOfWork.CORRECTIVE, EquipmentCondition.UNSERVICEABLEINCOMPLETE);
        mockMvc.perform(post("/api/support/maintenance-plan")
                .content(mapper.writeValueAsString(planDTO)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        planDTO = createMaintenancePlanWithTask(TypeOfWork.CORRECTIVE, EquipmentCondition.UNSERVICEABLECONDEMNED);
        errorDTO = validatePlanExpectError(planDTO);
        assertThat(errorDTO.getViolations().get("task and condition")).isEqualTo("Corrective maintenance task cannot be scheduled for an non-unserviceable repairable or an non-unserviceable complete plant");
    }

    @Test
    public void testPostMaintenancePlanTaskOperationalWithAnyExceptUNSERVICEABLECONDEMNED() throws Exception {
        MaintenancePlanDTO planDTO = createMaintenancePlanWithTask(TypeOfWork.OPERATIVE, EquipmentCondition.SERVICEABLE);
        mockMvc.perform(post("/api/support/maintenance-plan")
                .content(mapper.writeValueAsString(planDTO)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        planDTO = createMaintenancePlanWithTask(TypeOfWork.OPERATIVE, EquipmentCondition.UNSERVICEABLEREPAIRABLE);
        mockMvc.perform(post("/api/support/maintenance-plan")
                .content(mapper.writeValueAsString(planDTO)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        planDTO = createMaintenancePlanWithTask(TypeOfWork.OPERATIVE, EquipmentCondition.UNSERVICEABLEINCOMPLETE);
        mockMvc.perform(post("/api/support/maintenance-plan")
                .content(mapper.writeValueAsString(planDTO)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        planDTO = createMaintenancePlanWithTask(TypeOfWork.OPERATIVE, EquipmentCondition.UNSERVICEABLECONDEMNED);
        ValidationErrorDTO errorDTO = validatePlanExpectError(planDTO);
        assertThat(errorDTO.getViolations().get("task and condition")).isEqualTo("Operative maintenance task cannot be scheduled for an unserviceable condemned plant");
    }

    @Test
    public void testPutMaintenancePlanNotFound() throws Exception {
        MaintenancePlanDTO planDTO = setupPlanDTO();

        MvcResult result = mockMvc.perform(put("/api/support/maintenance-plan/666")
                .content(mapper.writeValueAsString(planDTO)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();

        SimpleErrorDTO errorDTO = mapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<SimpleErrorDTO>() {});

        assertThat(errorDTO.getMessage()).isEqualTo("Maintenance plan not found (id: 666)");
    }

    private ValidationErrorDTO executePostMaintenancePlanAndExpectTaskTermError(BusinessPeriodDTO taskPeriodDTO) throws Exception {
        MaintenancePlanDTO maintenancePlanDTO = createMaintenancePlanWithSingleTaskDTO(taskPeriodDTO);
        MvcResult result = mockMvc.perform(post("/api/support/maintenance-plan")
                .content(mapper.writeValueAsString(maintenancePlanDTO)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        ValidationErrorDTO validationErrorDTO = mapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<ValidationErrorDTO>() {});
        return validationErrorDTO;
    }

    private ValidationErrorDTO validatePlanExpectError(MaintenancePlanDTO maintenancePlanDTO) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/support/maintenance-plan")
                .content(mapper.writeValueAsString(maintenancePlanDTO)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        HashMap<String,String> hm = new HashMap<>();
        hm.put("task and condition", result.getResolvedException().getMessage());
        ValidationErrorDTO errorDTO = ValidationErrorDTO.of(hm);
        return errorDTO;
    }

    private MaintenancePlanDTO createMaintenancePlanWithTask(TypeOfWork typeOfWork, EquipmentCondition condition) throws Exception {
        MaintenancePlanDTO maintenancePlanDTO = new MaintenancePlanDTO();
        PlantInventoryEntry entry = plantInventoryEntryRepository.findById(100L).orElse(null);
        PlantInventoryItem item = PlantInventoryItem.of(null, "Z05", condition, entry, PlantStatus.AVAILABLE);
        item = plantInventoryItemRepository.saveAndFlush(item);
        PlantInventoryItemDTO plantInventoryItemDTO = plantInventoryItemAssembler.toResource(item);
        maintenancePlanDTO.setPlant(plantInventoryItemDTO);
        maintenancePlanDTO.setYearOfAction(LocalDate.now().getYear());
        ArrayList<MaintenanceTaskDTO> maintenanceTaskDTOList = new ArrayList<>();
        PlantReservationDTO plantReservationDTO = new PlantReservationDTO();
        plantReservationDTO.setPlant(plantInventoryItemDTO);
        plantReservationDTO.setSchedule(BusinessPeriodDTO.of(LocalDate.now().plusDays(10), LocalDate.now().plusDays(15)));
        MaintenanceTaskDTO maintenanceTaskDTO = new MaintenanceTaskDTO();
        maintenanceTaskDTO.setReservation(plantReservationDTO);
        maintenanceTaskDTO.setDescription("description");
        maintenanceTaskDTO.setPrice(BigDecimal.valueOf(300L));
        maintenanceTaskDTO.setTypeOfWork(typeOfWork);
        maintenanceTaskDTO.setTerm(BusinessPeriodDTO.of(LocalDate.now().plusDays(10), LocalDate.now().plusDays(15)));
        maintenanceTaskDTOList.add(maintenanceTaskDTO);
        maintenancePlanDTO.setTasks(maintenanceTaskDTOList);
        return maintenancePlanDTO;
    }

    private MaintenancePlanDTO createMaintenancePlanWithSingleTaskDTO(BusinessPeriodDTO taskPeriodDTO) throws Exception{
        MaintenancePlanDTO maintenancePlanDTO = new MaintenancePlanDTO();
        PlantInventoryItemDTO plantInventoryItemDTO = plantInventoryItemAssembler.toResource(plantInventoryItemRepository.findById(300L).orElse(null));
        maintenancePlanDTO.setTasks(setupMaintenanceTaskDTOListWithSingleTask(plantInventoryItemDTO, taskPeriodDTO));
        maintenancePlanDTO.setPlant(plantInventoryItemDTO);
        maintenancePlanDTO.setYearOfAction(LocalDate.now().getYear());
        return maintenancePlanDTO;
    }

    private ArrayList<MaintenanceTaskDTO> setupMaintenanceTaskDTOListWithSingleTask(PlantInventoryItemDTO plantInventoryItemDTO, BusinessPeriodDTO taskPeriodDTO) {
        ArrayList<MaintenanceTaskDTO> maintenanceTaskDTOList = new ArrayList<>();

        PlantReservationDTO plantReservationDTO = new PlantReservationDTO();
        plantReservationDTO.setPlant(plantInventoryItemDTO);
        plantReservationDTO.setSchedule(taskPeriodDTO);

        MaintenanceTaskDTO maintenanceTaskDTO = new MaintenanceTaskDTO();
        maintenanceTaskDTO.setDescription("description");
        maintenanceTaskDTO.setPrice(BigDecimal.valueOf(300L));
        maintenanceTaskDTO.setTypeOfWork(TypeOfWork.CORRECTIVE);
        maintenanceTaskDTO.setReservation(plantReservationDTO);
        maintenanceTaskDTO.setTerm(taskPeriodDTO);

        maintenanceTaskDTOList.add(maintenanceTaskDTO);
        return maintenanceTaskDTOList;
    }

    private MaintenancePlanDTO setupPlanDTO() {
        MaintenancePlan plan = maintenancePlanRepository.findById(100L).orElse(null);
        PlantInventoryItem item = plantInventoryItemRepository.findById(200L).orElse(null);

        plan.setPlant(item);
        plan.setYearOfAction(2030);

        return maintenancePlanAssembler.toResource(plan);
    }
}
