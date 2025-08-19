package com.example.task;

import com.example.task.Service.CustomerSer;
import com.example.task.controller.ControllerCust;
import com.example.task.model.Customer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ControllerCust.class)
public class ControllerCustTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private CustomerSer customerSer;

    @InjectMocks
    private ControllerCust controllerCust;

    @Test
    public void testCreateCustomer() throws Exception {
        Customer customer = new Customer();
        customer.setFirstName("John");
        customer.setLastName("Doe");

        // Mocking void method, so we use doNothing()
        Mockito.doNothing().when(customerSer).saveCustomer(Mockito.any(Customer.class));

        mockMvc.perform(post("/customers")
                        .contentType("application/json")
                        .content("{\"firstName\": \"John\", \"lastName\": \"Doe\", \"email\": \"john.doe@example.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }
    @Test
    public void testDeleteCustomer() throws Exception {
        Mockito.doNothing().when(customerSer).deleteCustomerByIdSoft(Mockito.anyLong());

        mockMvc.perform(post("/customers/deleteSelected")
                        .contentType("application/json")
                        .content("[1,2,3]"))
                .andExpect(status().isOk())
                .andExpect(content().string("Selected customers have been marked as deleted."));
    }

    @Test
    public void testFindCustomerByName() throws Exception {
        Customer customer = new Customer();
        customer.setFirstName("John");

        Mockito.when(customerSer.searchCustomersByName(Mockito.anyString()))
                .thenReturn(List.of(customer));

        mockMvc.perform(get("/customers/search?name=John"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("John"));
    }
}
