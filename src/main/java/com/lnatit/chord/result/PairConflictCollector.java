package com.lnatit.chord.result;

import java.util.Optional;

public final class PairConflictCollector extends ConflictCollector
{
    private boolean finished = false;

    public void merge(PairConflictCollector collector) {
        this.mergeFrom(collector);
    }

    public <R extends DynamicRisk> Optional<R> getRisk(Class<R> type) {
        return this.getRiskByType(type);
    }

    public void setFinished() {
        this.finished = true;
    }

    public boolean finished() {
        return this.finished;
    }
}


