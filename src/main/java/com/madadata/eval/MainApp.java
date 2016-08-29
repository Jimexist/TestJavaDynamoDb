package com.madadata.eval;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.AttributeUpdate;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.ImmutableList;
import io.dropwizard.jackson.Jackson;
import java.util.UUID;

/**
 * Created by jiayu on 8/29/16.
 */
public class MainApp {

    private static final ObjectMapper objectMapper = Jackson.newObjectMapper().disable(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS);

    public static void main(String[] args) throws Exception {
        AmazonDynamoDB client = new AmazonDynamoDBClient()
            .withEndpoint("http://localhost:8000");
        DynamoDB dynamoDB = new DynamoDB(client);

        Table table = dynamoDB.getTable("UserProfile");

        dynamoDB.createTable(new CreateTableRequest()
            .withKeySchema(new KeySchemaElement()
                .withKeyType(KeyType.HASH)
                .withAttributeName("id"))
            .withAttributeDefinitions(new AttributeDefinition()
                .withAttributeName("id")
                .withAttributeType(ScalarAttributeType.S))
            .withTableName("UserProfile")
            .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(4L).withWriteCapacityUnits(4L)));
        table.waitForActive();

        final String id = UUID.randomUUID().toString();

        table.putItem(Item.fromJSON(objectMapper.writeValueAsString(new UserProfile(id, ImmutableList.<String>of()))));
        System.out.println(table.getItem(new PrimaryKey("id", id)));

        System.out.println(table.updateItem(new UpdateItemSpec()
            .withPrimaryKey(new PrimaryKey("id", id))
            .withAttributeUpdate(new AttributeUpdate("addresses")
                .addElements("some random address"))
            .withReturnValues(ReturnValue.ALL_NEW)
        ).getItem());

        System.out.println(table.updateItem(new UpdateItemSpec()
            .withPrimaryKey(new PrimaryKey("id", id))
            .withAttributeUpdate(new AttributeUpdate("addresses")
                .addElements("some random address again"))
            .withReturnValues(ReturnValue.ALL_NEW)
        ).getItem());

        table.delete();
    }
}