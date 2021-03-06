/*
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.screens.projecteditor.client.forms;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.kie.workbench.common.screens.projecteditor.client.resources.i18n.ProjectEditorConstants;
import org.kie.workbench.common.services.data.observer.Observer;
import org.kie.workbench.common.services.project.service.model.POM;

import javax.inject.Inject;

public class POMEditorPanel
        implements IsWidget {

    private final POMEditorPanelView view;
    private POM model;
    private Observer<POM> observer;

    @Inject
    public POMEditorPanel(final POMEditorPanelView view) {
        this.view = view;

    }

    public void setPOM(POM model, boolean isReadOnly) {
        if (isReadOnly) {
            view.setReadOnly();
        }

        this.model = model;
        observer = new Observer(model);

        view.setGAV(model.getGav());
        view.addArtifactIdChangeHandler(new ArtifactIdChangeHandler() {
            @Override
            public void onChange(String newArtifactId) {
                setTitle(newArtifactId);
            }
        });
        setTitle(model.getGav().getArtifactId());
        view.setDependencies(POMEditorPanel.this.model.getDependencies());
    }

    private void setTitle(final String titleText) {
        if (titleText == null || titleText.isEmpty()) {
            view.setTitleText(ProjectEditorConstants.INSTANCE.ProjectModel());
        } else {
            view.setTitleText(titleText);
        }
    }

    @Override
    public Widget asWidget() {
        return view.asWidget();
    }

    public boolean isDirty() {
        return observer.isDirty(model);
    }
}