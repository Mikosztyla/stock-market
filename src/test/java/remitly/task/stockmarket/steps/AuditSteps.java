package remitly.task.stockmarket.steps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuditSteps {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    public AuditSteps(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    public List<AuditEntry> getLog() throws Exception {
        String response = mockMvc.perform(get("/log"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode root = objectMapper.readTree(response);
        JsonNode logNode = root.get("log");

        List<AuditEntry> entries = new ArrayList<>();
        for (JsonNode node : logNode) {
            entries.add(new AuditEntry(
                    node.get("type").asText(),
                    node.get("walletId").asText(),
                    node.get("stockName").asText()
            ));
        }
        return entries;
    }

    public record AuditEntry(String type, String walletId, String stockName) {}
}