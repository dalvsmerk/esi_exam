package com.example.rentit.sales;

import com.example.rentit.RentitApplication;
import com.example.rentit.common.application.dto.BusinessPeriodDTO;
import com.example.rentit.common.application.dto.SimpleErrorDTO;
import com.example.rentit.common.application.dto.ValidationErrorDTO;
import com.example.rentit.common.application.exception.ExternalApiCallException;
import com.example.rentit.common.application.service.ExternalAPICommunicator;
import com.example.rentit.inventory.application.service.PlantInventoryItemAssembler;
import com.example.rentit.inventory.domain.model.EquipmentCondition;
import com.example.rentit.inventory.domain.model.PlantInventoryEntry;
import com.example.rentit.inventory.domain.model.PlantInventoryItem;
import com.example.rentit.inventory.domain.model.PlantStatus;
import com.example.rentit.inventory.domain.repository.PlantInventoryEntryRepository;
import com.example.rentit.inventory.domain.repository.PlantInventoryItemRepository;
import com.example.rentit.sales.application.dto.InvoiceDTO;
import com.example.rentit.sales.application.dto.PurchaseOrderDTO;
import com.example.rentit.sales.application.exception.PurchaseOrderNotFoundException;
import com.example.rentit.sales.domain.model.Invoice;
import com.example.rentit.sales.domain.model.InvoiceStatus;
import com.example.rentit.sales.domain.model.POStatus;
import com.example.rentit.sales.domain.model.PurchaseOrder;
import com.example.rentit.sales.domain.repository.InvoiceRepository;
import com.example.rentit.sales.domain.repository.PurchaseOrderRepository;
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

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = RentitApplication.class)
@WebAppConfiguration
@Sql(scripts = "/sales/rest/po-dataset.sql")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class SalesRestControllerTest {
    @MockBean
    ExternalAPICommunicator mockExternalAPICommunicator;

    @Autowired
    PlantInventoryItemRepository itemRepository;

    @Autowired
    PlantInventoryEntryRepository plantInventoryEntryRepository;

    @Autowired
    InvoiceRepository invoiceRepository;

    @Autowired
    PlantInventoryItemAssembler itemAssembler;

    @Autowired
    PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private WebApplicationContext wac;
    private MockMvc mockMvc;

    @Autowired @Qualifier("_halObjectMapper")
    ObjectMapper mapper;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        Mockito.when(mockExternalAPICommunicator.acceptPO(Mockito.any()))
                .thenAnswer(any -> true);
        Mockito.when(mockExternalAPICommunicator.rejectPO(Mockito.any(), Mockito.any()))
                .thenAnswer(any -> true);
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Test
    public void testCreatePurchaseOrder() throws Exception {
        LocalDate startDate = LocalDate.of(2020, 8, 1);
        LocalDate endDate = LocalDate.of(2020, 8, 10);

        BusinessPeriodDTO periodDTO = BusinessPeriodDTO.of(startDate, endDate);

        PlantInventoryItem item = itemRepository.findById(1L).orElse(null);
        PurchaseOrderDTO poDTO = PurchaseOrderDTO.of(
                1055L, periodDTO, itemAssembler.toResource(item), null, null, 1055l);

        MvcResult result = mockMvc.perform(post("/api/sales/orders")
                .content(mapper.writeValueAsString(poDTO)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        PurchaseOrderDTO resultDTO = mapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<PurchaseOrderDTO>() {});

        assertThat(resultDTO.get_id()).isNotNull();
        assertThat(resultDTO.getPlant().get_id()).isEqualTo(1L);
        assertThat(resultDTO.getStatus()).isEqualTo(POStatus.PENDING);
        assertThat(resultDTO.getPlantReplaced()).isEqualTo(false);
        assertThat(resultDTO.getRentalPeriod().getStartDate()).isEqualTo(startDate);
        assertThat(resultDTO.getRentalPeriod().getEndDate()).isEqualTo(endDate);
    }

    @Test
    public void testCreatePurchaseOrderPlantNotFound() throws Exception {
        LocalDate startDate = LocalDate.of(2020, 6, 1);
        LocalDate endDate = LocalDate.of(2020, 6, 10);

        BusinessPeriodDTO periodDTO = BusinessPeriodDTO.of(startDate, endDate);

        PlantInventoryEntry entry = new PlantInventoryEntry();
        entry.setId(1L);
        PlantInventoryItem item = PlantInventoryItem.of(
                600L, "123", null, entry, null);
        PurchaseOrderDTO poDTO = PurchaseOrderDTO.of(
                1066L, periodDTO, itemAssembler.toResource(item), null, null, 1066l);

        MvcResult result = mockMvc.perform(post("/api/sales/orders")
                .content(mapper.writeValueAsString(poDTO)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();

        SimpleErrorDTO resultDTO = mapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<SimpleErrorDTO>() {});

        assertThat(resultDTO.getMessage()).isEqualTo("Plant not found (id: 600)");
    }

    @Test
    public void testCreatePurchaseOrderPlantNotFoundNull() throws Exception {
        LocalDate startDate = LocalDate.of(2020, 6, 1);
        LocalDate endDate = LocalDate.of(2020, 6, 10);

        BusinessPeriodDTO periodDTO = BusinessPeriodDTO.of(startDate, endDate);

        PurchaseOrderDTO poDTO = PurchaseOrderDTO.of(
                1013L, periodDTO, null, null, null, 1013l);

        MvcResult result = mockMvc.perform(post("/api/sales/orders")
                .content(mapper.writeValueAsString(poDTO)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();

        SimpleErrorDTO resultDTO = mapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<SimpleErrorDTO>() {});

        assertThat(resultDTO.getMessage()).isEqualTo("Plant not found (id: null)");
    }

    @Test
    public void testCreatePurchaseOrderNotAvailableRejected() throws Exception {
        LocalDate startDate = LocalDate.of(2020, 6, 1);
        LocalDate endDate = LocalDate.of(2020, 6, 6);

        BusinessPeriodDTO periodDTO = BusinessPeriodDTO.of(startDate, endDate);

        PlantInventoryItem item = itemRepository.findById(2L).orElse(null);
        PurchaseOrderDTO poDTO = PurchaseOrderDTO.of(
                1012L, periodDTO, itemAssembler.toResource(item), null, null, 1012l);

        MvcResult result = mockMvc.perform(post("/api/sales/orders")
                .content(mapper.writeValueAsString(poDTO)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        SimpleErrorDTO resultDTO = mapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<SimpleErrorDTO>() {});

        assertThat(resultDTO.getMessage()).isEqualTo("Plant 2 is not available from 2020-06-01 to 2020-06-06");
    }

    @Test
    public void testCreatePurchaseOrderReplacePlant() throws Exception {
        LocalDate startDate = LocalDate.of(2020, 6, 15);
        LocalDate endDate = LocalDate.of(2020, 6, 20);

        BusinessPeriodDTO periodDTO = BusinessPeriodDTO.of(startDate, endDate);

        PlantInventoryItem item = itemRepository.findById(3L).orElse(null);
        PurchaseOrderDTO poDTO = PurchaseOrderDTO.of(
                1011L, periodDTO, itemAssembler.toResource(item), null, null, 1011l);

        MvcResult result = mockMvc.perform(post("/api/sales/orders")
                .content(mapper.writeValueAsString(poDTO)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        PurchaseOrderDTO resultDTO = mapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<PurchaseOrderDTO>() {});

        assertThat(resultDTO.getPlant().get_id()).isEqualTo(4L);
        assertThat(resultDTO.getPlantReplaced()).isEqualTo(true);
    }

    @Test
    public void testCreatePurchaseOrderReplacePlantNotAvailable() throws Exception {
        LocalDate startDate = LocalDate.of(2020, 6, 15);
        LocalDate endDate = LocalDate.of(2020, 6, 20);

        BusinessPeriodDTO periodDTO = BusinessPeriodDTO.of(startDate, endDate);

        PlantInventoryItem item = itemRepository.findById(6L).orElse(null);
        PurchaseOrderDTO poDTO = PurchaseOrderDTO.of(
                1010L, periodDTO, itemAssembler.toResource(item), null, null, 1010l);

        MvcResult result = mockMvc.perform(post("/api/sales/orders")
                .content(mapper.writeValueAsString(poDTO)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        SimpleErrorDTO resultDTO = mapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<SimpleErrorDTO>() {});

        assertThat(resultDTO.getMessage()).isEqualTo(
                "No available items to replace plant 6 from 2020-06-15 to 2020-06-20");
    }

    @Test
    public void testEditPurchaseOrderPeriod() throws Exception {
        PurchaseOrder order = purchaseOrderRepository.findById(2l).orElseThrow(() -> new PurchaseOrderNotFoundException(2l));
        assertThat(order.getRentalPeriod().getStartDate()).isEqualTo("2020-06-01");
        assertThat(order.getRentalPeriod().getEndDate()).isEqualTo("2020-06-05");

        MvcResult result = mockMvc.perform(patch("/api/sales/orders/2")
                .param("startDate", "2020-06-01")
                .param("endDate", "2020-06-20"))
                .andExpect(status().isOk())
                .andReturn();

        PurchaseOrderDTO resultDTO = mapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<PurchaseOrderDTO>() {});

        assertThat(resultDTO.getStatus()).isNotEqualTo(POStatus.REJECTED);
        assertThat(resultDTO.getRentalPeriod().getStartDate()).isEqualTo("2020-06-01");
        assertThat(resultDTO.getRentalPeriod().getEndDate()).isEqualTo("2020-06-20");

        // period when plant is not available
        // purchase order gets rejected
        result = mockMvc.perform(patch("/api/sales/orders/2")
                .param("startDate", "2020-06-25")
                .param("endDate", "2020-07-05"))
                .andExpect(status().isOk())
                .andReturn();

        resultDTO = mapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<PurchaseOrderDTO>() {});

        assertThat(resultDTO.getStatus()).isEqualTo(POStatus.REJECTED);
    }

    @Test
    public void testExtendPurchaseOrder() throws Exception {
        PurchaseOrder order = purchaseOrderRepository.findById(2l).orElseThrow(() -> new PurchaseOrderNotFoundException(2l));
        assertThat(order.getRentalPeriod().getStartDate()).isEqualTo("2020-06-01");
        assertThat(order.getRentalPeriod().getEndDate()).isEqualTo("2020-06-05");

        MvcResult result = mockMvc.perform(post("/api/sales/orders/2/extend")
                .param("endDate", "2020-06-20"))
                .andExpect(status().isOk())
                .andReturn();

        PurchaseOrderDTO resultDTO = mapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<PurchaseOrderDTO>() {
                });

        assertThat(resultDTO.getRentalPeriod().getStartDate()).isEqualTo("2020-06-01");
        assertThat(resultDTO.getRentalPeriod().getEndDate()).isEqualTo("2020-06-20");
    }

    @Test
    public void testExtendPurchaseOrderWithNotValidEndDate() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/sales/orders/2/extend")
                .param("endDate", "2020-06-03"))
                .andExpect(status().isBadRequest())
                .andReturn();

        ValidationErrorDTO errorDTO = mapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<ValidationErrorDTO>() {
                });

        assertThat(errorDTO.getViolations().get("endDate")).isEqualTo("end date cannot be set before the current end date");
    }

    @Test
    public void testExtendPurchaseOrderPlantIsNotAvailableAndNoOtherToReplace() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/sales/orders/2/extend")
                .param("endDate", "2020-07-05"))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertThat(result.getResolvedException().getMessage()).isEqualTo("Plant 1 is not available from 2020-06-05 to 2020-07-05 and no other available items to replace it for requested period");
    }

    @Test
    public void testExtendPurchaseOrderPlantIsNotAvailableAndReplacedByAnother() throws Exception {
        PurchaseOrder order = purchaseOrderRepository.findById(2l).orElseThrow(() -> new PurchaseOrderNotFoundException(2l));
        assertThat(order.getPlantReplaced()).isFalse();
        assertThat(order.getRentalPeriod().getEndDate()).isEqualTo("2020-06-05");

        // add new plantInventoryItem of the same plantInventoryInfo
        PlantInventoryEntry entry = plantInventoryEntryRepository.findById(1l).orElse(null);
        PlantInventoryItem item = PlantInventoryItem.of(null, "A08", EquipmentCondition.SERVICEABLE, entry, PlantStatus.AVAILABLE);
        itemRepository.saveAndFlush(item);

        MvcResult result = mockMvc.perform(post("/api/sales/orders/2/extend")
                .param("endDate", "2020-07-05"))
                .andExpect(status().isOk())
                .andReturn();

        PurchaseOrderDTO resultDTO = mapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<PurchaseOrderDTO>() {});

        assertThat(resultDTO.getPlantReplaced()).isTrue();
        assertThat(resultDTO.getRentalPeriod().getEndDate()).isEqualTo("2020-07-05");
    }

    @Test
    public void testAcceptPurchaseOrder() throws Exception {
        PurchaseOrder order = purchaseOrderRepository.findById(100l).orElseThrow(() -> new PurchaseOrderNotFoundException(100l));

        MvcResult result = mockMvc.perform(patch("/api/sales/orders/" + order.getId() + "/accept"))
                .andExpect(status().isOk())
                .andReturn();

        PurchaseOrderDTO resultDTO = mapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<PurchaseOrderDTO>() {});

        assertThat(resultDTO.getStatus()).isEqualTo(POStatus.ACCEPTED);
    }

    @Test
    public void testRejectInvalidPurchaseOrder() throws Exception {
        PurchaseOrder order = purchaseOrderRepository.findById(102l).orElseThrow(() -> new PurchaseOrderNotFoundException(102l));

        MvcResult result = mockMvc.perform(patch("/api/sales/orders/" + order.getId() + "/accept"))
                .andExpect(status().isBadRequest())
                .andReturn();

        SimpleErrorDTO resultDTO = mapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<SimpleErrorDTO>() {});

        assertThat(resultDTO.getMessage())
                .isEqualTo("Status of the purchase order 102 cannot be changed to ACCEPTED because the plant was ACCEPTED");
    }

    @Test
    public void testRejectPurchaseOrder() throws Exception {
        PurchaseOrder order = purchaseOrderRepository.findById(100l).orElseThrow(() -> new PurchaseOrderNotFoundException(100l));

        MvcResult result = mockMvc.perform(patch("/api/sales/orders/" + order.getId() + "/reject?rejectReason=sosi"))
                .andExpect(status().isOk())
                .andReturn();

        PurchaseOrderDTO resultDTO = mapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<PurchaseOrderDTO>() {});

        assertThat(resultDTO.getStatus()).isEqualTo(POStatus.REJECTED);
    }

    @Test
    public void testAcceptInvalidPurchaseOrder() throws Exception {
        PurchaseOrder order = purchaseOrderRepository.findById(102l).orElseThrow(() -> new PurchaseOrderNotFoundException(102l));

        MvcResult result = mockMvc.perform(patch("/api/sales/orders/" + order.getId() + "/reject?rejectReason=sosi"))
                .andExpect(status().isBadRequest())
                .andReturn();

        SimpleErrorDTO resultDTO = mapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<SimpleErrorDTO>() {});

        assertThat(resultDTO.getMessage())
                .isEqualTo("Status of the purchase order 102 cannot be changed to REJECTED because the plant was ACCEPTED");
    }

    @Test
    public void testCancelPurchaseOrder() throws Exception {
        MvcResult result = mockMvc.perform(patch("/api/sales/orders/100/cancel"))
                .andExpect(status().isOk())
                .andReturn();

        PurchaseOrderDTO resultDTO = mapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<PurchaseOrderDTO>() {});

        assertThat(resultDTO.getStatus()).isEqualTo(POStatus.CANCELLED);
    }

    @Test
    public void testCancelPurchaseOrderRejectedPlantDispatched() throws Exception {
        MvcResult result = mockMvc.perform(patch("/api/sales/orders/101/cancel"))
                .andExpect(status().isBadRequest())
                .andReturn();

        SimpleErrorDTO resultDTO = mapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<SimpleErrorDTO>() {});

        assertThat(resultDTO.getMessage())
                .isEqualTo("Purchase order 101 cannot be cancelled because the plant was dispatched");
    }

    @Test
    public void testCancelPurchaseOrderRejectedPlantDelivered() throws Exception {
        MvcResult result = mockMvc.perform(patch("/api/sales/orders/102/cancel"))
                .andExpect(status().isBadRequest())
                .andReturn();

        SimpleErrorDTO resultDTO = mapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<SimpleErrorDTO>() {});

        assertThat(resultDTO.getMessage())
                .isEqualTo("Purchase order 102 cannot be cancelled because the plant was delivered");
    }

    @Test
    public void testCancelPurchaseOrderRejectedPlantRejectedByCustomer() throws Exception {
        MvcResult result = mockMvc.perform(patch("/api/sales/orders/103/cancel"))
                .andExpect(status().isBadRequest())
                .andReturn();

        SimpleErrorDTO resultDTO = mapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<SimpleErrorDTO>() {});

        assertThat(resultDTO.getMessage())
                .isEqualTo("Purchase order 103 cannot be cancelled because the plant was rejected by customer");
    }

    @Test
    public void testCancelPurchaseOrderNotFound() throws Exception {
        MvcResult result = mockMvc.perform(patch("/api/sales/orders/999/cancel"))
                .andExpect(status().isNotFound())
                .andReturn();

        SimpleErrorDTO resultDTO = mapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<SimpleErrorDTO>() {});

        assertThat(resultDTO.getMessage())
                .isEqualTo("PurchaseOrder not found (id: 999)");
    }

    @Test
    public void testGetPurchaseOrders() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/sales/orders"))
                .andExpect(status().isOk())
                .andReturn();

        List<PurchaseOrderDTO> resultDTOs = mapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<List<PurchaseOrderDTO>>() {});

        assertThat(resultDTOs.size()).isEqualTo(8);

    }

    @Test
    public void testPayInvoice() throws Exception {
        mockMvc.perform(post("/api/sales/invoices/1/pay"))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult result = mockMvc.perform(post("/api/sales/invoices/1/pay"))
                .andExpect(status().isBadRequest())
                .andReturn();
        assertThat(result.getResolvedException().getMessage()).isEqualTo("Invoice is already paid");
    }

    @Test
    public void testPayInvoiceNotFound() throws Exception {
        mockMvc.perform(post("/api/sales/invoices/100/pay"))
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    public void testRejectInvoice() throws Exception {
        mockMvc.perform(post("/api/sales/invoices/1/reject"))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult result = mockMvc.perform(post("/api/sales/invoices/1/reject"))
                .andExpect(status().isBadRequest())
                .andReturn();
        assertThat(result.getResolvedException().getMessage()).isEqualTo("Invoice is already rejected");

        result = mockMvc.perform(post("/api/sales/invoices/2/reject"))
                .andExpect(status().isBadRequest())
                .andReturn();
        assertThat(result.getResolvedException().getMessage()).isEqualTo("Invoice is already paid");
    }

    @Test
    public void testRejectInvoiceNotFound() throws Exception {
        mockMvc.perform(post("/api/sales/invoices/100/reject"))
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    public void testGetInvoice() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/sales/invoices/1"))
                .andExpect(status().isOk())
                .andReturn();

        InvoiceDTO resultDTO = mapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<InvoiceDTO>() {});

        assertThat(resultDTO.getPurchaseOrderDTO().get_id()).isEqualTo(1l);
        assertThat(resultDTO.getStatus()).isEqualTo(InvoiceStatus.PENDING);
    }

    @Test
    public void testNotifyIfInvoiceIsPendingAfterRentalPeriodFinished() throws ExternalApiCallException, InterruptedException {
        List<Invoice> pendingInvoices = invoiceRepository.getAllByStatus(InvoiceStatus.PENDING);
        assertThat(pendingInvoices.size()).isGreaterThan(0);

        verify(mockExternalAPICommunicator, times(0)).submitNotificationABoutUnpaidInvoice(Mockito.any(Invoice.class));
        Thread.sleep(1000 * 10);
        verify(mockExternalAPICommunicator, atLeast(1)).submitNotificationABoutUnpaidInvoice(Mockito.any(Invoice.class));
    }
}
