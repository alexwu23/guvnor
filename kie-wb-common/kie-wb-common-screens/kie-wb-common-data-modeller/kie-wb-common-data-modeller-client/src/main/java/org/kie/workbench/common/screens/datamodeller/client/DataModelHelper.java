/**
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.screens.datamodeller.client;

import org.kie.workbench.common.screens.datamodeller.client.util.DataModelerUtils;
import org.kie.workbench.common.screens.datamodeller.model.DataModelTO;
import org.kie.workbench.common.screens.datamodeller.model.DataObjectTO;
import org.kie.workbench.common.screens.datamodeller.model.ObjectPropertyTO;
import org.kie.workbench.common.screens.datamodeller.model.PropertyTypeTO;

import java.util.*;

public class DataModelHelper {

    private DataModelTO dataModel;

    // Map linking Objects with the Objects they are being referenced by (e.g. x.y.A --> {u.v.B} means B holds a reference to A)
    private Map<String, Set<String>> referencedBy = new HashMap<String, Set<String>>(10);

    // Map linking DataObjects with the Objects they are referencing by (e.g. u.v.B --> {x.y.A} means B references A)
    private Map<String, Set<String>> references = new HashMap<String, Set<String>>(10);

    // Map that keeps track of the siblings a parent class has.
    private Map<String, Set<String>> siblingsMap = new HashMap<String, Set<String>>(10);

    // Map of all class names and their corresponding labels (if any) that coexist within a project
    private Map<String, String> classNames = new HashMap<String, String>(10);

    // Map of all labelled class names that coexist within a project
    private Map<String, String> labelledClassNames = new TreeMap<String, String>();

    Map <String, String> orderedBaseTypes = new TreeMap<String, String>();

    public DataModelHelper() {
    }

    public Boolean isDataObjectReferenced(String className) {
        Set<String> refs = referencedBy.get(className);
        if ( (refs != null && refs.size() > 0) || siblingsMap.containsKey(className)) return true;
        return false;
    }

    public String getObjectLabelByClassName(String className) {
        return classNames.get(className);
    }

    public List<String> getClassList() {
        List<String> l = new ArrayList<String>(classNames.size());
        l.addAll(classNames.keySet());
        return Collections.unmodifiableList(l);
    }

    public Map<String, String> getLabelledClassMap() {
        return Collections.unmodifiableMap(labelledClassNames);
    }

    public Map <String, String> getOrderedBaseTypes() {
        return orderedBaseTypes;
    }

    // TODO change from listener methods to event observers
    // DataModelHelper methods

    public void dataModelChanged() {
        reset();
    }

    public void dataObjectReferenced(String objectClassName, String subjectClassName) {
        objectReferenced(objectClassName, subjectClassName);
    }

    public void dataObjectUnReferenced(String objectClassName, String subjectClassName) {
        objectUnReferenced(objectClassName, subjectClassName);
    }

    public void dataObjectExtended(String parentClassName, String siblingClassName, Boolean _extends) {
        objectExtended(parentClassName, siblingClassName, _extends);
    }

    public void dataObjectDeleted(String objectClassName) {
        reset();
    }

    public void dataObjectCreated(String objectClassName) {
        reset();
    }

    public void dataObjectSelected(String objectClassName) {
    }

    public void dataObjectUnSelected(String objectClassName) {
    }

    public Boolean isBaseType(String type) {
        return orderedBaseTypes.containsValue(type);
    }

    public Boolean isBeingExtended(String parentClassName) {
        Set s = siblingsMap.get(parentClassName);
        return s != null && s.size() > 0;
    }

    public void setDataModel(DataModelTO dataModel) {
        this.dataModel = dataModel;
        reset();
    }

    public void setBaseTypes(List<PropertyTypeTO> baseTypes) {
        if (baseTypes != null) {
            for (PropertyTypeTO type : baseTypes) {
                orderedBaseTypes.put(type.getName(), type.getClassName());
            }
        }
    }

    // Todo can be improved if required for performance reasons
    // Brute force recalculate all
    private void reset() {
        referencedBy.clear();
        references.clear();
        classNames.clear();
        labelledClassNames.clear();
        siblingsMap.clear();
        if (dataModel != null) {
            for (String extClassName : dataModel.getExternalClasses()) {
                classNames.put(extClassName, null);
                labelledClassNames.put(extClassName, extClassName);
            }
            for (DataObjectTO object : dataModel.getDataObjects()) {
                String className = object.getClassName();
                classNames.put(className, object.getLabel());
                labelledClassNames.put(DataModelerUtils.getDataObjectFullLabel(object), className);

                String superClassName = object.getSuperClassName();
                if (superClassName != null &&  !"".equals(superClassName)) objectExtended(superClassName, className, true);

                for (ObjectPropertyTO prop : object.getProperties()) {
                    if (!prop.isBaseType()) {
                        objectReferenced(prop.getClassName(), object.getClassName());
                    }
                }
            }
        }
    }

    private void objectReferenced(String objectClassName, String subjectClassName) {
        Set<String> refs = referencedBy.get(objectClassName);
        if (refs == null) refs = new HashSet<String>();
        refs.add(subjectClassName);
        referencedBy.put(objectClassName, refs);

        refs = references.get(subjectClassName);
        if (refs == null) refs = new HashSet<String>();
        refs.add(objectClassName);
        references.put(subjectClassName, refs);
    }

    private void objectUnReferenced(String objectClassName, String subjectClassName) {
        Set<String> refs = referencedBy.get(objectClassName);
        if (refs != null && refs.size() > 0) {
            refs.remove(subjectClassName);
        }
//        else ("Error de-referencing data object (referenced object)."));

        refs = references.get(subjectClassName);
        if (refs != null && refs.size() > 0) {
            refs.remove(objectClassName);
        }
//        else ("Error de-referencing data object (referring object)."));
    }

    private void objectExtended(String parentClassName, String siblingClassName, Boolean _extends) {
        Set<String> _siblings = siblingsMap.get(parentClassName);

        if (_extends) {
            if (_siblings != null ) _siblings.add(siblingClassName);
            else {
                _siblings = new HashSet<String>();
                _siblings.add(siblingClassName);
                siblingsMap.put(parentClassName, _siblings);
            }
        } else {
            if (_siblings != null && _siblings.size() > 0) _siblings.remove(siblingClassName);
            if (_siblings.size() == 0) siblingsMap.remove(parentClassName);
//            else ("Superclass referencing error"));
        }
    }
}
