package com.example.ymdbanking.model;


public class Payee
{

    private String payeeID;
    private String payeeName;

    public Payee()
    {
        //Empty constructor
    }

    /**
     *
     * @param payeeID - payee's ID
     * @param payeeName - name for payee
     */
    public Payee (String payeeID, String payeeName)
    {
        this.payeeID = payeeID;
        this.payeeName = payeeName;
    }

    public String getPayeeName() {
        return payeeName;
    }
    public String getPayeeID() { return payeeID; }
    public void setPayeeID(String payeeID) {this.payeeID = payeeID;}
    public void setPayeeName(String payeeName) {this.payeeName = payeeName;}

    public String toString() { return (payeeName + " (" + payeeID + ")"); }
}
