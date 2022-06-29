package com.tvs.vitalsign.ui.survey;

import java.util.List;
import java.util.stream.Collectors;

class MultiChoiceSurveyModel extends SurveyModel {
    public List<SurveyExampleModel> examples;
    public int multiChoiceTriggerCondition;

    public MultiChoiceSurveyModel(String id, String question, List<String> examples, int multiChoiceTriggerCondition) {
        super(id, question, SurveyType.MULTI_CHOICE);
        this.examples = examples.stream().map(SurveyExampleModel::new)
                .collect(Collectors.toList());
        this.multiChoiceTriggerCondition = multiChoiceTriggerCondition;
    }

    public List<String> getSelectedExamples() {
        return examples.stream()
                .filter(model -> model.selected)
                .map(model -> model.text)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isTriggered() {
        return multiChoiceTriggerCondition <= examples.stream().filter(model -> model.selected).count();
    }
}
