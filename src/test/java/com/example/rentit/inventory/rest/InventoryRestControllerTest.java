package com.example.rentit.inventory.rest;

import com.example.rentit.RentitApplication;
import com.example.rentit.common.application.dto.SimpleErrorDTO;
import com.example.rentit.common.application.dto.ValidationErrorDTO;
import com.example.rentit.common.application.exception.InvoiceNotSentException;
import com.example.rentit.common.application.service.ExternalAPICommunicator;
import com.example.rentit.common.domain.model.BusinessPeriod;
import com.example.rentit.inventory.application.dto.PlantInventoryEntryDTO;
import com.example.rentit.inventory.application.dto.PlantInventoryItemDTO;
import com.example.rentit.inventory.application.exception.PlantInventoryItemNotFoundException;
import com.example.rentit.inventory.application.service.PlantInventoryEntryAssembler;
import com.example.rentit.inventory.application.service.PlantInventoryItemAssembler;
import com.example.rentit.inventory.domain.model.*;
import com.example.rentit.inventory.domain.repository.PlantInventoryEntryRepository;
import com.example.rentit.inventory.domain.repository.PlantInventoryItemRepository;
import com.example.rentit.inventory.domain.repository.PlantReservationRepository;
import com.example.rentit.sales.domain.model.Invoice;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = RentitApplication.class)
@WebAppConfiguration
@Sql(scripts = "/inventory/rest/plants-dataset.sql")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class InventoryRestControllerTest {
    @Autowired
    PlantInventoryEntryRepository plantInventoryEntryRepository;

    @Autowired
    PlantInventoryEntryAssembler plantInventoryEntryAssembler;

    @Autowired
    PlantInventoryItemRepository plantInventoryItemRepository;

    @Autowired
    PlantInventoryItemAssembler plantInventoryItemAssembler;

    @Autowired
    PlantReservationRepository plantReservationRepository;

    @MockBean
    ExternalAPICommunicator mockExternalAPICommunicator;

    @Autowired
    private WebApplicationContext wac;
    private MockMvc mockMvc;

    @Autowired @Qualifier("_halObjectMapper")
    ObjectMapper mapper;

    @Before
    public void setup() throws InvoiceNotSentException, URISyntaxException {
        MockitoAnnotations.initMocks(this);
        Mockito.when(mockExternalAPICommunicator.submitInvoice(Mockito.any(Invoice.class))).thenAnswer(any -> true);
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Test
    public void testGetAvailablePlantsByPeriod() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/inventory/plant")
                .param("startDate", "2020-08-01")
                .param("endDate", "2020-08-20"))
                .andExpect(status().isOk())
                .andReturn();

        List<PlantInventoryItemDTO> itemsDTO = mapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<List<PlantInventoryItemDTO>>() {});

        assertThat(itemsDTO.size()).isEqualTo(4);
        assertThat(itemsDTO.get(0).getPrice().toString()).isEqualTo("150.00");
        assertThat(itemsDTO.get(1).getPrice().toString()).isEqualTo("150.00");
        assertThat(itemsDTO.get(2).getPrice().toString()).isEqualTo("150.00");
        assertThat(itemsDTO.get(3).getPrice().toString()).isEqualTo("150.00");
    }

    @Test
    public void testGetAvailablePlantsByPeriodTwoAvailable() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/inventory/plant")
                .param("startDate", "2020-05-30")
                .param("endDate", "2020-07-08"))
                .andExpect(status().isOk())
                .andReturn();

        List<PlantInventoryItemDTO> itemsDTO = mapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<List<PlantInventoryItemDTO>>() {});

        assertThat(itemsDTO.size()).isEqualTo(2);
        assertThat(itemsDTO.get(0).getPrice().toString()).isEqualTo("150.00");
        assertThat(itemsDTO.get(1).getPrice().toString()).isEqualTo("150.00");
    }

    @Test
    public void testGetPlantAvailability() throws Exception {
        // Item is considered available if it is not rented during the needed period
        // and if it is serviceable at the moment of the query
        // or the requested period is at least three weeks later in the future and the
        // plant is programed for maintenance at least one week before the rental. Plants marked
        // as UnserviceableCondemned cannot be available as they cannot be fixed

        String result = executeGetPlantAvailabilityAndExpectSuccess(100l);
        assertThat(result).isEqualTo("{\"available\":true}");

        // create a reservation intersecting with the needed period, plant should not be available
        PlantInventoryItem plant = plantInventoryItemRepository.findById(100l).orElseThrow(() ->new PlantInventoryItemNotFoundException(100l));
        BusinessPeriod reservationPeriod = BusinessPeriod.of(LocalDate.of(2020,07,03), LocalDate.of(2020,07,12));
        PlantReservation plantReservation = PlantReservation.of(null,reservationPeriod, null, plant, null);
        plantReservationRepository.saveAndFlush(plantReservation);
        result = executeGetPlantAvailabilityAndExpectSuccess(100l);
        assertThat(result).isEqualTo("{\"available\":false}");

        // UNSERVICEABLECONDEMNED plant should not be available
        result = executeGetPlantAvailabilityAndExpectSuccess(200l);
        assertThat(result).isEqualTo("{\"available\":false}");

        // UNSERVICEABLEREPAIRABLE plant which will be repaired more that one week before the needed period should be available
        result = executeGetPlantAvailabilityAndExpectSuccess(300l);
        assertThat(result).isEqualTo("{\"available\":true}");

        // UNSERVICEABLEREPAIRABLE plant which will be repaired less than a week before the needed period should not be available
        result = executeGetPlantAvailabilityAndExpectSuccess(400l);
        assertThat(result).isEqualTo("{\"available\":false}");
    }

    @Test
    public void testGetPlantAvailabilityPlantNotFound() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/inventory/plant/1")
                .param("startDate", "2020-07-10")
                .param("endDate", "2020-07-20"))
                .andExpect(status().isNotFound())
                .andReturn();
        assertThat(result.getResolvedException().getMessage()).isEqualTo("Plant inventory item not found (id: 1)");
    }

    @Test
    public void testPostPlantInventoryItem() throws Exception {
        PlantInventoryItemDTO partialItemDTO = setupNewPlantInventoryItemDTO();

        MvcResult result = mockMvc.perform(post("/api/inventory/plant")
                .content(mapper.writeValueAsString(partialItemDTO)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        PlantInventoryItemDTO itemDTO = mapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<PlantInventoryItemDTO>() {});

        assertThat(itemDTO.getPlantInfo().get_id()).isEqualTo(100L);
        assertThat(itemDTO.getSerialNumber()).isEqualTo("Z01");
        assertThat(itemDTO.getEquipmentCondition()).isEqualTo(EquipmentCondition.SERVICEABLE);
    }

    @Test
    public void testPostPlantInventoryItemInvalid() throws Exception {
        PlantInventoryItemDTO partialItemDTO = setupPlantInventoryItemDTOInvalidEntry();

        MvcResult result = mockMvc.perform(post("/api/inventory/plant")
                .content(mapper.writeValueAsString(partialItemDTO)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        ValidationErrorDTO errorDTO = mapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<ValidationErrorDTO>() {});

        assertThat(errorDTO.getViolations().get("plantInfo")).isEqualTo("plant info cannot be null");
    }

    @Test
    public void testPatchPlantInventoryItemInvalid() throws Exception {
        PlantInventoryItemDTO partialItemDTO = setupUpdatedPlantInventoryItemDTO();

        MvcResult result = mockMvc.perform(patch("/api/inventory/plant/100")
                .content(mapper.writeValueAsString(partialItemDTO)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        PlantInventoryItemDTO itemDTO = mapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<PlantInventoryItemDTO>() {});

        assertThat(itemDTO.getPlantInfo().get_id()).isEqualTo(100L);
        assertThat(itemDTO.getSerialNumber()).isEqualTo("A01");
        assertThat(itemDTO.getEquipmentCondition()).isEqualTo(EquipmentCondition.UNSERVICEABLEREPAIRABLE);
    }

    @Test
    public void testPatchPlantInventoryItemNotFound() throws Exception {
        PlantInventoryItemDTO itemDTO = setupUpdatedPlantInventoryItemDTO();

        MvcResult result = mockMvc.perform(patch("/api/inventory/plant/999")
                .content(mapper.writeValueAsString(itemDTO)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();

        SimpleErrorDTO errorDTO = mapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<SimpleErrorDTO>() {});

        assertThat(errorDTO.getMessage()).isEqualTo("Plant inventory item not found (id: 999)");
    }

    @Test
    public void testPostPlantInventoryEntry() throws Exception {
        PlantInventoryEntryDTO partialEntryDTO = setupNewPlantInventoryEntryDTO();
        MvcResult result = mockMvc.perform(post("/api/inventory/plant-entries")
                .content(mapper.writeValueAsString(partialEntryDTO)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        PlantInventoryEntryDTO entryDTO = mapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<PlantInventoryEntryDTO>() {});

        assertThat(entryDTO.getName()).isEqualTo("name");
        assertThat(entryDTO.getDescription()).isEqualTo("desc");
        assertThat(entryDTO.getPrice()).isEqualTo(BigDecimal.valueOf(100L));
    }

    @Test
    public void testPostPlantInventoryEntryInvalid() throws Exception {
        PlantInventoryEntryDTO partialEntryDTO = setupPlantInventoryEntryDTOInvalidEntry();

        MvcResult result = mockMvc.perform(post("/api/inventory/plant-entries")
                .content(mapper.writeValueAsString(partialEntryDTO)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        ValidationErrorDTO errorDTO = mapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<ValidationErrorDTO>() {});

        assertThat(errorDTO.getViolations().get("price")).isEqualTo("price cannot be null");
    }

    @Test
    public void testPatchPlantInventoryEntryValid() throws Exception {
        PlantInventoryEntryDTO partialEntryDTO = setupUpdatedPlantInventoryEntryDTO();

        MvcResult result = mockMvc.perform(patch("/api/inventory/plant-entries/100")
                .content(mapper.writeValueAsString(partialEntryDTO)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        PlantInventoryEntryDTO entryDTO = mapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<PlantInventoryEntryDTO>() {});

        assertThat(entryDTO.getName()).isEqualTo("newName");
        assertThat(entryDTO.getDescription()).isEqualTo("1.5 Tonne Mini excavator");
    }

    @Test
    public void testPatchPlantInventoryEntryNotFound() throws Exception {
        PlantInventoryEntryDTO entryDTO = setupUpdatedPlantInventoryEntryDTO();

        MvcResult result = mockMvc.perform(patch("/api/inventory/plant-entries/999")
                .content(mapper.writeValueAsString(entryDTO)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();

        SimpleErrorDTO errorDTO = mapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<SimpleErrorDTO>() {});

        assertThat(errorDTO.getMessage()).isEqualTo("Plant inventory entry not found (id: 999)");
    }

    @Test
    public void testChangeItemStatuses() throws Exception {
        PlantInventoryItemDTO itemDTO;
        MvcResult result;
        PlantInventoryItem item = plantInventoryItemRepository.getOne(100L);

        result = mockMvc.perform(patch("/api/inventory/plant/100/dispatch?orderId="+1)).andReturn();
        itemDTO = mapper.readValue(result.getResponse().getContentAsString(), new TypeReference<PlantInventoryItemDTO>() {});
        assertThat(itemDTO.getStatus()).isEqualTo(PlantStatus.DISPATCHED);

        mockMvc.perform(patch("/api/inventory/plant/100/dispatch?orderId="+1)).andExpect(status().isBadRequest()).andReturn();
        mockMvc.perform(patch("/api/inventory/plant/100/return?orderId="+1)).andExpect(status().isBadRequest()).andReturn();

        result = mockMvc.perform(patch("/api/inventory/plant/100/deliver?orderId="+1)).andExpect(status().isOk()).andReturn();
        itemDTO = mapper.readValue(result.getResponse().getContentAsString(), new TypeReference<PlantInventoryItemDTO>() {});
        assertThat(itemDTO.getStatus()).isEqualTo(PlantStatus.DELIVERED);

        mockMvc.perform(patch("/api/inventory/plant/100/dispatch?orderId="+1)).andExpect(status().isBadRequest()).andReturn();
        mockMvc.perform(patch("/api/inventory/plant/100/deliver?orderId="+1)).andExpect(status().isBadRequest()).andReturn();
        mockMvc.perform(patch("/api/inventory/plant/100/reject?orderId="+1)).andExpect(status().isBadRequest()).andReturn();

        result = mockMvc.perform(patch("/api/inventory/plant/100/return?orderId="+1)).andExpect(status().isOk()).andReturn();
        itemDTO = mapper.readValue(result.getResponse().getContentAsString(), new TypeReference<PlantInventoryItemDTO>() {});
        assertThat(itemDTO.getStatus()).isEqualTo(PlantStatus.AVAILABLE);

        mockMvc.perform(patch("/api/inventory/plant/100/dispatch?orderId="+1)).andExpect(status().isOk()).andReturn();
        result = mockMvc.perform(patch("/api/inventory/plant/100/reject?orderId="+1)).andExpect(status().isOk()).andReturn();
        itemDTO = mapper.readValue(result.getResponse().getContentAsString(), new TypeReference<PlantInventoryItemDTO>() {});
        assertThat(itemDTO.getStatus()).isEqualTo(PlantStatus.REJECTED_BY_CUSTOMER);
    }

    @Test
    public void testSendSubmitOnReturnPlantItem() throws Exception {
        mockMvc.perform(patch("/api/inventory/plant/100/dispatch?orderId="+1)).andExpect(status().isOk()).andReturn();
        mockMvc.perform(patch("/api/inventory/plant/100/deliver?orderId="+1)).andExpect(status().isOk()).andReturn();
        mockMvc.perform(patch("/api/inventory/plant/100/return?orderId="+1)).andExpect(status().isOk()).andReturn();


        verify(mockExternalAPICommunicator, times(1)).submitInvoice(Mockito.any(Invoice.class));
    }


    @Test
    public void testGetPlantInventoryItemPrice() throws Exception {
        PlantInventoryItem item = plantInventoryItemRepository.findById(100l).orElseThrow(() -> new PlantInventoryItemNotFoundException(100l));

        String result = mockMvc.perform(get("/api/inventory/plant/" + item.getId().toString() + "/price")
                .param("startDate", "2020-07-10")
                .param("endDate", "2020-07-20"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        BigDecimal expectedPrice = item.getPlantInfo().getPrice().multiply(BigDecimal.valueOf(10));
        Map<String, BigDecimal> expectedMap = new HashMap<>();
        expectedMap.put("price", expectedPrice);
        ObjectMapper objectMapper = new ObjectMapper();

        assertThat(result).isEqualTo(objectMapper.writeValueAsString(expectedMap));
    }

    @Test
    public void testGetPlantInventoryItemPriceWrongPeriod() throws Exception {
        PlantInventoryItem item = plantInventoryItemRepository.findById(100l).orElseThrow(() -> new PlantInventoryItemNotFoundException(100l));

        String result = mockMvc.perform(get("/api/inventory/plant/" + item.getId().toString() + "/price")
                .param("startDate", "2020-07-21")
                .param("endDate", "2020-07-20"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, BigDecimal> expectedMap = new HashMap<>();
        expectedMap.put("price", BigDecimal.valueOf(0));
        ObjectMapper objectMapper = new ObjectMapper();

        assertThat(result).isEqualTo(objectMapper.writeValueAsString(expectedMap));
    }

    private PlantInventoryItemDTO setupPlantInventoryItemDTOInvalidEntry() {
        PlantInventoryItemDTO itemDTO = new PlantInventoryItemDTO();
        itemDTO.setEquipmentCondition(EquipmentCondition.SERVICEABLE);
        itemDTO.setSerialNumber("Z01");
        itemDTO.setPlantInfo(null);
        return itemDTO;
    }

    private PlantInventoryItemDTO setupUpdatedPlantInventoryItemDTO() {
        PlantInventoryItemDTO itemDTO = new PlantInventoryItemDTO();
        itemDTO.setEquipmentCondition(EquipmentCondition.UNSERVICEABLEREPAIRABLE);
        itemDTO.setSerialNumber(null);
        itemDTO.setPlantInfo(null);
        return itemDTO;
    }

    private PlantInventoryItemDTO setupNewPlantInventoryItemDTO() {
        PlantInventoryItemDTO itemDTO = new PlantInventoryItemDTO();
        PlantInventoryEntry entry = plantInventoryEntryRepository.findById(100L).orElse(null);
        itemDTO.setPlantInfo(plantInventoryEntryAssembler.toResource(entry));
        itemDTO.setEquipmentCondition(EquipmentCondition.SERVICEABLE);
        itemDTO.setSerialNumber("Z01");
        return itemDTO;
    }

    private PlantInventoryEntryDTO setupNewPlantInventoryEntryDTO() {
        PlantInventoryEntryDTO entryDTO = new PlantInventoryEntryDTO();
        entryDTO.setName("name");
        entryDTO.setDescription("desc");
        entryDTO.setPrice(BigDecimal.valueOf(100L));
        return entryDTO;
    }

    private PlantInventoryEntryDTO setupPlantInventoryEntryDTOInvalidEntry() {
        PlantInventoryEntryDTO entryDTO = new PlantInventoryEntryDTO();
        entryDTO.setName("name");
        entryDTO.setPrice(null);
        entryDTO.setDescription("desc");
        return entryDTO;
    }

    private PlantInventoryEntryDTO setupUpdatedPlantInventoryEntryDTO() {
        PlantInventoryEntryDTO entryDTO = new PlantInventoryEntryDTO();
        entryDTO.setName("newName");
        entryDTO.setDescription(null);
        entryDTO.setPrice(null);
        return entryDTO;
    }

    private String executeGetPlantAvailabilityAndExpectSuccess(Long plantId) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/inventory/plant/" + plantId.toString())
                .param("startDate", "2020-07-10")
                .param("endDate", "2020-07-20"))
                .andExpect(status().isOk())
                .andReturn();
        return result.getResponse().getContentAsString();
    }
}
