package com.lnatit.chord.result.context;

import com.lnatit.chord.result.Collector;
import com.lnatit.chord.result.ConflictRisk;
import com.lnatit.chord.result.RiskEntry;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ContextCollector implements Collector<ConflictRisk.Packed> {
    private final StateTag state;
    @Nullable
    private RiskEntry.Simple<InterceptTag> intercept;
    @Nullable
    private RiskEntry.Simple<RedirectTag> redirect;
    @Nullable
    private RiskEntry.Simple<ResourceTag> resource;
    @Nullable
    private RiskEntry.Simple<IntentTag> intent;
    @Nullable
    private RiskEntry.Simple<ModalityTag> modality;

    public ContextCollector(StateTag state) {
        this.state = state;
    }

    public void setIntercept(RiskEntry.Simple<InterceptTag> intercept) {
        this.intercept = intercept;
    }

    public void setRedirect(RiskEntry.Simple<RedirectTag> redirect) {
        this.redirect = redirect;
    }

    public void setResource(RiskEntry.Simple<ResourceTag> resource) {
        this.resource = resource;
    }

    public void setIntent(RiskEntry.Simple<IntentTag> intent) {
        this.intent = intent;
    }

    public void setModality(RiskEntry.Simple<ModalityTag> modality) {
        this.modality = modality;
    }

    public StateTag state() {
        return state;
    }

    @Nullable
    public RiskEntry.Simple<InterceptTag> intercept() {
        return intercept;
    }

    @Nullable
    public RiskEntry.Simple<RedirectTag> redirect() {
        return redirect;
    }

    @Nullable
    public RiskEntry.Simple<ResourceTag> resource() {
        return resource;
    }

    @Nullable
    public RiskEntry.Simple<IntentTag> intent() {
        return intent;
    }

    @Nullable
    public RiskEntry.Simple<ModalityTag> modality() {
        return modality;
    }

    public ConflictRisk.Packed collect() {
        List<ConflictRisk> risks = new ArrayList<>();
        risks.add(state.toRisk());
        for (ConflictRisk field : new ConflictRisk[]{intercept, redirect, resource, intent, modality}) {
            if (field != null) {
                risks.add(field);
            } else break;
        }
        return new ConflictRisk.Packed(risks);
    }
}
