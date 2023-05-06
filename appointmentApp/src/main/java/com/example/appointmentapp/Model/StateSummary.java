package com.example.appointmentapp.Model;

/**
 * This class represents a summary of states with a count of how much it occurs.
 * It provides a constructor for creating StateSummary instances and getter and setter methods for each property.
 */
public class StateSummary {

    private String state;
    private int stateCount;

    /**
     * Constructs a StateSummary object with the specified state and state count.
     * @param state The name of the state.
     * @param stateCount The count of occurrences for the state.
     */
    public StateSummary (String state, int stateCount) {

        this.state = state;
        this.stateCount = stateCount;


    }

    /** Getters and Setters */

    public String getState() {
        return state;

    }

    public void setState(String state){
        this.state = state;

    }

    public int getStateCount() {
        return stateCount;

    }

    public void setStateCount(int stateCount) {
        this.stateCount  = stateCount;

    }

}


