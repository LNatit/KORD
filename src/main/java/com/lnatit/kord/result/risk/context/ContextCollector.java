package com.lnatit.kord.result.risk.context;

import com.lnatit.kord.result.risk.Collector;
import com.lnatit.kord.result.risk.ConflictRisk;
import com.lnatit.kord.result.risk.RiskEntry;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ContextCollector implements Collector<ConflictRisk.Packed>
{
    @Nullable
    private RiskEntry<StateTag> state;
    @Nullable
    private RiskEntry<InterceptTag> intercept;
    @Nullable
    private RiskEntry<RedirectTag> redirect;
    @Nullable
    private RiskEntry<ResourceTag> resource;
    @Nullable
    private RiskEntry<IntentTag> intent;
    @Nullable
    private RiskEntry<ModalityTag> modality;

    public ContextCollector() {
    }

    public void setState(RiskEntry<StateTag> state) {
        this.state = state;
    }

    public void setIntercept(RiskEntry<InterceptTag> intercept) {
        this.intercept = intercept;
    }

    public void setRedirect(RiskEntry<RedirectTag> redirect) {
        this.redirect = redirect;
    }

    public void setResource(RiskEntry<ResourceTag> resource) {
        this.resource = resource;
    }

    public void setIntent(RiskEntry<IntentTag> intent) {
        this.intent = intent;
    }

    public void setModality(RiskEntry<ModalityTag> modality) {
        this.modality = modality;
    }

    public RiskEntry<StateTag> state() {
        return state;
    }

    @Nullable
    public RiskEntry<InterceptTag> intercept() {
        return intercept;
    }

    @Nullable
    public RiskEntry<RedirectTag> redirect() {
        return redirect;
    }

    @Nullable
    public RiskEntry<ResourceTag> resource() {
        return resource;
    }

    @Nullable
    public RiskEntry<IntentTag> intent() {
        return intent;
    }

    @Nullable
    public RiskEntry<ModalityTag> modality() {
        return modality;
    }

    public ConflictRisk.Packed collect() {
        List<ConflictRisk> risks = new ArrayList<>();
        for (ConflictRisk field : new ConflictRisk[]{state, intercept, redirect, resource, intent, modality}) {
            // 符合原始设计约定：流水线运行时第一个null后的字段将全部为null，直接break减少遍历开销
            if (field != null) {risks.add(field);}
            else {break;}
        }
        return new ConflictRisk.Packed(risks);
    }
}
