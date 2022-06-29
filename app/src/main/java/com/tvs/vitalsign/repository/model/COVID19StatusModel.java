package com.tvs.vitalsign.repository.model;

import java.text.DecimalFormat;

public class COVID19StatusModel {

    public String decideCnt = "";
    public String decideCntDiff = "";
    public String clearCnt = "";
    public String clearCntDiff = "";
    public String deathCnt = "";
    public String deathCntDiff = "";

    public COVID19StatusModel(
            int _decideCnt,
            int _decideCntDiff,
            int _clearCnt,
            int _clearCntDiff,
            int _deathCnt,
            int _deathCntDiff
    ) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        decideCnt = _decideCnt == 0 ? "-" : formatter.format(_decideCnt);
        decideCntDiff = _decideCntDiff == 0 ? "(-)" : "(+" + formatter.format(_decideCntDiff) + ")";
        clearCnt = _clearCnt == 0 ? "-" : formatter.format(_clearCnt);
        clearCntDiff = _clearCntDiff == 0 ? "(-)" : "(+" + formatter.format(_clearCntDiff) + ")";
        deathCnt = _deathCnt == 0 ? "-" : formatter.format(_deathCnt);
        deathCntDiff = _deathCntDiff == 0 ? "(-)" : "(+" + formatter.format(_deathCntDiff) + ")";
    }
}
