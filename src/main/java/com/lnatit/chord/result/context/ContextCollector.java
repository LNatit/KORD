package com.lnatit.chord.result.context;

import com.lnatit.chord.result.ConflictRisk;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ContextCollector
{
    private final StateTag state;
    @Nullable
    private TaggedRisk.Simple<InterceptTag> intercept;
    @Nullable
    private TaggedRisk.Simple<RedirectTag> redirect;
    @Nullable
    private TaggedRisk.Simple<ResourceTag> resource;
    @Nullable
    private TaggedRisk.Simple<IntentTag> intent;
    @Nullable
    private TaggedRisk.Simple<ModalityTag> modality;

    public ContextCollector(StateTag state) {this.state = state;}

    public void setIntercept(TaggedRisk.Simple<InterceptTag> intercept) {
        this.intercept = intercept;
    }

    public void setRedirect(TaggedRisk.Simple<RedirectTag> redirect) {
        this.redirect = redirect;
    }

    public void setResource(TaggedRisk.Simple<ResourceTag> resource) {
        this.resource = resource;
    }

    public void setIntent(TaggedRisk.Simple<IntentTag> intent) {
        this.intent = intent;
    }

    public void setModality(TaggedRisk.Simple<ModalityTag> modality) {
        this.modality = modality;
    }

    public StateTag state() {
        return state;
    }

    @Nullable
    public TaggedRisk.Simple<InterceptTag> intercept() {
        return intercept;
    }

    @Nullable
    public TaggedRisk.Simple<RedirectTag> redirect() {
        return redirect;
    }

    @Nullable
    public TaggedRisk.Simple<ResourceTag> resource() {
        return resource;
    }

    @Nullable
    public TaggedRisk.Simple<IntentTag> intent() {
        return intent;
    }

    @Nullable
    public TaggedRisk.Simple<ModalityTag> modality() {
        return modality;
    }

    public ConflictRisk.Packed collect() {
        List<ConflictRisk> risks = new ArrayList<>();
        risks.add(state.toRisk());
        for (ConflictRisk field : new ConflictRisk[] {intercept, redirect, resource, intent, modality}) {
            if (field != null) {
                risks.add(field);
            }
            else break;
        }
        return new ConflictRisk.Packed(risks);
    }
}
