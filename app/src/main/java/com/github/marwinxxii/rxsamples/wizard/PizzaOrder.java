package com.github.marwinxxii.rxsamples.wizard;

public class PizzaOrder {
    private Pizza mType;
    private Size mSize;
    private String mPhone;

    public PizzaOrder(Pizza type, Size size, String phone) {
        mType = type;
        mSize = size;
        mPhone = phone;
    }

    public Pizza getType() {
        return mType;
    }

    public Size getSize() {
        return mSize;
    }

    public String getPhone() {
        return mPhone;
    }
}
