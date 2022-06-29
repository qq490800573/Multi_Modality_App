package com.tvs.vitalsign.repository.response;

import com.google.gson.annotations.SerializedName;

public class OpenApiResponse {
    @SerializedName("response")
    public OpenApiBody response;
    public class OpenApiBody {
        @SerializedName("body")
        public COVID19StatusResponse body;
    }
}
