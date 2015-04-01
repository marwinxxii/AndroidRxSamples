package com.github.marwinxxii.rxsamples.wizard;

import com.github.marwinxxii.rxsamples.R;

public enum Pizza {
    Pepperoni(R.drawable.pizza_pepperoni),
    Margherita(R.drawable.pizza_margherita),
    Hawaiian(R.drawable.pizza_hawaiian),
    QuattroFormaggi(R.drawable.pizza_quattro);

    public final int drawableId;

    Pizza(int drawableId) {
        this.drawableId = drawableId;
    }
}
