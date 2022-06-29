package com.tvs.vitalsign.repository.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class COVID19StatusResponse {
    @SerializedName("items")
    public COVID19StatusItem items;
    @SerializedName("totalCount")
    public int totalCount = 0;

    public class COVID19StatusItem {
        @SerializedName("item")
        public List<COVID19Status> statuses;
    }
}
