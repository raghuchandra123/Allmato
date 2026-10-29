package www.almato.com.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import www.almato.com.pojo.Item;
import www.almato.com.pojo.Menu;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class AllRestController {

    @Autowired
    private DynamoDbClient dynamoDbClient;

    @Autowired
    ObjectMapper mapper;

    @GetMapping("/allmato/v1/items")
    public Menu getAllItems(@RequestParam(required = false) String lstKeEncrpt) throws Exception {

        Map<String, String> replace = new HashMap<>();
        replace.put("#item", "item");

        ScanRequest.Builder requestBuilder = ScanRequest.builder()
                .tableName("CombinedMenu")
                .expressionAttributeNames(replace)
                .projectionExpression("#item, price")
                .limit(10);

        if (lstKeEncrpt != null && !lstKeEncrpt.isEmpty()) {
            byte[] decode = Base64.getUrlDecoder().decode(lstKeEncrpt);
            String jsonStringLstKey = new String(decode, StandardCharsets.UTF_8);

            JsonNode root = mapper.readTree(jsonStringLstKey);

            Map<String, AttributeValue> lastKey = new HashMap<>();

            root.properties().forEach(entry -> {
                JsonNode value = entry.getValue();

                if (value.has("S")) {
                    lastKey.put(entry.getKey(),
                            AttributeValue.fromS(value.get("S").asText()));
                } else if (value.has("N")) {
                    lastKey.put(entry.getKey(),
                            AttributeValue.fromN(value.get("N").asText()));
                }
            });
            requestBuilder.exclusiveStartKey(lastKey);
        }

        ScanRequest request = requestBuilder.build();
        ScanResponse response;
        try {
            response = dynamoDbClient.scan(request);
        } catch (Exception ex) {
            System.out.println("Some thing went wrong when calling dynamo db");
            throw new Exception(ex);
        }

        List<Map<String, AttributeValue>> rows = response.items();
        Map<String, AttributeValue> lastEvaluatedKey = response.lastEvaluatedKey();
        Map<String, Object> betterConvert = new HashMap<>();

        for (Map.Entry<String, AttributeValue> entry : lastEvaluatedKey.entrySet()) {
            if (entry.getValue().s() != null) {
                betterConvert.put(entry.getKey(), Map.of("S", entry.getValue().s()));
            }
            else {
                betterConvert.put(entry.getKey(), Map.of("N", entry.getValue().n()));
            }
        }

        String jsonStringLstKey = mapper.writeValueAsString(betterConvert);

        Menu menu = new Menu();
        menu.setLstKeEncrpt(Base64.getUrlEncoder().encodeToString(jsonStringLstKey.getBytes(StandardCharsets.UTF_8)));

        if (lastEvaluatedKey.size() > 0) {
            menu.setCanQueryForMore(true);
        }
        else {
            menu.setCanQueryForMore(false);
        }

        for (Map<String, AttributeValue> element : rows) {
            System.out.println(element);
            Item item = new Item();
            for (Map.Entry<String, AttributeValue> entry: element.entrySet()) {
                if (entry.getKey().toLowerCase().equals("item")) {
                    item.setItem(entry.getValue().s());
                }
                else {
                    item.setPrice(Integer.parseInt(entry.getValue().n()));
                }
            }
            menu.addItem(item);
        }
        System.out.println("------------------Done------------------------");
        return menu;
    }

}
