package com.linus.application.ui.util.components.formulars;

import com.vaadin.flow.component.slider.Slider;

public class StarSlider extends Slider {
  public StarSlider(String category) {
    super(category);
    this.setMin(0);
    this.setMax(5.0);
    this.setStep(0.5);
    this.setWidthFull();
  }
}
