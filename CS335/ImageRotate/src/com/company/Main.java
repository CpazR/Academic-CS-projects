package com.company;

import com.company.ApplicationGUI.ApplicationContext;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        var image = new RotatableImage("./test_assets/strawberry.jpg");
        new ApplicationContext("Rotating Image",List.of(image));
    }
}
