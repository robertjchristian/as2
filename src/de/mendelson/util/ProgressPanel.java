//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/ProgressPanel.java,v 1.1 2012/04/18 14:10:41 heller Exp $
package de.mendelson.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Progress panel to display status informations.
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class ProgressPanel extends JPanel {

    private final List<ProgressRequest> progressList = Collections.synchronizedList(new ArrayList<ProgressRequest>());

    /** Creates new form ProgressPanel */
    public ProgressPanel() {
        this.initComponents();
    }

    /**Adds a new progress to display to the progress bar. Its possible to add several requests,
     * just use unique ids for each request*/
    public void startProgressIndeterminate(String progressDetails, String uniqueId) {
        ProgressRequest request = new ProgressRequest(progressDetails, uniqueId);
        request.setIndeterminate(true);
        synchronized (this.progressList) {
            this.progressList.add(request);
        }
        this.displayProgressBar(request);
    }

    private synchronized void displayProgressBar(final ProgressRequest request) {
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                ProgressPanel.this.jLabelProgressDetails.setText(request.getDisplay());
                ProgressPanel.this.jProgressBar.setIndeterminate(request.isIndeterminate());
                if (request.isIndeterminate()) {
                    ProgressPanel.this.jProgressBar.setStringPainted(false);
                } else {
                    ProgressPanel.this.jProgressBar.setMinimum(request.getMinValue());
                    ProgressPanel.this.jProgressBar.setMaximum(request.getMaxValue());
                    ProgressPanel.this.jProgressBar.setValue(request.getActualValue());
                }
            }
        };
        try{
            SwingUtilities.invokeAndWait(runnable);
        }
        catch( Exception e ){
            e.printStackTrace();
        }
        
    }

    /**Adds a new progress to display to the progress bar. Its possible to add several requests,
     * just use unique ids for each request. The last request is always displayed*/
    public void startProgress(String display, String uniqueId, int min, int max) {
        ProgressRequest request = new ProgressRequest(display, uniqueId);
        request.setIndeterminate(false);
        request.setMinValue(min);
        request.setMaxValue(max);
        request.setActualValue(0);
        synchronized (this.progressList) {
            this.progressList.add(request);
        }
        this.displayProgressBar(request);
    }

    /**This is ignored if the unique id is not assigned to a progress request - anyway always the
     * last progress request is displayed
     */
    public void setProgressValue(String uniqueId, int progress) {
        ProgressRequest foundRequest = null;
        ProgressRequest actualDisplayedProgress = null;
        synchronized (this.progressList) {
            //find the progress request to delete
            for (ProgressRequest request : this.progressList) {
                if (request.uniqueId.equals(uniqueId)) {
                    foundRequest = request;
                    break;
                }
            }
            actualDisplayedProgress = this.progressList.get(this.progressList.size() - 1);
            //update the display if the set progress bar is the actual one
            if (foundRequest != null && !foundRequest.isIndeterminate()) {
                foundRequest.setActualValue(progress);
                if (foundRequest.equals(actualDisplayedProgress)) {
                    this.displayProgressBar(foundRequest);
                }
            }
        }
    }

    /**Triies to stop a progress and does not care if it does not exist*/
    public void stopProgressIfExists(String uniqueId) {
        ProgressRequest foundRequest = null;
        synchronized (this.progressList) {
            //find the progress request to delete
            for (ProgressRequest request : this.progressList) {
                if (request.uniqueId.equals(uniqueId)) {
                    foundRequest = request;
                    break;
                }
            }
            if (foundRequest != null) {
                //now delete the found request
                this.progressList.remove(foundRequest);
                //no more progress entries?
                if (this.progressList.isEmpty()) {
                    //disable the progress display
                    this.jProgressBar.setIndeterminate(false);
                    this.jProgressBar.setMinimum(0);
                    this.jProgressBar.setMaximum(0);
                    this.jProgressBar.setValue(0);
                    this.jProgressBar.setStringPainted(false);
                    this.jLabelProgressDetails.setText(null);
                } else {
                    //get last entry and display its progress text
                    ProgressRequest progressToDisplay = this.progressList.get(this.progressList.size() - 1);
                    this.displayProgressBar(progressToDisplay);
                }
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabelProgressDetails = new javax.swing.JLabel();
        jProgressBar = new javax.swing.JProgressBar();

        setPreferredSize(new java.awt.Dimension(100, 12));
        setLayout(new java.awt.GridBagLayout());

        jLabelProgressDetails.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabelProgressDetails.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabelProgressDetails, gridBagConstraints);

        jProgressBar.setBorder(null);
        jProgressBar.setPreferredSize(new java.awt.Dimension(100, 12));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
        add(jProgressBar, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabelProgressDetails;
    private javax.swing.JProgressBar jProgressBar;
    // End of variables declaration//GEN-END:variables

    /**Stores information about a progress display request*/
    private static class ProgressRequest {

        private String display;
        private String uniqueId;
        private boolean indeterminate = false;
        private int minValue = 0;
        private int maxValue = 0;
        private int actualValue = 0;

        public ProgressRequest(String display, String uniqueId) {
            this.display = display;
            this.uniqueId = uniqueId;
        }

        /**
         * @return the display
         */
        public String getDisplay() {
            return display;
        }

        /**
         * @return the uniqueId
         */
        public String getUniqueId() {
            return uniqueId;
        }

        /**Overwrite the equal method of object, an object will be equal
         *if the key is equal !
         *@param anObject object ot compare
         */
        @Override
        public boolean equals(Object anObject) {
            if (anObject == this) {
                return (true);
            }
            if (anObject != null && anObject instanceof ProgressRequest) {
                ProgressRequest object = (ProgressRequest) anObject;
                return (object.uniqueId.equals(this.uniqueId));
            }
            return (false);
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 29 * hash + (this.uniqueId != null ? this.uniqueId.hashCode() : 0);
            return hash;
        }

        /**
         * @return the minValue
         */
        public int getMinValue() {
            return minValue;
        }

        /**
         * @param minValue the minValue to set
         */
        public void setMinValue(int minValue) {
            this.minValue = minValue;
        }

        /**
         * @return the maxValue
         */
        public int getMaxValue() {
            return maxValue;
        }

        /**
         * @param maxValue the maxValue to set
         */
        public void setMaxValue(int maxValue) {
            this.maxValue = maxValue;
        }

        /**
         * @return the actualValue
         */
        public int getActualValue() {
            return actualValue;
        }

        /**
         * @param actualValue the actualValue to set
         */
        public void setActualValue(int actualValue) {
            this.actualValue = actualValue;
        }

        /**
         * @return the indeterminate
         */
        public boolean isIndeterminate() {
            return indeterminate;
        }

        /**
         * @param indeterminate the indeterminate to set
         */
        public void setIndeterminate(boolean indeterminate) {
            this.indeterminate = indeterminate;
        }
    }
}
