package com.madadata.eval;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Created by jiayu on 8/29/16.
 */
public class UserProfile {

    private final String id;

    private final int age;

    private final List<String> addresses;

    @JsonCreator
    public UserProfile(@JsonProperty("id") String id,
                       @JsonProperty("age") int age,
                       @JsonProperty("addresses") List<String> addresses) {
        this.id = id;
        this.age = age;
        this.addresses = addresses;
    }

    @JsonProperty
    public int getAge() {
        return age;
    }

    @JsonProperty
    public String getId() {
        return id;
    }

    @JsonProperty
    public List<String> getAddresses() {
        return addresses;
    }
}
