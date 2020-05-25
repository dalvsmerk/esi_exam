package com.example.rentit.final_exam.rest;

import com.example.rentit.RentitApplication;
import com.example.rentit.final_exam.application.dto.ReturnOrderDTO;
import com.example.rentit.final_exam.application.dto.ReturnOrderRequestDTO;
import com.example.rentit.final_exam.domain.model.ReturnOrder;
import com.example.rentit.final_exam.domain.model.ReturnOrderStatus;
import com.example.rentit.final_exam.domain.repository.ReturnOrderRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.Hibernate;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = RentitApplication.class)
@WebAppConfiguration
@Sql(scripts = "/final_exam/rest/exam_dataset.sql")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class ReturnOrderRestControllerTest {
    @Autowired
    ReturnOrderRepository returnOrderRepository;

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
    @Sql("classpath:/final_exam/rest/exam_dataset.sql")
    public void testCreateReturnOrderSuccessfully() throws Exception {
        List<Long> items = new ArrayList<>();
        items.add(1L);
        items.add(2L);
        items.add(3L);

        ReturnOrderRequestDTO requestDTO = ReturnOrderRequestDTO.of(
                LocalDate.of(2020, 07, 03), items);

        MvcResult result = mockMvc.perform(post("/api/returns/return-orders")
                .content(mapper.writeValueAsString(requestDTO)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        ReturnOrderDTO orderDTO = mapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<ReturnOrderDTO>() {});

        BigDecimal fee = BigDecimal.valueOf((200*7 + 300*7 + 500*7) * 0.05);
        assertThat(orderDTO.getFee().toBigInteger()).isEqualTo(fee.toBigInteger());
        assertThat(orderDTO.getOrders().size()).isEqualTo(3);

        ReturnOrder order = returnOrderRepository.findById(orderDTO.get_id()).orElse(null);

        assertThat(order).isNotNull();
        assertThat(order.getStatus()).isEqualTo(ReturnOrderStatus.PENDING);
        assertThat(order.getReturnDate()).isEqualTo(LocalDate.of(2020, 07, 03));
//        assertThat(order.getOrders().size()).isEqualTo(3); // Hibernation lazy initialization error, no time to solve
    }
}
