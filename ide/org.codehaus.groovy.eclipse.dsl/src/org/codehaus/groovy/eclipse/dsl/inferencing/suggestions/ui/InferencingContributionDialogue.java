/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.ui;

import java.util.List;

import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.GroovyMethodSuggestion;
import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.GroovyMethodSuggestion.MethodParameter;
import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.GroovySuggestionDeclaringType;
import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.IGroovySuggestion;
import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.SuggestionDescriptor;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * If the dialogue is opened with an existing declaring type, the declaring type
 * cannot be changed.
 * Therefore it means no support for refactor move of an existing suggestion
 * through the dialogue.
 * <p>
 * Refactor move of an existing suggestion from one declaring type to another
 * may be possible in the future
 * </p>
 * 
 * 
 * @author Nieraj Singh
 * @created 2011-05-13
 */
public class InferencingContributionDialogue extends AbstractDialogue {
    public static final DialogueDescriptor DIALOGUE_DESCRIPTOR = new DialogueDescriptor("Add a Groovy Inferencing Suggestion",
            "Groovy Inferencing Suggestion", "icons/GROOVY.png");

    enum ControlTypes implements IDialogueControlDescriptor {
        NAME("Name", "Enter a type name"),

        DECLARING_TYPE("Declaring Type", "Enter or browse for a declaring type"),

        TYPE("Type", "Enter or browse for a type"),

        IS_STATIC("  Is static", "Is the type static"),

        PROPERTY("Property", "Is type a property"),

        METHOD("Method", "Is type a method"),

        PARAMETERS("Parameters", "Enter parameters"),

        USE_NAMED_ARGUMENTS("  Use named arguments", "Should use named arguments?"),

        JAVA_DOC("Java Doc", "Enter Java Doc"),

        ADD("Add", "Add a suggestion"),

        REMOVE("Remove", "Remove a suggestions"),

        UP("Up", "Move suggestion up"),

        DOWN("Down", "Move suggestion down");

        private String label;

        private String toolTipText;

        private ControlTypes(String label, String toolTipText) {
            this.label = label;
            this.toolTipText = toolTipText;
        }

        public String getLabel() {

            return label;
        }

        public String getToolTipText() {

            return toolTipText;
        }

    }

    private Point labelControlOffset;

    private boolean isStatic;

    private boolean isMethod = false;

    private String name;

    private String javaDoc;

    private String declaringTypeName;

    private String suggestionType;

    private boolean isActive;

    private List<MethodParameter> initialParameters;

    private IGroovySuggestion currentSuggestion;

    private boolean editDeclaringType = true;

    private boolean useNamedParameters;

    private ParameterTable table;

    private IProject project;

    /**
     * This constructor is used to edit an existing suggestion. Editing a
     * declaring type is not yet supported.
     */
    public InferencingContributionDialogue(Shell parentShell, IGroovySuggestion suggestion, IProject project) {
        super(parentShell, DIALOGUE_DESCRIPTOR);
        this.project = project;
        setSuggestion(suggestion);
    }

    /**
     * This constructor is used to add new suggestion to an existing declaring
     * type
     */
    public InferencingContributionDialogue(Shell parentShell, GroovySuggestionDeclaringType declaringType, IProject project) {
        this(parentShell, project, null, declaringType, false, true);
    }

    /**
     * This constructor is used to add a new suggestion. The user is expected to
     * specify the declaring type in the UI controls.
     */
    public InferencingContributionDialogue(Shell parentShell, IProject project) {
        this(parentShell, project, null, null, true, true);

    }

    protected InferencingContributionDialogue(Shell parentShell, IProject project, IGroovySuggestion currentSuggestion,
            GroovySuggestionDeclaringType declaringType, boolean editDeclaringType, boolean isActive) {
        super(parentShell, DIALOGUE_DESCRIPTOR);
        this.project = project;
        this.currentSuggestion = currentSuggestion;
        this.declaringTypeName = declaringType != null ? declaringType.getName() : null;
        this.editDeclaringType = editDeclaringType;
        this.isActive = isActive;
    }

    /**
     * May be null if there is no suggestion that is being edited. If the
     * dialogue is
     * being used to add a new suggestion, current suggestion will return null,
     * as the dialogue
     * doesn't create a suggestion. Rather the dialogue creates a suggestion
     * descriptor that can be
     * used to either create the actual suggestion or edit an existing one
     * outside of the dialogue logic.
     * 
     * @return
     */
    public IGroovySuggestion getCurrentSuggestion() {
        return currentSuggestion;
    }

    public SuggestionDescriptor getSuggestionChange() {
        return new SuggestionDescriptor(declaringTypeName, isStatic, isMethod, declaringTypeName, javaDoc, suggestionType,
                useNamedParameters, table.getMethodParameter(), isActive);
    }

    protected void setSuggestion(IGroovySuggestion suggestion) {
        this.currentSuggestion = suggestion;
        if (currentSuggestion != null) {
            isStatic = currentSuggestion.isStatic();
            name = currentSuggestion.getName();
            declaringTypeName = currentSuggestion.getDeclaringType().getName();
            javaDoc = currentSuggestion.getJavaDoc();
            suggestionType = currentSuggestion.getType();
            isActive = currentSuggestion.isActive();
            if (currentSuggestion instanceof GroovyMethodSuggestion) {
                GroovyMethodSuggestion method = (GroovyMethodSuggestion) currentSuggestion;
                initialParameters = method.getMethodArguments();
                useNamedParameters = method.useNamedArguments();
                isMethod = true;
            }
        }
    }

    @Override
    protected void createCommandArea(Composite parent) {
        createFieldAreas(parent);
        createDocumentationArea(parent);
    }

    protected IJavaProject getJavaProject() {
        return project != null ? JavaCore.create(project) : null;
    }

    protected void createFieldAreas(Composite parent) {
        LabeledTextControl nameControl = new LabeledTextControl(ControlTypes.NAME, getOffsetLabelLocation(), name);
        nameControl.createControlArea(parent);
        nameControl.addSelectionListener(new IControlSelectionListener() {

            public void handleSelection(ControlSelectionEvent event) {
                Object selection = event.getSelectionData();
                if (selection instanceof String) {
                    name = (String) selection;
                }
            }
        });

        JavaTextDialogueControl declaringTypeControl = new JavaTextDialogueControl(ControlTypes.DECLARING_TYPE,
                getOffsetLabelLocation(), declaringTypeName, getJavaProject());
        declaringTypeControl.createControlArea(parent);
        if (!editDeclaringType) {
            declaringTypeControl.setEnabled(false);
        }
        declaringTypeControl.addSelectionListener(new IControlSelectionListener() {

            public void handleSelection(ControlSelectionEvent event) {
                Object selection = event.getSelectionData();
                if (selection instanceof String) {
                    declaringTypeName = (String) selection;
                }
            }
        });

        JavaTextDialogueControl typeControl = new JavaTextDialogueControl(ControlTypes.TYPE, getOffsetLabelLocation(),
                suggestionType, getJavaProject());
        typeControl.createControlArea(parent);
        typeControl.addSelectionListener(new IControlSelectionListener() {

            public void handleSelection(ControlSelectionEvent event) {
                Object selection = event.getSelectionData();
                if (selection instanceof String) {
                    suggestionType = (String) selection;
                }
            }
        });

        ButtonDialogueControl isStaticButton = new ButtonDialogueControl(ControlTypes.IS_STATIC, SWT.CHECK, isStatic);
        isStaticButton.createControlArea(parent);
        isStaticButton.addSelectionListener(new IControlSelectionListener() {

            public void handleSelection(ControlSelectionEvent event) {
                Object selection = event.getSelectionData();
                if (selection instanceof Boolean) {
                    isStatic = ((Boolean) selection).booleanValue();
                }
            }
        });

        // Set Property as the default selected button
        ControlTypes defaultSuggestionTypeButton = isMethod ? ControlTypes.METHOD : ControlTypes.PROPERTY;

        RadioSelectionDialogueControl radioSelection = new RadioSelectionDialogueControl(new IDialogueControlDescriptor[] {
                ControlTypes.PROPERTY, ControlTypes.METHOD }, defaultSuggestionTypeButton);

        radioSelection.createControlArea(parent);

        table = new ParameterTable(getJavaProject(), initialParameters, useNamedParameters);

        table.createControlArea(parent);

        // If the default is not a method suggestion, disable the parameter tree
        if (!isMethod) {
            table.setEnabled(false);
        }

        table.addSelectionListener(new IControlSelectionListener() {

            public void handleSelection(ControlSelectionEvent event) {
                Object selection = event.getSelectionData();
                if (event.getControlDescriptor() == ControlTypes.USE_NAMED_ARGUMENTS && selection instanceof Boolean) {
                    useNamedParameters = ((Boolean) selection).booleanValue();
                }
            }
        });

        radioSelection.addSelectionListener(new IControlSelectionListener() {

            public void handleSelection(ControlSelectionEvent event) {
                IDialogueControlDescriptor descriptor = event.getControlDescriptor();
                if (descriptor == ControlTypes.PROPERTY) {
                    table.setEnabled(false);
                    isMethod = false;
                } else if (descriptor == ControlTypes.METHOD) {
                    table.setEnabled(true);
                    isMethod = true;
                }
            }
        });

    }

    protected Point getOffsetLabelLocation() {
        if (labelControlOffset == null) {
            IDialogueControlDescriptor[] descriptors = new IDialogueControlDescriptor[] { ControlTypes.DECLARING_TYPE,
                    ControlTypes.IS_STATIC, ControlTypes.TYPE, ControlTypes.NAME

            };
            String[] labelNames = new String[descriptors.length];
            for (int i = 0; i < descriptors.length; ++i) {
                labelNames[i] = descriptors[i].getLabel();
            }
            labelControlOffset = getOffsetLabelLocation(labelNames);
        }
        return labelControlOffset;
    }

    protected void createDocumentationArea(Composite parent) {

        DocumentDialogueControl docControl = new DocumentDialogueControl(ControlTypes.JAVA_DOC, null, javaDoc);
        docControl.createControlArea(parent);
        docControl.addSelectionListener(new IControlSelectionListener() {

            public void handleSelection(ControlSelectionEvent event) {
                if (event.getSelectionData() instanceof String) {
                    javaDoc = (String) event.getSelectionData();
                }
            }
        });

    }

}