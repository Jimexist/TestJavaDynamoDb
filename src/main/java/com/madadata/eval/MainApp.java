package com.madadata.eval;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.AttributeUpdate;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.QueryFilter;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import io.dropwizard.jackson.Jackson;
import java.util.Map;
import java.util.UUID;

/**
 * Created by jiayu on 8/29/16.
 */
public class MainApp {

    private static final ObjectMapper objectMapper = Jackson.newObjectMapper();

    public static void main(String[] args) throws Exception {
        AmazonDynamoDB client = new AmazonDynamoDBClient()
            .withEndpoint("http://localhost:8000");
        DynamoDB dynamoDB = new DynamoDB(client);
        Table table = dynamoDB.getTable("UserProfile");
        try {
            dynamoDB.createTable(new CreateTableRequest()
                .withKeySchema(
                    new KeySchemaElement()
                        .withKeyType(KeyType.HASH)
                        .withAttributeName("id"),
                    new KeySchemaElement()
                        .withKeyType(KeyType.RANGE)
                        .withAttributeName("age"))
                .withAttributeDefinitions(
                    new AttributeDefinition()
                        .withAttributeName("id")
                        .withAttributeType(ScalarAttributeType.S),
                    new AttributeDefinition()
                        .withAttributeName("age")
                        .withAttributeType(ScalarAttributeType.N))
                .withTableName("UserProfile")
                .withProvisionedThroughput(new ProvisionedThroughput()
                    .withReadCapacityUnits(4L)
                    .withWriteCapacityUnits(4L)));
            table.waitForActive();

            final String id = UUID.randomUUID().toString();
            UserProfile userProfile = new UserProfile(id, 21, ImmutableList.of("original id"));

            String json = objectMapper.writeValueAsString(userProfile);

            Map<String, Object> map = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });

            table.putItem(Item.fromMap(map));
            System.err.println(table.getItem(new PrimaryKey("id", id, "age", 21)));

            System.err.println(table.updateItem(new UpdateItemSpec()
                .withPrimaryKey(new PrimaryKey("id", id, "age", 21))
                .withAttributeUpdate(new AttributeUpdate("addresses").put(ImmutableList.of("new id")))
                .withReturnValues(ReturnValue.ALL_NEW)
            ).getItem());

            table.query(new QuerySpec().withHashKey("id", id).withQueryFilters(new QueryFilter("addresses").notExist())).forEach(i -> System.err.println(i.toJSONPretty()));

        } finally {
            table.delete();
        }
    }
}
