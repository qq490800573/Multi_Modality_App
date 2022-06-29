package com.tvs.vitalsign.ui.survey;

class YesNoSurveyModel extends SurveyModel{
    public YesNo yesNoTriggerCondition;
    public YesNo answer;

    public YesNoSurveyModel(String id, String question, YesNo yesNoTriggerCondition) {
        super(id, question, SurveyType.YES_OR_NO);
        this.yesNoTriggerCondition = yesNoTriggerCondition;
    }

    @Override
    public boolean isTriggered() {
        return yesNoTriggerCondition == answer;
    }

    enum YesNo {
        YES, NO, NONE
    }
}
