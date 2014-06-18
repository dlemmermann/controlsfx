/**
 * Copyright (c) 2014, ControlsFX
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of ControlsFX, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL CONTROLSFX BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.controlsfx.dialog;

import impl.org.controlsfx.ImplUtils;

import java.util.HashMap;
import java.util.List;

import javafx.beans.NamedArg;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.scene.Node;

import org.controlsfx.control.ButtonBar.ButtonType;
import org.controlsfx.control.action.Action;
import org.controlsfx.tools.ValueExtractor;

public class Wizard {
    
    private Dialog dialog;
    
    private int previousPageIndex = 0;
    private int currentPageIndex = 0;
    
    private final ObservableList<WizardPage> pages = FXCollections.observableArrayList();
    private final ObservableMap<String, Object> settings = FXCollections.observableHashMap();
    
    private final Action ACTION_PREVIOUS = new DialogAction(/*Localization.asKey("wizard.previous.button")*/"Previous", ButtonType.BACK_PREVIOUS, false, false, false) { //$NON-NLS-1$
        @Override public void handle(ActionEvent ae) {
            previousPageIndex = currentPageIndex;
            currentPageIndex--;
            validateCurrentPageIndex();
            updatePage(dialog);
            validateActionState();
        }
    };
    private final Action ACTION_NEXT = new DialogAction(/*Localization.asKey("wizard.next.button")*/"Next", ButtonType.NEXT_FORWARD, false, false, true) { //$NON-NLS-1$
        @Override public void handle(ActionEvent ae) {
            previousPageIndex = currentPageIndex;
            currentPageIndex++;
            validateCurrentPageIndex();
            updatePage(dialog);
            validateActionState();
        }
    };
    private final Action ACTION_FINISH = new DialogAction(/*Localization.asKey("wizard.finish.button")*/"Finish", ButtonType.FINISH, false, true, true) { //$NON-NLS-1$
        
    };
    
    
    // --- pages
    public final ObservableList<WizardPage> getPages() {
        return pages;
    }
    
    
    // --- settings
    public final ObservableMap<String, Object> getSettings() {
        return settings;
    }
    
    
    // --- Properties
    private static final Object USER_DATA_KEY = new Object();
    
    // A map containing a set of properties for this Wizard
    private ObservableMap<Object, Object> properties;

    /**
      * Returns an observable map of properties on this Wizard for use primarily
      * by application developers.
      *
      * @return an observable map of properties on this Wizard for use primarily
      * by application developers
     */
     public final ObservableMap<Object, Object> getProperties() {
        if (properties == null) {
            properties = FXCollections.observableMap(new HashMap<Object, Object>());
        }
        return properties;
    }
    
    /**
     * Tests if this Wizard has properties.
     * @return true if this Wizard has properties.
     */
     public boolean hasProperties() {
        return properties != null && !properties.isEmpty();
    }

     
    // --- UserData
    /**
     * Convenience method for setting a single Object property that can be
     * retrieved at a later date. This is functionally equivalent to calling
     * the getProperties().put(Object key, Object value) method. This can later
     * be retrieved by calling {@link Wizard#getUserData()}.
     *
     * @param value The value to be stored - this can later be retrieved by calling
     *          {@link Wizard#getUserData()}.
     */
    public void setUserData(Object value) {
        getProperties().put(USER_DATA_KEY, value);
    }

    /**
     * Returns a previously set Object property, or null if no such property
     * has been set using the {@link Wizard#setUserData(java.lang.Object)} method.
     *
     * @return The Object that was previously set, or null if no property
     *          has been set or if null was set.
     */
    public Object getUserData() {
        return getProperties().get(USER_DATA_KEY);
    }
    
    
    
    public Action show() {
        dialog = new Dialog(null, "WIZARD POWER!");
        dialog.setMasthead("Masthead text");
        dialog.getActions().setAll(ACTION_PREVIOUS, ACTION_NEXT, ACTION_FINISH, Dialog.ACTION_CANCEL);
        
        updatePage(dialog);
        validateActionState();
        
        // --- show the wizard!
        return dialog.show();
    }
    
    private void updatePage(Dialog dialog) {
        WizardPage previousPage;
        WizardPage newPage;
        
        final boolean goingForward = currentPageIndex > previousPageIndex;
        
        if (previousPageIndex >= 0 && previousPageIndex < getPages().size()) {
            previousPage = getPages().get(previousPageIndex);
            
            // if we are going forward in the wizard, we read in the settings 
            // from the page and store them in the settings map.
            // If we are going backwards, we do nothing
            if (goingForward) {
                readSettings(previousPage);
            }
            
            // give the previous wizard page a chance to update the pages list
            // based on the settings it has received
            if (previousPage != null) {
                previousPage.updatePages(this);
            }
        }
        
        // we get the previous page (i.e. the one we were just on before the
        // previous / next button was clicked), and we ask it what page
        // should be previous / next. Then we look up that index and record
        // that for next time this method is called.
        final boolean firstCall = currentPageIndex == 0 && previousPageIndex == 0;
        
        WizardPage currentPage = getPages().get(currentPageIndex);
        if (firstCall) {
            newPage = currentPage;
        } else {
            // now that the pages are updated, we need to re-find the current page
            // so that we know its new index.
            // then we update the page index to be based off the new index
            int newPageIndex = getPages().indexOf(currentPage);
            
            currentPageIndex = newPageIndex;
    
            // and we go get that page
            newPage = getPages().get(newPageIndex);
        }
        
        dialog.setContent(newPage == null ? null : newPage.getContent());
    }
    
    private void validateCurrentPageIndex() {
        final int pageCount = getPages().size();
    
        if (currentPageIndex < 0) {
            currentPageIndex = 0;
        } else if (currentPageIndex > pageCount - 1) {
            currentPageIndex = pageCount - 1;
        }
    }
    
    private void validateActionState() {
        final int pageCount = getPages().size();
        final boolean atEndOfWizard = currentPageIndex == pageCount - 1;
        final List<Action> actions = dialog.getActions();
        
        ACTION_PREVIOUS.setDisabled(currentPageIndex == 0);
        
        if (atEndOfWizard) {
            actions.remove(ACTION_NEXT);
            actions.add(ACTION_FINISH);
        } else {
            if (! actions.contains(ACTION_NEXT)) {
                actions.add(ACTION_NEXT);
            }
            actions.remove(ACTION_FINISH);
        }
        
        // remove actions from the previous page
        WizardPage previousPage = getPages().get(previousPageIndex);
        actions.removeAll(previousPage.getActions());
        
        // add in the actions for the new page
        WizardPage currentPage = getPages().get(currentPageIndex);
        actions.addAll(currentPage.getActions());
    }
    
    private int settingCounter;
    private void readSettings(WizardPage page) {
        // for now we cannot know the structure of the page, so we just drill down
        // through the entire scenegraph (from page.content down) until we get
        // to the leaf nodes. We stop only if we find a node that is a
        // ValueContainer (either by implementing the interface), or being 
        // listed in the internal valueContainers map.
        
        settingCounter = 0;
        checkNode(page.getContent());
    }
    
    private boolean checkNode(Node n) {
        boolean success = readSetting(n);
        
        if (success) {
            // we've added the setting to the settings map and we should stop drilling deeper
            return true;
        } else {
            // go into children of this node (if possible) and see if we can get
            // a value from them (recursively)
            List<Node> children = ImplUtils.getChildren(n, false);
            
            // we're doing a depth-first search, where we stop drilling down
            // once we hit a successful read
            for (Node child : children) {
                boolean childSuccess = checkNode(child);
                if (childSuccess) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private boolean readSetting(Node n) {
        if (n == null) {
            return false;
        }

        Object setting = ValueExtractor.getValue(n);
        
        if (setting != null) {
            // save it into the settings map.
            // if the node has an id set, we will use that as the setting name
            String settingName = n.getId();
            
            // but if the id is not set, we will use a generic naming scheme
            if (settingName == null || settingName.isEmpty()) {
                settingName = "page_" + previousPageIndex + ".setting_" + settingCounter; 
            }
            
            getSettings().put(settingName, setting);
            
            settingCounter++;
        }
        
        return setting != null;
    }
    
    
    // TODO this should just contain a ControlsFX Form, but for now it is hand-coded
    public static class WizardPage {
        private Node content;
        private final ObservableList<Action> actions;
        
        public WizardPage() {
            this(null);
        }
        
        public WizardPage(@NamedArg("content") Node content) {
            this(content, (Action[]) null);
        }
        
        public WizardPage(@NamedArg("content") Node content, Action... actions) {
            this.content = content;
            this.actions = actions == null ? 
                    FXCollections.observableArrayList() : FXCollections.observableArrayList(actions);
        }
        
        public final Node getContent() {
            return content;
        }
        
        public ObservableList<Action> getActions() {
            return actions;
        }
        
        public void updatePages(Wizard wizard) {
            // no-op
        }
    }
}