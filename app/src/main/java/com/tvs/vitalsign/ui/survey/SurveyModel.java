package com.tvs.vitalsign.ui.survey;

abstract class SurveyModel {
    public String id;
    public String question;
    public SurveyType type;

    public SurveyModel(String id, String question, SurveyType type) {
        this.id = id;
        this.question = question;
        this.type = type;
    }

    public abstract boolean isTriggered();

    enum SurveyType {
        YES_OR_NO, MULTI_CHOICE
    }
}
