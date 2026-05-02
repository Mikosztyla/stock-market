package remitly.task.stockmarket.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import remitly.task.stockmarket.dto.StockOperationRequest;
import remitly.task.stockmarket.dto.WalletResponse;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class WalletSteps {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    public WalletSteps(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    public void buyStock(String walletId, String stockName) throws Exception {
        performOperation(walletId, stockName, "buy")
                .andExpect(status().isOk());
    }

    public void sellStock(String walletId, String stockName) throws Exception {
        performOperation(walletId, stockName, "sell")
                .andExpect(status().isOk());
    }

    public ResultActions buyStockExpectingError(String walletId, String stockName) throws Exception {
        return performOperation(walletId, stockName, "buy");
    }

    public ResultActions sellStockExpectingError(String walletId, String stockName) throws Exception {
        return performOperation(walletId, stockName, "sell");
    }

    public ResultActions performOperation(String walletId, String stockName, String type) throws Exception {
        StockOperationRequest request = new StockOperationRequest(type);
        return mockMvc.perform(post("/wallets/{walletId}/stocks/{stock}", walletId, stockName)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    public WalletResponse getWallet(String walletId) throws Exception {
        String response = mockMvc.perform(get("/wallets/{walletId}", walletId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readValue(response, WalletResponse.class);
    }

    public int getStockQuantity(String walletId, String stockName) throws Exception {
        String response = mockMvc.perform(get("/wallets/{walletId}/stocks/{stock}", walletId, stockName))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return Integer.parseInt(response);
    }
}