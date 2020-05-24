package com.example.rentit.support.rest;

import com.example.rentit.RentitApplication;
import com.example.rentit.common.application.dto.BusinessPeriodDTO;
import com.example.rentit.common.application.dto.SimpleErrorDTO;
import com.example.rentit.common.application.dto.ValidationErrorDTO;
import com.example.rentit.common.application.service.BusinessPeriodAssembler;
import com.example.rentit.common.domain.model.BusinessPeriod;
import com.example.rentit.inventory.application.service.PlantInventoryItemAssembler;
import com.example.rentit.inventory.domain.model.PlantInventoryItem;
import com.example.rentit.inventory.domain.repository.PlantInventoryItemRepository;
import com.example.rentit.support.application.dto.MaintenanceOrderDTO;
import com.example.rentit.support.application.dto.MaintenancePlanDTO;
import com.example.rentit.support.application.service.assemblers.MaintenanceOrderAssembler;
import com.example.rentit.support.domain.model.*;
import com.example.rentit.support.domain.repository.MaintenanceOrderRepository;
import com.example.rentit.support.domain.repository.MaintenanceTaskRepository;
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

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = RentitApplication.class)
@WebAppConfiguration
@Sql(scripts = "/support/rest/plants-dataset.sql")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class MaintenanceRestControllerTest {
    @Autowired
    MaintenanceOrderRepository maintenanceOrderRepo;

    @Autowired
    MaintenanceTaskRepository maintenanceTaskRepository;

    @Autowired
    PlantInventoryItemRepository plantInventoryItemRepository;

    @Autowired
    PlantInventoryItemRepository itemRepo;

    @Autowired
    MaintenanceOrderAssembler maintenanceOrderAssembler;

    @Autowired
    PlantInventoryItemAssembler itemAssembler;

    @Autowired
    BusinessPeriodAssembler scheduleAssembler;

    @Autowired
    PlantInventoryItemAssembler plantInventoryItemAssembler;

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
    @Sql("classpath:/support/rest/plants-dataset.sql")
    public void testGetMaintenanceOrder() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/support/maintenance-order/100"))
                .andExpect(status().isOk())
                .andReturn();

        MaintenanceOrderDTO orderDTO = mapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<MaintenanceOrderDTO>() {});

        assertThat(orderDTO.get_id()).isEqualTo(100L);
        assertThat(orderDTO.getPlantId()).isEqualTo(200L);
        assertThat(orderDTO.getDescription()).isEqualTo("I do not like the vehicle colour");
        assertThat(orderDTO.getStatus()).isEqualTo(MaintenanceOrderStatus.PENDING);
        assertThat(orderDTO.getSchedule().getStartDate().toString()).isEqualTo("2020-06-12");
        assertThat(orderDTO.getSchedule().getEndDate().toString()).isEqualTo("2020-06-30");
    }

    @Test
    public void testGetMaintenanceOrderByIdNotFound() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/support/maintenance-order/99999"))
                .andExpect(status().isNotFound())
                .andReturn();

        SimpleErrorDTO errorDTO = mapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<SimpleErrorDTO>() {});

        assertThat(errorDTO.getMessage()).isEqualTo("Maintenance order not found (id: 99999)");
    }

    @Test
    @Sql("classpath:/support/rest/plants-dataset.sql")
    public void testPostMaintenanceOrder() throws Exception {
        BusinessPeriodDTO scheduleDTO = scheduleAssembler.toResource(
                BusinessPeriod.of(
                        LocalDate.of(2020, 7, 1),
                        LocalDate.of(2020, 7, 14)));

        MaintenanceOrderDTO partialOrderDTO = new MaintenanceOrderDTO();
        partialOrderDTO.setPlantId(300L);
        partialOrderDTO.setSchedule(scheduleDTO);
        partialOrderDTO.setDescription("Description");

        MvcResult result = mockMvc.perform(post("/api/support/maintenance-order")
                .content(mapper.writeValueAsString(partialOrderDTO)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        MaintenanceOrderDTO orderDTO = mapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<MaintenanceOrderDTO>() {});

        assertThat(orderDTO.get_id()).isNotNull();
        assertThat(orderDTO.getPlantId()).isEqualTo(300L);
        assertThat(orderDTO.getDescription()).isEqualTo("Description");
        assertThat(orderDTO.getStatus()).isEqualTo(MaintenanceOrderStatus.PENDING);
        assertThat(orderDTO.getSchedule().getStartDate().toString()).isEqualTo("2020-07-01");
        assertThat(orderDTO.getSchedule().getEndDate().toString()).isEqualTo("2020-07-14");
    }

    @Test
    @Sql("classpath:/support/rest/plants-dataset.sql")
    public void testPatchCancelMaintenanceOrder() throws Exception {
        MaintenanceOrder orderAccepted = maintenanceOrderRepo.findById(200l).orElse(null);
        assertThat(orderAccepted.getStatus()).isEqualTo(MaintenanceOrderStatus.ACCEPTED);
        MaintenanceOrderDTO orderDTO = executeCancelMO(orderAccepted.getId());
        assertThat(orderDTO.getStatus()).isEqualTo(MaintenanceOrderStatus.CANCELLED);

        MaintenanceOrder orderPending = maintenanceOrderRepo.findById(100l).orElse(null);
        assertThat(orderPending.getStatus()).isEqualTo(MaintenanceOrderStatus.PENDING);
        orderDTO = executeCancelMO(orderPending.getId());
        assertThat(orderDTO.getStatus()).isEqualTo(MaintenanceOrderStatus.CANCELLED);
    }

    @Test
    @Sql("classpath:/support/rest/plants-dataset.sql")
    public void testPatchCompleteMaintenanceOrder() throws Exception {
        MaintenanceOrder orderAccepted = maintenanceOrderRepo.findById(200l).orElse(null);
        assertThat(orderAccepted.getStatus()).isEqualTo(MaintenanceOrderStatus.ACCEPTED);
        MvcResult result = mockMvc.perform(patch("/api/support/maintenance-order/" + orderAccepted.getId().toString() + "/complete"))
                .andExpect(status().isOk())
                .andReturn();
        MaintenanceOrderDTO orderDTO = mapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<MaintenanceOrderDTO>() {});
        assertThat(orderDTO.getStatus()).isEqualTo(MaintenanceOrderStatus.COMPLETED);
    }

    @Test
    @Sql("classpath:/support/rest/plants-dataset.sql")
    public void testPendingOrderCanNotBeCompleted() throws Exception {
        MaintenanceOrder orderPending = maintenanceOrderRepo.findById(100l).orElse(null);
        assertThat(orderPending.getStatus()).isEqualTo(MaintenanceOrderStatus.PENDING);
        executeCompleteMOAndAssertStatusTransitionErrorThrown(maintenanceOrderAssembler.toResource(orderPending));
    }

    @Test
    @Sql("classpath:/support/rest/plants-dataset.sql")
    public void testRejectedOrderCanNotBeCompleted() throws Exception {
        MaintenanceOrder orderRejected = maintenanceOrderRepo.findById(300l).orElse(null);
        assertThat(orderRejected.getStatus()).isEqualTo(MaintenanceOrderStatus.REJECTED);
        executeCompleteMOAndAssertStatusTransitionErrorThrown(maintenanceOrderAssembler.toResource(orderRejected));
    }

    @Test
    @Sql("classpath:/support/rest/plants-dataset.sql")
    public void testCancelledOrderCanNotBeCompleted() throws Exception {
        MaintenanceOrder orderCancelled = maintenanceOrderRepo.findById(400l).orElse(null);
        assertThat(orderCancelled.getStatus()).isEqualTo(MaintenanceOrderStatus.CANCELLED);
        executeCompleteMOAndAssertStatusTransitionErrorThrown(maintenanceOrderAssembler.toResource(orderCancelled));
    }

    @Test
    @Sql("classpath:/support/rest/plants-dataset.sql")
    public void testCompletedOrderCanNotBeCompletedAgain() throws Exception {
        MaintenanceOrder orderCompleted = maintenanceOrderRepo.findById(500l).orElse(null);
        assertThat(orderCompleted.getStatus()).isEqualTo(MaintenanceOrderStatus.COMPLETED);
        executeCompleteMOAndAssertStatusTransitionErrorThrown(maintenanceOrderAssembler.toResource(orderCompleted));
    }

    @Test
    @Sql("classpath:/support/rest/plants-dataset.sql")
    public void testPatchCancelMaintenanceOrderAfterMaintenanceStarted() throws Exception {
        MaintenanceOrder orderAccepted = maintenanceOrderRepo.findById(700l).orElse(null);
        assertThat(orderAccepted.getStatus()).isEqualTo(MaintenanceOrderStatus.ACCEPTED);
        MaintenanceOrderDTO orderDTO = maintenanceOrderAssembler.toResource(orderAccepted);
        executeCancelMOAndAssertStatusTransitionErrorThrown(orderDTO);
    }

    @Test
    @Sql("classpath:/support/rest/plants-dataset.sql")
    public void testRejectedOrderCanNotBeCancelled() throws Exception {
        MaintenanceOrder orderRejected = maintenanceOrderRepo.findById(300l).orElse(null);
        assertThat(orderRejected.getStatus()).isEqualTo(MaintenanceOrderStatus.REJECTED);
        executeCancelMOAndAssertStatusTransitionErrorThrown(maintenanceOrderAssembler.toResource(orderRejected));
    }

    @Test
    @Sql("classpath:/support/rest/plants-dataset.sql")
    public void testCancelledOrderCanNotBeCancelledAgain() throws Exception {
        MaintenanceOrder orderCancelled = maintenanceOrderRepo.findById(400l).orElse(null);
        assertThat(orderCancelled.getStatus()).isEqualTo(MaintenanceOrderStatus.CANCELLED);
        executeCancelMOAndAssertStatusTransitionErrorThrown(maintenanceOrderAssembler.toResource(orderCancelled));
    }

    @Test
    @Sql("classpath:/support/rest/plants-dataset.sql")
    public void testCompletedOrderCanNotBeCancelled() throws Exception {
        MaintenanceOrder orderCompleted = maintenanceOrderRepo.findById(500l).orElse(null);
        assertThat(orderCompleted.getStatus()).isEqualTo(MaintenanceOrderStatus.COMPLETED);
        executeCancelMOAndAssertStatusTransitionErrorThrown(maintenanceOrderAssembler.toResource(orderCompleted));
    }

    @Test
    @Sql("classpath:/support/rest/plants-dataset.sql")
    public void testOnlyPendingOrderCanBeBeRejected() throws Exception {
        MaintenanceOrder order = maintenanceOrderRepo.findById(500l).orElse(null);
        MaintenanceOrderStatus[] forbiddenStatuses = {
                MaintenanceOrderStatus.CANCELLED,
                MaintenanceOrderStatus.REJECTED,
                MaintenanceOrderStatus.COMPLETED,
                MaintenanceOrderStatus.ACCEPTED,
        };
        for (MaintenanceOrderStatus status: forbiddenStatuses) {
            order.setStatus(status);
            executeRejectMOAndAssertStatusTransitionErrorThrown(maintenanceOrderAssembler.toResource(order));
        }

        MaintenanceOrder orderPending = maintenanceOrderRepo.findById(100l).orElse(null);
        MaintenanceOrderDTO orderDTO = executeRejectMO(orderPending.getId());

        assertThat(orderDTO.getStatus()).isEqualTo(MaintenanceOrderStatus.REJECTED);
    }

    @Test
    @Sql("classpath:/support/rest/plants-dataset.sql")
    public void testOnlyPendingOrderCanBeBeAccepted() throws Exception {
        MaintenanceOrder order = maintenanceOrderRepo.findById(500l).orElse(null);
        MaintenanceOrderStatus[] forbiddenStatuses = {
                MaintenanceOrderStatus.CANCELLED,
                MaintenanceOrderStatus.REJECTED,
                MaintenanceOrderStatus.COMPLETED,
                MaintenanceOrderStatus.ACCEPTED,
        };
        for (MaintenanceOrderStatus status: forbiddenStatuses) {
            order.setStatus(status);
            MaintenanceOrderDTO orderDTO = acceptOrder(order);
            assertThat(orderDTO.get_id()).isEqualTo(null);
        }

        MaintenanceOrder orderPending = maintenanceOrderRepo.findById(100l).orElse(null);
        assertThat(orderPending.getStatus()).isEqualTo(MaintenanceOrderStatus.PENDING);

        MaintenanceOrderDTO orderDTO = acceptOrder(orderPending);

        assertThat(orderDTO.getStatus()).isEqualTo(MaintenanceOrderStatus.ACCEPTED);
    }

    private MaintenanceOrderDTO acceptOrder(MaintenanceOrder order) throws Exception {
        BusinessPeriodDTO scheduleDTO = scheduleAssembler.toResource(
                BusinessPeriod.of(
                        LocalDate.of(2020, 7, 1),
                        LocalDate.of(2020, 7, 14)));

        MaintenancePlanDTO partialPlanDTO = new MaintenancePlanDTO();
        PlantInventoryItem item = plantInventoryItemRepository.findById(300L).orElse(null);

        partialPlanDTO.setPlant(plantInventoryItemAssembler.toResource(item));
        partialPlanDTO.setYearOfAction(2040);

        MaintenanceOrderDTO partialOrderDTO = new MaintenanceOrderDTO();
        partialOrderDTO.setPlantId(300L);
        partialOrderDTO.setSchedule(scheduleDTO);
        partialOrderDTO.setDescription("Description");
        partialOrderDTO.setMaintenancePlan(partialPlanDTO);

        return executeAcceptMO(order.getId(), partialOrderDTO);
    }

    private MaintenanceOrderDTO executeCancelMO(Long orderId) throws Exception {
        MvcResult result = mockMvc.perform(patch("/api/support/maintenance-order/" + orderId.toString() + "/cancel"))
                .andExpect(status().isOk())
                .andReturn();
        MaintenanceOrderDTO orderDTO = mapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<MaintenanceOrderDTO>() {});
        return orderDTO;
    }

    private  MaintenanceOrderDTO executeAcceptMO(Long orderId, MaintenanceOrderDTO partialOrderDTO) throws Exception {
        MvcResult result = mockMvc.perform(patch("/api/support/maintenance-order/" + orderId.toString() + "/accept")
                .content(mapper.writeValueAsString(partialOrderDTO)).contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        MaintenanceOrderDTO orderDTO = mapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<MaintenanceOrderDTO>() {});

        return orderDTO;
    }

    private  MaintenanceOrderDTO executeRejectMO(Long orderId) throws Exception {
        MvcResult result = mockMvc.perform(patch("/api/support/maintenance-order/" + orderId.toString() + "/reject"))
                .andExpect(status().isOk())
                .andReturn();

        MaintenanceOrderDTO orderDTO = mapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<MaintenanceOrderDTO>() {});

        return orderDTO;
    }

    private void executeCancelMOAndAssertStatusTransitionErrorThrown(MaintenanceOrderDTO orderDTO) throws Exception {
        executePatchAssertStatusTransitionErrorThrown("/api/support/maintenance-order/" + orderDTO.get_id().toString() + "/cancel");
    }

    private void executeCompleteMOAndAssertStatusTransitionErrorThrown(MaintenanceOrderDTO orderDTO) throws Exception {
        executePatchAssertStatusTransitionErrorThrown("/api/support/maintenance-order/" + orderDTO.get_id().toString() + "/complete");
    }

    private void executeRejectMOAndAssertStatusTransitionErrorThrown(MaintenanceOrderDTO orderDTO) throws Exception {
        executePatchAssertStatusTransitionErrorThrown("/api/support/maintenance-order/" + orderDTO.get_id().toString() + "/reject");
    }

    private void executeAcceptMOAndAssertStatusTransitionErrorThrown(MaintenanceOrderDTO orderDTO) throws Exception {
        executePatchAssertStatusTransitionErrorThrown("/api/support/maintenance-order/" + orderDTO.get_id().toString() + "/accept");
    }

    private void executePatchAssertStatusTransitionErrorThrown(String url) throws Exception {
        MvcResult result = mockMvc.perform(patch(url))
                .andExpect(status().isBadRequest())
                .andReturn();
        ValidationErrorDTO validationErrorDTO = mapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<ValidationErrorDTO>() {});
        assertThat(validationErrorDTO.getViolations().get("transitionTo")).isEqualTo("Forbidden transition");
    }
}
