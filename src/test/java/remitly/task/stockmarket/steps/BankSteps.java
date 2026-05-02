package remitly.task.stockmarket.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import remitly.task.stockmarket.dto.BankResponse;
import remitly.task.stockmarket.dto.StockDto;

import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BankSteps {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    public BankSteps(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    public void seedBank(String stockName, int quantity) throws Exception {
        seedBank(new StockDto(stockName, quantity));
    }

    public void seedBank(StockDto... stocks) throws Exception {
        BankResponse request = new BankResponse(Arrays.asList(stocks));
        mockMvc.perform(post("/stocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    public BankResponse getBankState() throws Exception {
        String response = mockMvc.perform(get("/stocks"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readValue(response, BankResponse.class);
    }

    public int getStockQuantity(String stockName) throws Exception {
        return getBankState().stocks().stream()
                .filter(s -> s.name().equals(stockName))
                .map(StockDto::quantity)
                .findFirst()
                .orElse(0);
    }
}