package com.lnatit.chord.result.context;

import com.lnatit.chord.result.Collector;
import com.lnatit.chord.result.ConflictRisk;
import com.lnatit.chord.result.RiskEntry;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ContextCollector implements Collector<ConflictRisk.Packed>
{
    private final StateTag state;
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

    public ContextCollector(StateTag state) {
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

    public StateTag state() {
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
        risks.add(state.toRisk());
        for (ConflictRisk field : new ConflictRisk[]{intercept, redirect, resource, intent, modality}) {
            if (field != null) {risks.add(field);}
            else {break;}
        }
        return new ConflictRisk.Packed(risks);
    }
}
