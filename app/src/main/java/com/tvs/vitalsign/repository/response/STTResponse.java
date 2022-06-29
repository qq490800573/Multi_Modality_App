package com.tvs.vitalsign.repository.response;

import com.google.gson.annotations.SerializedName;

public class STTResponse {
    @SerializedName("results")
    public STTResultResponse response;
    @SerializedName("errorCode")
    public int errorCode;
    @SerializedName("message")
    public String message;

    public class STTResultResponse {
        @SerializedName("text")
        public String text;
    }
}
