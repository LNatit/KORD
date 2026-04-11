package com.lnatit.chord.result;

public sealed interface ConflictInfo permits ConflictRisk, ConflictInfo.MeltdownWrapper
{
    void attachTo(ConflictCollector collector);

    static ConflictInfo meltdown(ConflictInfo info) {
        return new MeltdownWrapper(info);
    }

    record MeltdownWrapper(ConflictInfo info) implements ConflictInfo
    {
        @Override
        public void attachTo(ConflictCollector collector) {
            info.attachTo(collector);
            collector.setFinished();
        }
    }
}
